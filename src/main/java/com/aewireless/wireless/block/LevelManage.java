package com.aewireless.wireless.block;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IInWorldGridNodeHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModWorkManager;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;


@Mod.EventBusSubscriber
public class LevelManage {
    private static int tickCounter = 0;

    private static int TICK_INTERVAL = 20;

    public static CopyOnWriteArrayList<WirelessBlockLink> blockPosList1 = new CopyOnWriteArrayList<>();



    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        tickCounter ++;
        if (tickCounter > TICK_INTERVAL) tickCounter = 0;
        if (tickCounter != TICK_INTERVAL)return;

        for (WirelessBlockLink wirelessBlockLink : blockPosList1) {
            wirelessBlockLink.update();
        }

        blockPosList1.clear();
    }


    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {

        if (event.level.getGameTime() % TICK_INTERVAL != 0) return;

        Level level = event.level;

        if (level.isClientSide) return;

        HashMap<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosList = WirelessBlockManage.getBlockPosList();


        for (Map.Entry<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosWirelessBlockLinkEntry : blockPosList.entrySet()) {
            BlockPos blockPos = blockPosWirelessBlockLinkEntry.getKey().pos();
            Direction direction = blockPosWirelessBlockLinkEntry.getKey().direction();
            WirelessBlockLink value = blockPosWirelessBlockLinkEntry.getValue();
            WirelessBlockManage.PosAndDirection posAndDirection = new WirelessBlockManage.PosAndDirection(blockPos, direction);
            IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(level, blockPos);


            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockPosList.get(posAndDirection) != null) {
                if (value.getHost() == null || value.getHost().getGridNode(direction) == null) {
                    value.setHost(nodeHost);
                }
                blockPosList1.add(value);

                continue;
            };

            if (blockEntity != null) {
                CompoundTag compoundTag = blockEntity.getPersistentData();
                if (compoundTag.contains("frequency") && compoundTag.contains("uuid") && compoundTag.contains("direction")) {
                    String frequency = compoundTag.getString("frequency");
                    UUID uuid = compoundTag.getUUID("uuid");
                    if (!frequency.isEmpty()) {
                        if (nodeHost != null) {
                            if (value == null){
                                WirelessBlockLink wirelessLink = new WirelessBlockLink(nodeHost , level instanceof ServerLevel sl ? sl : null  , blockPos);
                                wirelessLink.setUuid(uuid);
                                wirelessLink.setFrequency(frequency);
                                wirelessLink.setDirection(direction);
                                WirelessBlockManage.addBlockPos(posAndDirection , wirelessLink);
                            }
                        }
                    }
                }
            }
        }
    }
}