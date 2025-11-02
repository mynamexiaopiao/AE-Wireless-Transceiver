package com.aewireless.register;

import appeng.init.client.InitScreens;
import com.aewireless.AeWireless;
import com.aewireless.gui.wireless.WirelessMenu;
import com.aewireless.gui.wireless.WirelessScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;


@EventBusSubscriber
public class RegisterHandler {
    @SubscribeEvent
    public static void reg(RegisterMenuScreensEvent event){
        InitScreens.register(event , ModRegister.WIRELESS_MENU.get(), WirelessScreen::new, "/screens/wireless.json");
    }
}
