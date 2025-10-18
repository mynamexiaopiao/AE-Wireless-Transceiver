package com.aewireless.register;

import appeng.init.client.InitScreens;
import com.aewireless.gui.wireless.WirelessScreen;

public class RegisterHandler {
    public static final RegisterHandler INSTANCE = new RegisterHandler();


    public void init(){
        registerGui();
    }

    public void registerGui() {
        InitScreens.register(ModRegister.WIRELESS_MENU.get(), WirelessScreen::new, "/screens/wireless.json");
    }
}
