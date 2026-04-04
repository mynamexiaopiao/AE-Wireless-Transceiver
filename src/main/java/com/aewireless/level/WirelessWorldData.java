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
            loadListFormat(tag.getList("endpoints", Tag.TAG_COMPOUND));
            return;
        }

        if (tag.contains("wireless_endpoints_v2")) {
            loadV2Format(tag.getCompound("wireless_endpoints_v2"));
            return;
        }

        if (tag.contains("wirelessString") && tag.contains("wirelessUUID")) {
            loadLegacyFormat(tag);
        }
    }

    private void loadListFormat(ListTag endpointsList) {
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

    private void loadV2Format(CompoundTag endpointsTag) {
        for (String key : endpointsTag.getAllKeys()) {
            try {
                CompoundTag endpointTag = endpointsTag.getCompound(key);
                String frequencyString = endpointTag.getString("frequency");
                UUID uuid = endpointTag.getUUID("uuid");
                if (!frequencyString.isEmpty()) {
                    data.put(new WirelessData.Key(frequencyString, uuid), null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLegacyFormat(CompoundTag tag) {
        CompoundTag endpointsTag = tag.getCompound("wirelessString");
        CompoundTag uuidTag = tag.getCompound("wirelessUUID");

        var stringKeys = new ArrayList<>(endpointsTag.getAllKeys());
        var uuidKeys = new ArrayList<>(uuidTag.getAllKeys());

        stringKeys.sort((a, b) -> {
            int numA = Integer.parseInt(a.replace("string", ""));
            int numB = Integer.parseInt(b.replace("string", ""));
            return Integer.compare(numA, numB);
        });

        uuidKeys.sort((a, b) -> {
            int numA = Integer.parseInt(a.replace("uuid", ""));
            int numB = Integer.parseInt(b.replace("uuid", ""));
            return Integer.compare(numA, numB);
        });

        int size = Math.min(stringKeys.size(), uuidKeys.size());
        for (int i = 0; i < size; i++) {
            String frequencyString = endpointsTag.getString(stringKeys.get(i));
            UUID uuid = uuidTag.getUUID(uuidKeys.get(i));
            if (!frequencyString.isEmpty()) {
                data.put(new WirelessData.Key(frequencyString, uuid), null);
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
