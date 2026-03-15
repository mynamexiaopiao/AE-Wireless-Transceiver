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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


@EventBusSubscriber
public class LevelManage {

    public static CopyOnWriteArrayList<WirelessBlockLink> blockPosList1 = new CopyOnWriteArrayList<>();
    private static HashMap<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosList;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        for (WirelessBlockLink wirelessBlockLink : blockPosList1) {
            wirelessBlockLink.update();
        }

        blockPosList1.clear();
    }


    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();

        if (level.isClientSide) return;

        // 只有在 blockPosList 为 null 时才加载
        if (blockPosList == null) {
            blockPosList = WirelessBlockManage.getBlockPosList();
        }

        // 仅在必要时刷新 blockPosList
        if (WirelessBlockManage.isDirty()) {
            blockPosList = WirelessBlockManage.getBlockPosList();
            WirelessBlockManage.setUndirty();
        }

        for (Map.Entry<WirelessBlockManage.PosAndDirection, WirelessBlockLink> entry : blockPosList.entrySet()) {
            WirelessBlockManage.PosAndDirection posAndDirection = entry.getKey();
            BlockPos blockPos = posAndDirection.pos();
            Direction direction = posAndDirection.direction();
            WirelessBlockLink wirelessBlockLink = entry.getValue();

            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            IInWorldGridNodeHost nodeHost = null;

            if (wirelessBlockLink != null) {
                if (wirelessBlockLink.getHostNode() == null) {
                    nodeHost = GridHelper.getNodeHost(level , blockPos);
                    if (nodeHost != null) {
                        wirelessBlockLink.setHostNode(nodeHost.getGridNode(direction));
                    }
                }
                blockPosList1.add(wirelessBlockLink);
                continue;
            }

            nodeHost = GridHelper.getNodeHost(level , blockPos);

            if (blockEntity != null) {
                CompoundTag compoundTag = blockEntity.getPersistentData();
                if (compoundTag.contains("frequency") && compoundTag.contains("uuid") && compoundTag.contains("direction")) {
                    String frequency = compoundTag.getString("frequency");
                    UUID uuid = compoundTag.getUUID("uuid");
                    if (!frequency.isEmpty()) {
                        if (nodeHost != null) {
                            if (wirelessBlockLink == null) {
                                ServerLevel serverLevel = level instanceof ServerLevel ? (ServerLevel) level : null;

                                if (isPart(level, blockPos)) {
                                    WirelessPartLink wirelessPartLink = new WirelessPartLink(serverLevel, blockPos);
                                    wirelessPartLink.setUuid(uuid);
                                    wirelessPartLink.setFrequency(frequency);
                                    WirelessBlockManage.addBlockPos(posAndDirection, wirelessPartLink);
                                } else {
                                    IGridNode gridNode = nodeHost.getGridNode(direction);
                                    if (gridNode == null) continue;

                                    WirelessBlockLink newWirelessBlockLink = new WirelessBlockLink(gridNode, serverLevel, blockPos);
                                    newWirelessBlockLink.setUuid(uuid);
                                    newWirelessBlockLink.setFrequency(frequency);
                                    WirelessBlockManage.addBlockPos(posAndDirection, newWirelessBlockLink);
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