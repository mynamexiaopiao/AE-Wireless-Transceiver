package com.aewireless.level;


import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessData;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AeWireless.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        WirelessWorldData worldData = WirelessWorldData.get(event.getServer().getLevel(Level.OVERWORLD));
        if (worldData != null) {
            worldData.data = new HashMap<>(WirelessData.DATA);
            worldData.setDirty();
        }
        WirelessData.DATA.clear();
    }
}
