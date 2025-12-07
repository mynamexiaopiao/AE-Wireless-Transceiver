package com.aewireless.level;

import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class WirelessWorldData extends SavedData {
    Map<WirelessData.Key, IWirelessEndpoint> data;

    public WirelessWorldData(Map<WirelessData.Key, IWirelessEndpoint> data){
        this.data = data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // 保存端点数据
        ListTag endpointsList = new ListTag();

        for (Map.Entry<WirelessData.Key, IWirelessEndpoint> entry : data.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("string", entry.getKey().string());
            entryTag.putUUID("uuid", entry.getKey().uuid());
            endpointsList.add(entryTag);
        }

        tag.put("endpoints", endpointsList);
        return tag;
    }

    public void loadFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        data.clear();

        if (tag.contains("endpoints", Tag.TAG_LIST)) {
            ListTag endpointsList = tag.getList("endpoints", Tag.TAG_COMPOUND);

            for (int i = 0; i < endpointsList.size(); i++) {
                CompoundTag entryTag = endpointsList.getCompound(i);
                try {
                    String frequencyString = entryTag.getString("string");
                    UUID uuid = entryTag.getUUID("uuid");

                    if (!frequencyString.isEmpty()) {
                        data.put(new WirelessData.Key(frequencyString, uuid), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static WirelessWorldData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            DimensionDataStorage storage = serverLevel.getDataStorage();
            return storage.computeIfAbsent(
                    new SavedData.Factory<>(
                            () -> new WirelessWorldData(WirelessData.getDATAMap()),
                            (tag, provider) -> {
                                WirelessWorldData data = new WirelessWorldData(WirelessData.getDATAMap());
                                data.loadFromNBT(tag, provider);
                                return data;
                            }
                    ),
                    "wireless_world_data"
            );
        }
        return null;
    }
}
