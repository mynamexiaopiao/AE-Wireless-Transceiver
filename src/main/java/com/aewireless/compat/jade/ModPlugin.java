package com.aewireless.compat.jade;

import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlock;
import com.aewireless.block.WirelessConnectBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(AeWireless.MOD_ID)
public class ModPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AEWirelessTransceiverProvider.INSTANCE, WirelessConnectBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // 遍历组件常量，逐一注册
        registration.registerBlockComponent(WirelessConnectComponents.TOOLTIP, WirelessConnectBlock.class);

    }
}
