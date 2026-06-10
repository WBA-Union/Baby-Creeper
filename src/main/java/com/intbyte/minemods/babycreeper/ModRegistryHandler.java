package com.intbyte.minemods.babycreeper;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = CreeperMod.MODID)
public class ModRegistryHandler {

    @SubscribeEvent
    public static void registerEntities(@Nonnull final RegistryEvent.Register<EntityEntry> event) {
        final ResourceLocation exampleEntity1RegistryName = new ResourceLocation(CreeperMod.MODID, "baby_creeper");


        event.getRegistry().registerAll(EntityEntryBuilder.create().entity(BabyCreeper.class).id(exampleEntity1RegistryName, 1)

                .tracker(80, 1, true).egg(0xFFFFFF, 0x000000).name("baby_creeper").build());
        CreeperMod.proxy.register();
    }
}

