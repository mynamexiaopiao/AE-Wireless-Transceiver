package com.aewireless.wireless.block;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.capabilities.Capabilities;
import com.aewireless.AeWirelessConfig;
import com.aewireless.wireless.block.link.WirelessBlockLink;
import com.aewireless.wireless.block.link.WirelessPartLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
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

    public static List<WirelessBlockLink> blockPosList1 = new ArrayList<>();
    private static Map<WirelessBlockManage.PosAndDirection, WirelessBlockLink> blockPosList;
    private static Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
    private static boolean isLoad = true;
    private static boolean isCacheLoad ;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!isLoad)return;

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
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!isLoad)return;

        Level level = event.level;
        if (level.isClientSide) return;

        if (blockPosList == null) return;



        for (Map.Entry<WirelessBlockManage.PosAndDirection, WirelessBlockLink> entry : new HashMap<>(blockPosList).entrySet()) {
            WirelessBlockManage.PosAndDirection posAndDirection = entry.getKey();
            BlockPos blockPos = posAndDirection.pos();
            Direction direction = posAndDirection.direction();
            WirelessBlockLink wirelessBlockLink = entry.getValue();

            isCacheLoad = AeWirelessConfig.INSTANCE.isCacheLoad;
            BlockEntity blockEntity;
            if (isCacheLoad){
                blockEntity = getBlockEntity(blockPos);
            }else {
                blockEntity = getBlockEntity(level,blockPos);
            }

            IInWorldGridNodeHost nodeHost = null;

            if (wirelessBlockLink != null) {
                if (wirelessBlockLink.getHostNode() == null) {
                    nodeHost = getNodeHost(blockEntity);
                    if (nodeHost != null) {
                        wirelessBlockLink.setHostNode(nodeHost.getGridNode(direction));
                    }
                }
                blockPosList1.add(wirelessBlockLink);
                continue;
            }

            nodeHost = getNodeHost(blockEntity);

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
    public static BlockEntity getBlockEntity(BlockPos pos) {
        // 先从缓存获取
        return blockEntities.get(pos);
    }

    public static BlockEntity getBlockEntity(Level level, BlockPos pos) {
        return level.getBlockEntity(pos);
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

    private static IInWorldGridNodeHost getNodeHost(BlockEntity blockEntity) {
        if (blockEntity instanceof IInWorldGridNodeHost host) {
            return host;
        }
        return blockEntity != null ? blockEntity.getCapability(Capabilities.IN_WORLD_GRID_NODE_HOST).orElse(null) : null;
    }

    public static void setIsLoad(boolean isLoad) {
        LevelManage.isLoad = isLoad;
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