package com.aewireless.level;


import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.block.LevelManage;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = AeWireless.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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


        WirelessData.clearData();
        WirelessBlockManage.clearBlockPosList();
        LevelManage.clearBlockEntity();
    }
}
