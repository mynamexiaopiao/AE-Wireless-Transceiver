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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


@EventBusSubscriber
public class LevelManage {

    public static List<WirelessBlockLink> blockPosList1 = new ArrayList<>();
    private static Map<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosList;
    private static Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        // 只有在 blockPosList 为 null 时才加载
        if (blockPosList == null) {
            blockPosList = WirelessBlockManage.getBlockPosList();
        }


        // 仅在必要时刷新 blockPosList
        if (WirelessBlockManage.isDirty()) {
            blockPosList = WirelessBlockManage.getBlockPosList();
            WirelessBlockManage.setUndirty();
        }

        MinecraftServer server = event.getServer();
        Iterable<ServerLevel> allLevels = server.getAllLevels();
        if (blockEntities.isEmpty() && !blockPosList.isEmpty()) {
            for (ServerLevel level : allLevels) {
                blockPosList.entrySet().forEach(entry -> {
                    BlockPos pos = entry.getKey().pos();
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity != null) {
                        blockEntities.put(pos, blockEntity);
                    }
                });
            }
        }

        // 仅更新必要的 blockPosList1
        for (WirelessBlockLink wirelessBlockLink : blockPosList1) {
            wirelessBlockLink.update();
        }

        blockPosList1.clear();
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();

        if (level.isClientSide) return;

        if (blockPosList == null) return;



        for (Map.Entry<WirelessBlockManage.PosAndDirection, WirelessBlockLink> entry : new HashMap<>(blockPosList).entrySet()) {
            WirelessBlockManage.PosAndDirection posAndDirection = entry.getKey();
            BlockPos blockPos = posAndDirection.pos();
            Direction direction = posAndDirection.direction();
            WirelessBlockLink wirelessBlockLink = entry.getValue();

            BlockEntity blockEntity = getBlockEntity(level, blockPos);

            IInWorldGridNodeHost nodeHost = null;

            if (wirelessBlockLink != null) {
                if (wirelessBlockLink.getHostNode() == null) {
                    nodeHost = getNodeHost(level, blockPos);
                    if (nodeHost != null) {
                        wirelessBlockLink.setHostNode(nodeHost.getGridNode(direction));
                    }
                }
                blockPosList1.add(wirelessBlockLink);
                continue;
            }

            nodeHost = getNodeHost(level, blockPos);

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
                                    // 这里可能会修改 blockPosList，但我们在遍历副本，所以安全
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
    public static BlockEntity getBlockEntity(Level level, BlockPos pos) {
        // 先从缓存获取
        BlockEntity be = blockEntities.get(pos);

        // 如果缓存有效且位置相同，直接返回
        if (be != null && !be.isRemoved() && be.getBlockPos().equals(pos)) {
            return be;
        }

        // 缓存失效，从世界获取
        be = level.getBlockEntity(pos);


        // 更新缓存
        if (be != null && !be.isRemoved()) {
            blockEntities.put(pos.immutable(), be);
        } else {
            blockEntities.remove(pos);
        }

        return be;
    }

    public static void addBlockEntityList(BlockPos pos, BlockEntity blockEntity) {
        blockEntities.put(pos, blockEntity);
    }

    public static void removeBlockEntity(BlockPos pos) {
        blockEntities.remove(pos);
    }

    public static void clearBlockEntity(){
        blockEntities.clear();
    }

    private static IInWorldGridNodeHost getNodeHost(Level level ,BlockPos pos) {
        return GridHelper.getNodeHost(level, pos);
    }

    private static boolean isPart(Level level, BlockPos pos) {
        // 仅检查方向上有部件的情况，减少重复检查
        for (Direction direction : Direction.values()) {
            IPart part = PartHelper.getPart(level, pos, direction);
            if (part != null) return true;
        }
        return false;
    }
}