package com.intbyte.minemods.babycreeper.render;

import com.intbyte.minemods.babycreeper.BabyCreeper;
import com.intbyte.minemods.babycreeper.CommonProxy;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientRenderProxy extends CommonProxy {
    public void register() {
        RenderingRegistry.registerEntityRenderingHandler(BabyCreeper.class, RenderBabyCreeper::new);
    }
}
