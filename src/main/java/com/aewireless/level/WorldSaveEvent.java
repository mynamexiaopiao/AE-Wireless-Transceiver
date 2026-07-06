package com.aewireless.level;


import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.block.link.JoinWorldWireless;
import com.aewireless.wireless.block.link.WirelessBlockLinkManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.HashMap;
import java.util.Objects;

@EventBusSubscriber(modid = AeWireless.MOD_ID)
public class WorldSaveEvent {


    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            WirelessWorldData worldData = WirelessWorldData.get(Objects.requireNonNull(event.getLevel().getServer()).getLevel(Level.OVERWORLD));

            if (worldData != null){
                WirelessData.setDATAMap(worldData.data);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (WirelessBlockLinkManager.hasWirelessData(blockEntity)) {
                JoinWorldWireless.add(level, blockEntity.getBlockPos());
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            WirelessBlockLinkManager.clear(blockEntity);
        }
    }


    @SubscribeEvent
    public static void onServerStopped(ServerStoppingEvent event) {
        WirelessWorldData worldData = WirelessWorldData.get(event.getServer().getLevel(Level.OVERWORLD));

        if (worldData != null) {
            worldData.data = new HashMap<>(WirelessData.getDATAMap());
            worldData.setDirty();
        }

        WirelessData.clearData();
    }
}
