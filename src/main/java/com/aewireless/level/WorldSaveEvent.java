package com.aewireless.level;


import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class WorldSaveEvent {


    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            WirelessWorldData worldData = WirelessWorldData.get(event.getLevel().getServer().getLevel(Level.OVERWORLD));

            if (worldData != null && !worldData.data.isEmpty()){
                WirelessData.DATA = worldData.data;

            }
        }
    }

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (!event.getLevel().isClientSide()) {
            WirelessWorldData worldData = WirelessWorldData.get(event.getLevel().getServer().getLevel(Level.OVERWORLD));
            if (worldData != null) {
                worldData.data = new HashMap<>(WirelessData.DATA);
                worldData.setDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppingEvent event) {
        WirelessData.DATA.clear();
    }
}
