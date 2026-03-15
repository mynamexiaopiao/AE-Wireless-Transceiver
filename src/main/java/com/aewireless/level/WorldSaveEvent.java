package com.aewireless.level;


import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.block.LevelManage;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EventBusSubscriber
public class WorldSaveEvent {


    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            WirelessWorldData worldData = WirelessWorldData.get(Objects.requireNonNull(event.getLevel().getServer()).getLevel(Level.OVERWORLD));
            WirelessBlockWorldData blockWorldData = WirelessBlockWorldData.get(Objects.requireNonNull(event.getLevel().getServer()).getLevel(Level.OVERWORLD));
            if (blockWorldData != null  && !blockWorldData.blockPosList.isEmpty()){
                WirelessBlockManage.setBlockPosList(blockWorldData.blockPosList);
            }
            if (worldData != null && !worldData.data.isEmpty()){
                WirelessData.setDATAMap(worldData.data);
            }
        }
    }


    @SubscribeEvent
    public static void onServerStopped(ServerStoppingEvent event) {
        WirelessWorldData worldData = WirelessWorldData.get(event.getServer().getLevel(Level.OVERWORLD));
        WirelessBlockWorldData blockWorldData = WirelessBlockWorldData.get(event.getServer().getLevel(Level.OVERWORLD));
        if (blockWorldData != null) {
            blockWorldData.blockPosList = new HashMap<>(WirelessBlockManage.getBlockPosList());
            blockWorldData.setDirty();
        }
        if (worldData != null) {
            worldData.data = new HashMap<>(WirelessData.getDATAMap());
            worldData.setDirty();
        }

        LevelManage.clearBlockEntity();
        WirelessData.clearData();
        WirelessBlockManage.clearBlockPosList();
    }
}
