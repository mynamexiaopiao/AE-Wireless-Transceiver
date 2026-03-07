package com.aewireless.level;

import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WirelessWorldData extends SavedData {
    public Map<WirelessData.Key, IWirelessEndpoint> data;

    public WirelessWorldData(Map<WirelessData.Key, IWirelessEndpoint> data) {
        this.data = data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        // 保持原有的存储格式以保持兼容性
        CompoundTag endpointsTag = new CompoundTag();
        CompoundTag uuidTag = new CompoundTag();

        int i = 0;
        for (Map.Entry<WirelessData.Key, IWirelessEndpoint> entry : data.entrySet()) {
            endpointsTag.putString("string" + i, entry.getKey().string());
            uuidTag.putUUID("uuid" + i, entry.getKey().uuid());
            i++;
        }

        tag.put("wirelessString", endpointsTag);
        tag.put("wirelessUUID", uuidTag);

        // 同时保存新格式，方便将来迁移
        CompoundTag newFormatTag = new CompoundTag();
        i = 0;
        for (Map.Entry<WirelessData.Key, IWirelessEndpoint> entry : data.entrySet()) {
            CompoundTag endpointTag = new CompoundTag();
            endpointTag.putString("frequency", entry.getKey().string());
            endpointTag.putUUID("uuid", entry.getKey().uuid());
            newFormatTag.put(String.valueOf(i), endpointTag);
            i++;
        }
        tag.put("wireless_endpoints_v2", newFormatTag);

        return tag;
    }

    public void loadFromNBT(CompoundTag tag) {
        data.clear();

        // 先尝试加载新格式
        if (tag.contains("wireless_endpoints_v2")) {
            loadNewFormat(tag.getCompound("wireless_endpoints_v2"));
        }
        // 否则加载旧格式
        else if (tag.contains("wirelessString") && tag.contains("wirelessUUID")) {
            loadOldFormat(tag);
        }
    }

    private void loadNewFormat(CompoundTag endpointsTag) {
        for (String key : endpointsTag.getAllKeys()) {
            try {
                CompoundTag endpointTag = endpointsTag.getCompound(key);
                String frequencyString = endpointTag.getString("frequency");
                UUID uuid = endpointTag.getUUID("uuid");
                data.put(new WirelessData.Key(frequencyString, uuid), null);
            } catch (Exception e) {
                throw new RuntimeException("Error loading wireless endpoint data from NBT (new format)", e);
            }
        }
    }

    private void loadOldFormat(CompoundTag tag) {
        CompoundTag endpointsTag = tag.getCompound("wirelessString");
        CompoundTag uuidTag = tag.getCompound("wirelessUUID");

        // 获取所有键并按数字排序以确保正确的对应关系
        List<String> stringKeys = new ArrayList<>(endpointsTag.getAllKeys());
        List<String> uuidKeys = new ArrayList<>(uuidTag.getAllKeys());

        // 自定义排序，确保按索引顺序
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

        // 确保两个列表大小相同
        int size = Math.min(stringKeys.size(), uuidKeys.size());
        for (int i = 0; i < size; i++) {
            String frequencyString = endpointsTag.getString(stringKeys.get(i));
            UUID uuid = uuidTag.getUUID(uuidKeys.get(i));
            data.put(new WirelessData.Key(frequencyString, uuid), null);
        }
    }

    public static WirelessWorldData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(
                    (tag) -> {
                        WirelessWorldData worldData = new WirelessWorldData(new HashMap<>());
                        worldData.loadFromNBT(tag);
                        return worldData;
                    },
                    () -> new WirelessWorldData(new HashMap<>()),
                    "wireless_world_data"
            );
        }
        return null;
    }

    public Map<WirelessData.Key, IWirelessEndpoint> getData() {
        return data;
    }

    public void setData(Map<WirelessData.Key, IWirelessEndpoint> newData) {
        this.data = newData;
        this.setDirty();
    }
}