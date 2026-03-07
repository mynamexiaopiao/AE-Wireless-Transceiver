package com.aewireless.wireless.block;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import com.aewireless.wireless.block.link.WirelessBlockLink;
import com.aewireless.wireless.block.link.WirelessPartLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


@Mod.EventBusSubscriber
public class LevelManage {

    public static CopyOnWriteArrayList<WirelessBlockLink> blockPosList1 = new CopyOnWriteArrayList<>();
    private static HashMap<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosList;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;


        for (WirelessBlockLink wirelessBlockLink : blockPosList1) {
            wirelessBlockLink.update();
        }

        blockPosList1.clear();
    }


    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Level level = event.level;

        if (level.isClientSide) return;

        if (blockPosList == null){
            blockPosList = WirelessBlockManage.getBlockPosList();
        }

        if (WirelessBlockManage.isDirty()){
            blockPosList = WirelessBlockManage.getBlockPosList();
            WirelessBlockManage.setUndirty();
        }

        for (Map.Entry<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosWirelessBlockLinkEntry : blockPosList.entrySet()) {
            BlockPos blockPos = blockPosWirelessBlockLinkEntry.getKey().pos();
            Direction direction = blockPosWirelessBlockLinkEntry.getKey().direction();
            WirelessBlockLink value = blockPosWirelessBlockLinkEntry.getValue();
            WirelessBlockManage.PosAndDirection posAndDirection = blockPosWirelessBlockLinkEntry.getKey();
            IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(level, blockPos);

            if (value != null) {
                if (value.getHostNode() == null && nodeHost != null) {
                    value.setHostNode(nodeHost.getGridNode(direction));
                }
                blockPosList1.add(value);
                continue;
            };



            BlockEntity blockEntity = level.getBlockEntity(blockPos);


            if (blockEntity != null) {
                CompoundTag compoundTag = blockEntity.getPersistentData();
                if (compoundTag.contains("frequency") && compoundTag.contains("uuid") && compoundTag.contains("direction")) {
                    String frequency = compoundTag.getString("frequency");
                    UUID uuid = compoundTag.getUUID("uuid");
                    if (!frequency.isEmpty()) {
                        if (nodeHost != null) {
                            if (value == null){
                                ServerLevel serverLevel = level instanceof ServerLevel sl ? sl : null;

                                if (isPart(level, blockPos)) {
                                    WirelessPartLink wirelessPartLink = new WirelessPartLink(serverLevel, blockPos);
                                    wirelessPartLink.setUuid(uuid);
                                    wirelessPartLink.setFrequency(frequency);
                                    WirelessBlockManage.addBlockPos(posAndDirection , wirelessPartLink);
                                }else {
                                    IGridNode gridNode = nodeHost.getGridNode(direction);

                                    if (gridNode == null)  continue;

                                    WirelessBlockLink wirelessLink = new WirelessBlockLink(gridNode , serverLevel, blockPos);
                                    wirelessLink.setUuid(uuid);
                                    wirelessLink.setFrequency(frequency);
                                    WirelessBlockManage.addBlockPos(posAndDirection , wirelessLink);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isPart(Level level , BlockPos pos){
        for (Direction value : Direction.values()) {
            IPart part = PartHelper.getPart(level, pos, value);
            if (part != null) return true;
        }
        return false;
    }


}