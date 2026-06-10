package com.intbyte.minemods.babycreeper.registry;

import com.intbyte.minemods.babycreeper.BabyCreeperMod;
import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BabyCreeperMod.MOD_ID);

    public static final RegistryObject<EntityType<BabyCreeperEntity>> BABY_CREEPER = ENTITY_TYPES.register("baby_creeper", () ->
            EntityType.Builder.of(BabyCreeperEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.9F)
                    .clientTrackingRange(5)
                    .updateInterval(1)
                    .build(BabyCreeperMod.MOD_ID + ":baby_creeper"));

    private ModEntities() {
    }
}
