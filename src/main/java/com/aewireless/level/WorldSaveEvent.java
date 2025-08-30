package com.aewireless.level;


import com.aewireless.AeWireless;
import com.aewireless.wireless.WirelessData;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AeWireless.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
