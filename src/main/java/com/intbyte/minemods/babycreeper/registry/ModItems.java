package com.intbyte.minemods.babycreeper.registry;

import com.intbyte.minemods.babycreeper.BabyCreeperMod;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BabyCreeperMod.MOD_ID);

    public static final RegistryObject<Item> BABY_CREEPER_SPAWN_EGG = ITEMS.register("baby_creeper_spawn_egg", () ->
            new ForgeSpawnEggItem(ModEntities.BABY_CREEPER, 0xFFFFFF, 0x000000, new Item.Properties()));

    private ModItems() {
    }
}
