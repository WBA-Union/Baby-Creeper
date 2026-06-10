package com.intbyte.minemods.babycreeper.render;


import com.intbyte.minemods.babycreeper.BabyCreeper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderBabyCreeper extends RenderLiving<BabyCreeper> {
    private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("babycreeper:textures/baby_creeper.png");

    public RenderBabyCreeper(RenderManager p_i46186_1_) {
        super(p_i46186_1_, new BabyCreeperModel(), 0.1F);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(BabyCreeper babyCreeper) {
        return CREEPER_TEXTURES;
    }


    protected ResourceLocation getEntityTexture(EntityCreeper p_110775_1_) {
        return CREEPER_TEXTURES;
    }
}
