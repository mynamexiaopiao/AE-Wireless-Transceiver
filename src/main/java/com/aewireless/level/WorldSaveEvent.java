package com.aewireless.level;

import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber
public class WorldSaveEvent {
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            Level level = (Level) event.getLevel();
            WirelessData.DATA.clear();

            WirelessWorldData worldData = WirelessWorldData.get(level);

            WirelessData.DATA = worldData.data;
        }
    }

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (!event.getLevel().isClientSide()) {
            Level level = (Level) event.getLevel();
            WirelessWorldData worldData = WirelessWorldData.get(level);
            if (worldData != null) {
                worldData.setDirty();
            }
        }
    }
}
