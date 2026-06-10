package com.intbyte.minemods.babycreeper;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = CreeperMod.MODID, name = CreeperMod.NAME, version = CreeperMod.VERSION)
public class CreeperMod {
    public static final String MODID = "babycreeper";
    public static final String NAME = "Baby Creeper Mod";
    public static final String VERSION = "1.0";
    public static CreeperMod mod;
    @SidedProxy(clientSide = "com.intbyte.minemods.babycreeper.render.ClientRenderProxy", serverSide = "com.intbyte.minemods.babycreeper.CommonProxy")
    public static CommonProxy proxy;
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        mod = this;
    }
}
