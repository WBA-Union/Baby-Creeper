package com.intbyte.minemods.babycreeper.client;

import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import com.mojang.math.Axis;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Слой рендеринга шлема на голове малыша крипера.
 *
 * FIX #1: Шлем теперь корректно привязан к голове крипера и поворачивается
 * вместе с ней. Исправление заключается в том, что метод translateToHead()
 * применяет translateAndRotate() головы модели, которая уже содержит
 * актуальный yRot/xRot из setupAnim(). Поэтому helmetModel НЕ получает
 * netHeadYaw/headPitch — они уже учтены через трансформацию головы.
 */
public class BabyCreeperHelmetLayer
        extends RenderLayer<BabyCreeperEntity, BabyCreeperModel<BabyCreeperEntity>> {

    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = new ConcurrentHashMap<>();
    private static final float HELMET_SCALE = 0.94f;

    private final HumanoidModel<BabyCreeperEntity> helmetModel;
    private final TextureAtlas armorTrimAtlas;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;

    public BabyCreeperHelmetLayer(RenderLayerParent<BabyCreeperEntity, BabyCreeperModel<BabyCreeperEntity>> parent) {
        super(parent);
        this.helmetModel = new HumanoidModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR));
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
        this.skullModels = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BabyCreeperEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) return;

        Item item = helmet.getItem();
        if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock skullBlock) {
            this.renderMobHead(poseStack,buffer,packedLight,helmet,skullBlock);return;
        }
        if (!(item instanceof ArmorItem armorItem)) {
            return;
        }

        if (armorItem.getEquipmentSlot() != EquipmentSlot.HEAD) {
            return;
        }

        // Подготавливаем helmetModel: скрываем всё кроме головы, обнуляем углы.
        // FIX #1: НЕ устанавливаем netHeadYaw/headPitch в helmetModel.head —
        // поворот головы уже применён через translateToHead() ниже.
        this.prepareHelmetModel(entity);

        Model model = ForgeHooksClient.getArmorModel(entity, helmet, EquipmentSlot.HEAD, this.helmetModel);

        poseStack.pushPose();

        // FIX #1: translateToHead вызывает head.translateAndRotate(poseStack),
        // что применяет позицию головы И её текущий поворот (yRot/xRot из setupAnim).
        // Шлем будет следовать за головой крипера точно.
        ((BabyCreeperModel<?>) this.getParentModel()).translateToHead(poseStack);

        // Подгоняем размер шлема под голову крипера
        this.fitHumanoidHelmetToBabyCreeperHead(poseStack);

        this.renderArmor(poseStack, buffer, packedLight, entity, helmet, armorItem, model);

        poseStack.popPose();
    }

    /**
     * FIX #1: Подготавливаем helmetModel без установки углов поворота головы.
     * Поворот уже будет применён через translateToHead() в PoseStack.
     */
    private void prepareHelmetModel(BabyCreeperEntity entity) {
        this.helmetModel.setAllVisible(false);
        this.helmetModel.hat.visible = true;
        this.helmetModel.head.visible = true;
        // Сбрасываем все углы головы helmetModel в ноль —
        // поворот применяется через PoseStack (translateToHead), а не через углы модели
        this.helmetModel.crouching = false;
        this.helmetModel.young = entity.isBaby();
        this.helmetModel.riding = false;
        this.helmetModel.head.xRot = 0.0f;
        this.helmetModel.head.yRot = 0.0f;
        this.helmetModel.head.zRot = 0.0f;
        this.helmetModel.hat.copyFrom(this.helmetModel.head);
    }

    private void fitHumanoidHelmetToBabyCreeperHead(PoseStack poseStack) {
        // Смещение вверх чтобы шлем сидел на голове, а не в теле
        poseStack.translate(0.0, 0.46, 0.0);
        // Масштаб подгонки
        poseStack.scale(HELMET_SCALE, HELMET_SCALE, HELMET_SCALE);
    }

    private void renderArmor(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                              BabyCreeperEntity entity, ItemStack stack, ArmorItem armorItem, Model model) {
        if (armorItem instanceof DyeableLeatherItem dyeableLeatherItem) {
            int color = dyeableLeatherItem.getColor(stack);
            float red = (float) FastColor.ARGB32.red(color) / 255.0f;
            float green = (float) FastColor.ARGB32.green(color) / 255.0f;
            float blue = (float) FastColor.ARGB32.blue(color) / 255.0f;
            this.renderModel(poseStack, buffer, packedLight, armorItem, model, red, green, blue,
                    this.getArmorResource(entity, stack, EquipmentSlot.HEAD, null));
            this.renderModel(poseStack, buffer, packedLight, armorItem, model, 1.0f, 1.0f, 1.0f,
                    this.getArmorResource(entity, stack, EquipmentSlot.HEAD, "overlay"));
        } else {
            this.renderModel(poseStack, buffer, packedLight, armorItem, model, 1.0f, 1.0f, 1.0f,
                    this.getArmorResource(entity, stack, EquipmentSlot.HEAD, null));
        }

        Optional<ArmorTrim> trim = ArmorTrim.getTrim(entity.level().registryAccess(), stack);
        trim.ifPresent(armorTrim ->
                this.renderTrim(armorItem.getMaterial(), poseStack, buffer, packedLight, armorTrim, model));

        if (stack.hasFoil()) {
            this.renderGlint(poseStack, buffer, packedLight, model);
        }
    }

    private void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                              ArmorItem armorItem, Model model, float red, float green, float blue,
                              ResourceLocation armorResource) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.armorCutoutNoCull(armorResource));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                red, green, blue, 1.0f);
    }

    private void renderTrim(ArmorMaterial armorMaterial, PoseStack poseStack, MultiBufferSource buffer,
                             int packedLight, ArmorTrim armorTrim, Model model) {
        TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(armorTrim.outerTexture(armorMaterial));
        VertexConsumer vertexConsumer = sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet()));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderGlint(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Model model) {
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorEntityGlint()),
                packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderMobHead(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack stack, AbstractSkullBlock skullBlock) {
        poseStack.pushPose();

        ((BabyCreeperModel<?>) this.getParentModel()).translateToHead(poseStack);

        /*
         * Подгонка позиции
         */
        poseStack.translate(-0.5D, 0.5D, -0.52D);

        /*
         * Размер головы
         */
        poseStack.scale(1.0F, -0.94F, 1.04F);

        SkullBlock.Type skullType = skullBlock.getType();

        SkullModelBase skullModel = this.skullModels.get(skullType);

        if (skullModel == null) {
            poseStack.popPose();
            return;
        }

        GameProfile profile = null;

        CompoundTag tag = stack.getTag();

        if (tag != null && tag.contains("SkullOwner")) {
            profile = net.minecraft.nbt.NbtUtils.readGameProfile(
                    tag.getCompound("SkullOwner")
            );
        }

        RenderType renderType =
                SkullBlockRenderer.getRenderType(
                        skullType,
                        profile
                );

        SkullBlockRenderer.renderSkull(
                null, 0.0F, 0.0F,
                poseStack, buffer, packedLight, skullModel, renderType
        );

        poseStack.popPose();
    }

    public ResourceLocation getArmorResource(BabyCreeperEntity entity, ItemStack stack,
                                              EquipmentSlot slot, @Nullable String type) {
        ArmorItem item = (ArmorItem) stack.getItem();
        String texture = item.getMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        String path = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_1%s.png",
                domain, texture, type == null ? "" : String.format(Locale.ROOT, "_%s", type));
        path = ForgeHooksClient.getArmorTexture(entity, stack, path, slot, type);
        return ARMOR_LOCATION_CACHE.computeIfAbsent(path, ResourceLocation::tryParse);
    }
}
