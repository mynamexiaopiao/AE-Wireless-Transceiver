package com.aewireless.register;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber
public class ModTab {
    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModRegister.WIRELESS_TRANSCEIVER.get().asItem());
        }
    }
}
