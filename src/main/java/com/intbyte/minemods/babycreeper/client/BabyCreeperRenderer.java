package com.intbyte.minemods.babycreeper.client;

import com.intbyte.minemods.babycreeper.BabyCreeperMod;
import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BabyCreeperRenderer extends MobRenderer<BabyCreeperEntity, BabyCreeperModel<BabyCreeperEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(BabyCreeperMod.MOD_ID, "textures/entity/baby_creeper.png");

    public BabyCreeperRenderer(EntityRendererProvider.Context context,
                                BabyCreeperModel<BabyCreeperEntity> model) {
        super(context, model, 0.1F);
        this.addLayer(new BabyCreeperHelmetLayer(this));
        this.addLayer(new BabyCreeperChargeLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(BabyCreeperEntity entity) {
        return TEXTURE;
    }
}
