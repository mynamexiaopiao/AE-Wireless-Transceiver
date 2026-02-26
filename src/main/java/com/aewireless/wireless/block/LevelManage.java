package com.aewireless.wireless.block;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessLink;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Mod.EventBusSubscriber
public class LevelManage {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        tickCounter++;
        if (tickCounter > 10) tickCounter = 0;
        if (tickCounter != 10 )return;

        Level level = event.level;

        if (level.isClientSide) return;

        HashMap<BlockPos, WirelessBlockLink> blockPosList = WirelessBlockManage.getBlockPosList();

        Iterator<Map.Entry<BlockPos, WirelessBlockLink>> iterator = blockPosList.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, WirelessBlockLink> blockPosWirelessBlockLinkEntry = iterator.next();
            BlockPos blockPos = blockPosWirelessBlockLinkEntry.getKey();
            WirelessBlockLink value = blockPosWirelessBlockLinkEntry.getValue();
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null) {
                CompoundTag compoundTag = blockEntity.getPersistentData();
                if (compoundTag.contains("frequency") && compoundTag.contains("uuid")) {
                    String frequency = compoundTag.getString("frequency");
                    UUID uuid = compoundTag.getUUID("uuid");
                    if (!frequency.isEmpty()) {
                        IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(level, blockPos);
                        if (nodeHost != null) {
                            if (value == null){
                                WirelessBlockLink wirelessLink = new WirelessBlockLink(nodeHost , level instanceof ServerLevel sl ? sl : null  , blockPos);
                                wirelessLink.setUuid(uuid);
                                wirelessLink.setFrequency(frequency);
                                WirelessBlockManage.addBlockPos(blockPos , wirelessLink);
                            }else {
                                value.setUuid(uuid);
                                value.setFrequency(frequency);
                                value.update();
                            }
                        }
                    }
                }else {
                    if (value != null){
                        value.destroyConnection();
                    }
                    iterator.remove();
                }
            }
        }

    }
}
