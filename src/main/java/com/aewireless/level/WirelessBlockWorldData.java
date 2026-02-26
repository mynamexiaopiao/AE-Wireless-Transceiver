package com.aewireless.level;

import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.block.WirelessBlockLink;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WirelessBlockWorldData extends SavedData {
    HashMap<BlockPos, WirelessBlockLink> blockPosList;

    public WirelessBlockWorldData(HashMap<BlockPos, WirelessBlockLink> pos){
        blockPosList = pos;
    }

    @Override
    public CompoundTag save(CompoundTag arg) {

        CompoundTag posTag = new CompoundTag();

        int i = 0;

        for (Map.Entry<BlockPos, WirelessBlockLink> blockPosWirelessBlockLinkEntry : blockPosList.entrySet()) {
            BlockPos blockPos = blockPosWirelessBlockLinkEntry.getKey();
            posTag.putLong("pos"+i ,blockPos.asLong());
            i++;
        }

        arg.put("blockPosList" , posTag);
        return arg;
    }

    public void loadFromNBT(CompoundTag tag) {
        blockPosList.clear();

        if (tag.contains("blockPosList")) {

            CompoundTag posTag = tag.getCompound("blockPosList");

            for (String pos : posTag.getAllKeys()) {
                long aLong = posTag.getLong(pos);
                BlockPos blockPos = BlockPos.of(aLong);
                blockPosList.put(blockPos , null);
            }
        }
    }

    public static WirelessBlockWorldData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(
                    (tag) -> {
                        WirelessBlockWorldData data = new WirelessBlockWorldData(WirelessBlockManage.getBlockPosList());
                        data.loadFromNBT(tag);
                        return data;
                    },
                    () -> new WirelessBlockWorldData(WirelessBlockManage.getBlockPosList()),
                    "wireless_block_world_data"
            );
        }
        return null;
    }
}
