package com.intbyte.minemods.babycreeper.client;

import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BabyCreeperChargeLayer
        extends RenderLayer<BabyCreeperEntity, BabyCreeperModel<BabyCreeperEntity>> {

    private static final ResourceLocation POWER_LOCATION =
            new ResourceLocation("textures/entity/creeper/creeper_armor.png");

    public BabyCreeperChargeLayer(RenderLayerParent<BabyCreeperEntity, BabyCreeperModel<BabyCreeperEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BabyCreeperEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isEnchantedAppleCharged()) {
            return;
        }
        float xOffset = (entity.tickCount + partialTick) * 0.01f;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.energySwirl(POWER_LOCATION, xOffset, xOffset));
        ((BabyCreeperModel<?>) this.getParentModel()).renderToBuffer(
                poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);
    }
}
