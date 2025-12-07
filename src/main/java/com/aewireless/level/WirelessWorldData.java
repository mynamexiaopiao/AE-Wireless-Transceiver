package com.aewireless.level;

import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class WirelessWorldData extends SavedData {
    Map<WirelessData.Key, IWirelessEndpoint> data;

    public WirelessWorldData(Map<WirelessData.Key, IWirelessEndpoint> data){
        this.data = data;
    }


    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag arg) {
        // 保存端点数据
        CompoundTag endpointsTag = new CompoundTag();

        CompoundTag uuidTag = new CompoundTag();


        int i = 0;
        for (Map.Entry<WirelessData.Key, IWirelessEndpoint> entry : data.entrySet()) {

            endpointsTag.putString("string" + i, entry.getKey().string());
            uuidTag.putUUID("uuid" + i, entry.getKey().uuid());

            i++;
        }

        arg.put("wirelessString" , endpointsTag);
        arg.put("wirelessUUID" , uuidTag);
        return arg;
    }


    public void loadFromNBT(CompoundTag tag) {
        data.clear();

        if (tag.contains("wirelessString") && tag.contains("wirelessUUID")) {
            CompoundTag endpointsTag = tag.getCompound("wirelessString");
            CompoundTag uuidTag = tag.getCompound("wirelessUUID");

            ArrayList<String> strings = new ArrayList<>();


            // 遍历所有保存的键值
            for (String key : endpointsTag.getAllKeys()) {
                try {
                    String frequencyString = endpointsTag.getString(key);
                    if (!frequencyString.isEmpty()) {

                        strings.add(frequencyString);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error loading wireless endpoint data from NBT");
                }
            }

            for (int i = 0 ; i<uuidTag.size() ; i ++){
                UUID uuid = uuidTag.getUUID(uuidTag.getAllKeys().toArray()[i].toString());
                data.put(new WirelessData.Key(strings.get(i), uuid), null);
            }

        }
    }

    public static WirelessWorldData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(
                    (tag) -> {
                        WirelessWorldData data = new WirelessWorldData(WirelessData.getDATAMap());
                        data.loadFromNBT(tag);
                        return data;
                    },
                    () -> new WirelessWorldData(WirelessData.getDATAMap()),
                    "wireless_world_data"
            );
        }
        return null;
    }
}
