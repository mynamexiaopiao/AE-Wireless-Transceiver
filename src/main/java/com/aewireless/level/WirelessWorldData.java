package com.aewireless.level;

import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;

public class WirelessWorldData extends SavedData {
    Map<String, IWirelessEndpoint> data;

    public WirelessWorldData(Map<String, IWirelessEndpoint> data){
        this.data = data;
    }

    public WirelessWorldData(){

    }
    @Override
    public CompoundTag save(CompoundTag arg) {
        // 保存端点数据
        CompoundTag endpointsTag = new CompoundTag();

        int i = 0;
        for (Map.Entry<String, IWirelessEndpoint> entry : data.entrySet()) {

            endpointsTag.putString("" + i, entry.getKey());

            i++;
        }

        arg.put("wirelessString" , endpointsTag);
        return arg;
    }


    public void loadFromNBT(CompoundTag tag) {
        data.clear();

        if (tag.contains("wirelessString")) {
            CompoundTag endpointsTag = tag.getCompound("wirelessString");

            // 遍历所有保存的键值
            for (String key : endpointsTag.getAllKeys()) {
                try {
                    String frequencyString = endpointsTag.getString(key);
                    if (frequencyString != null && !frequencyString.isEmpty()) {

                        data.put(frequencyString, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static WirelessWorldData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(
                    (tag) -> {
                        WirelessWorldData data = new WirelessWorldData(WirelessData.DATA);
                        data.loadFromNBT(tag);
                        return data;
                    },
                    () -> new WirelessWorldData(WirelessData.DATA),
                    "wireless_world_data"
            );
        }
        return null;
    }
}
