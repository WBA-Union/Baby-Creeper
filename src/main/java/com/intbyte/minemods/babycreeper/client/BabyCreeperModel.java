package com.intbyte.minemods.babycreeper.client;

import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class BabyCreeperModel<T extends BabyCreeperEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public BabyCreeperModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        float yOffset = 4.0F;
        int legX = 2;
        int legZ = 4;

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -3.0F + yOffset, -4.0F, 8.0F, 7.0F, 8.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16)
                .addBox(-3.0F, 4.0F + yOffset, -2.0F, 6.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 3.0F, -3.0F, (float) legX, 3.0F, (float) legZ),
                PartPose.offset(-2.0F, 18.0F, 4.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(0.0F, 3.0F, -3.0F, (float) legX, 3.0F, (float) legZ),
                PartPose.offset(2.0F, 18.0F, 4.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 3.0F, -1.0F, (float) legX, 3.0F, (float) legZ),
                PartPose.offset(-2.0F, 18.0F, -4.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(0.0F, 3.0F, -1.0F, (float) legX, 3.0F, (float) legZ),
                PartPose.offset(2.0F, 18.0F, -4.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public ModelPart getHead() {
        return this.head;
    }

    /**
     * Применяет трансформацию (позицию + поворот) головы к PoseStack.
     * Используется в BabyCreeperHelmetLayer для корректного позиционирования шлема.
     */
    public void translateToHead(PoseStack poseStack) {
        this.head.translateAndRotate(poseStack);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        float speed = 1.5F;
        limbSwing *= speed;
        this.leg1.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg2.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leg3.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leg4.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        if (entity.isDancing()) {
            float beat = ageInTicks * 0.45f;
            float sway = Mth.sin(beat) * 0.18f;
            float bounce = Mth.cos(beat * 2.0f) * 0.22f;
            this.head.yRot = sway;
            this.body.yRot = -sway * 0.65f;
            this.leg1.xRot = bounce;
            this.leg2.xRot = -bounce;
            this.leg3.xRot = -bounce;
            this.leg4.xRot = bounce;
        } else {
            this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
            this.body.yRot = 0.0f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
