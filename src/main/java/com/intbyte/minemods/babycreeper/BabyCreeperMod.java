package com.intbyte.minemods.babycreeper;

import com.intbyte.minemods.babycreeper.client.BabyCreeperModel;
import com.intbyte.minemods.babycreeper.client.BabyCreeperRenderer;
import com.intbyte.minemods.babycreeper.entity.BabyCreeperEntity;
import com.intbyte.minemods.babycreeper.registry.ModEntities;
import com.intbyte.minemods.babycreeper.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BabyCreeperMod.MOD_ID)
public class BabyCreeperMod {

    public static final String MOD_ID = "babycreeper";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BabyCreeperMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);
        modEventBus.addListener(this::addCreative);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BABY_CREEPER.get(), BabyCreeperEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.BABY_CREEPER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BabyCreeperEntity::canSpawn,
                SpawnPlacementRegisterEvent.Operation.OR);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.BABY_CREEPER_SPAWN_EGG.get());
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> EntityRenderers.register(
                    ModEntities.BABY_CREEPER.get(),
                    (EntityRendererProvider.Context context) ->
                            new BabyCreeperRenderer(context,
                                    new BabyCreeperModel<>(BabyCreeperModel.createBodyLayer().bakeRoot()))));
        }
    }
}
