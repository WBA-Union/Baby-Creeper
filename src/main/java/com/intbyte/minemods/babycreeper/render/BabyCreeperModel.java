package com.intbyte.minemods.babycreeper.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BabyCreeperModel extends ModelBase {
    public ModelRenderer head;
    public ModelRenderer creeperArmor;
    public ModelRenderer body;
    public ModelRenderer leg1;
    public ModelRenderer leg2;
    public ModelRenderer leg3;
    public ModelRenderer leg4;

    public BabyCreeperModel() {
        this(0.0F);
    }

    public BabyCreeperModel(float scale) {
        boolean lvt_2_1_ = true;
        float y_offset = 4;

        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-4.0F, -3.0F + y_offset, -4.0F, 8, 7, 8, scale);
        this.head.setRotationPoint(0.0F, 6.0F, 0.0F);
        this.creeperArmor = new ModelRenderer(this, 32, 0);
        this.creeperArmor.addBox(-4.0F, -8.0F + y_offset, -4.0F, 8, 8, 8, scale + 0.5F);
        this.creeperArmor.setRotationPoint(0.0F, 6.0F, 0.0F);
        this.body = new ModelRenderer(this, 16, 16);
        this.body.addBox(-3.0F, 4.0F + y_offset, -2.0F, 6, 8, 4, scale);
        this.body.setRotationPoint(0.0F, 6.0F, 0.0F);
        int leg_x = 2, leg_z = 4;

        this.leg1 = new ModelRenderer(this, 0, 16);
        this.leg1.addBox(-2.0F, 3, -3.0F, leg_x, 3, leg_z, scale);
        this.leg1.setRotationPoint(-2.0F, 18.0F, 4.0F);
        this.leg2 = new ModelRenderer(this, 0, 16);
        this.leg2.addBox(0.0F, 3, -3.0F, leg_x, 3, leg_z, scale);
        this.leg2.setRotationPoint(2.0F, 18.0F, 4.0F);
        this.leg3 = new ModelRenderer(this, 0, 16);
        this.leg3.addBox(-2.0F, 3, -1.0F, leg_x, 3, leg_z, scale);
        this.leg3.setRotationPoint(-2.0F, 18.0F, -4.0F);
        this.leg4 = new ModelRenderer(this, 0, 16);
        this.leg4.addBox(0F, 3, -1.0F, leg_x, 3, leg_z, scale);
        this.leg4.setRotationPoint(2.0F, 18.0F, -4.0F);
    }

    public void render(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, p_78088_1_);
        this.head.render(p_78088_7_);
        this.body.render(p_78088_7_);
        this.leg1.render(p_78088_7_);
        this.leg2.render(p_78088_7_);
        this.leg3.render(p_78088_7_);
        this.leg4.render(p_78088_7_);
    }

    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
        this.head.rotateAngleY = p_78087_4_ * 0.017453292F;
        this.head.rotateAngleX = p_78087_5_ * 0.017453292F;
        float speed = 1.5f;

        p_78087_1_ *= speed;

        this.leg1.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
        this.leg2.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + 3.1415927F) * 1.4F * p_78087_2_;
        this.leg3.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + 3.1415927F) * 1.4F * p_78087_2_;
        this.leg4.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_;
    }
}

