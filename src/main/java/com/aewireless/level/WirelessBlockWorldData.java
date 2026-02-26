package com.aewireless.level;

import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.block.WirelessBlockLink;
import com.aewireless.wireless.block.WirelessBlockManage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WirelessBlockWorldData extends SavedData {
    HashMap<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosList;

    public WirelessBlockWorldData(HashMap<WirelessBlockManage.PosAndDirection, WirelessBlockLink> pos){
        blockPosList = pos;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {

        ListTag list = new ListTag();

        for (var entry : blockPosList.entrySet()) {

            CompoundTag element = new CompoundTag();

            element.putLong("pos", entry.getKey().pos().asLong());
            element.putInt("dir", entry.getKey().direction().ordinal());

            list.add(element);
        }

        tag.put("entries", list);

        return tag;
    }

    /* ===================== 读取 ===================== */

    public void loadFromNBT(CompoundTag tag) {

        if (tag.contains("entries", Tag.TAG_LIST)) {

            ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);

            for (int i = 0; i < list.size(); i++) {

                CompoundTag element = list.getCompound(i);

                BlockPos pos = BlockPos.of(element.getLong("pos"));
                Direction dir = Direction.values()[element.getInt("dir")];

                WirelessBlockManage.PosAndDirection key =
                        new WirelessBlockManage.PosAndDirection(pos, dir);

                blockPosList.put(key, null);
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
