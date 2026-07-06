package com.aewireless.wireless.block.link;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.aewireless.block.WirelessConnectBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

public final class WirelessBlockLinkManager {
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_DIRECTION = "direction";

    private static final Map<ServerLevel, Map<BlockPos, Entry>> ENTRIES = new WeakHashMap<>();

    private WirelessBlockLinkManager() {
    }

    public static boolean update(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) return true;

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (blockEntity == null) {
            clear(serverLevel, pos);
            return true;
        }

        if (blockEntity instanceof CableBusBlockEntity cableBus) {
            return updatePart(cableBus);
        }
        return updateHost(blockEntity);
    }

    public static boolean updateHost(BlockEntity blockEntity) {
        if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) return true;
        if (blockEntity instanceof WirelessConnectBlockEntity) return true;

        WirelessData data = readWirelessData(blockEntity);
        if (data == null) {
            clear(serverLevel, blockEntity.getBlockPos());
            return true;
        }

        Entry entry = getEntry(serverLevel, blockEntity.getBlockPos());
        WirelessBlockLink link = entry.link;
        if (link == null || link instanceof WirelessPartLink || !entry.matches(data)) {
            entry.destroy();
            IGridNode node = resolveHostNode(blockEntity, data.direction());
            if (node == null) return false;
            link = new WirelessBlockLink(node, serverLevel, blockEntity.getBlockPos());
            configureLink(link, data);
            entry.link = link;
            entry.remember(data);
        } else {
            ensureHostNode(blockEntity, data, link);
        }

        link.update();
        return link.isConnected();
    }

    public static boolean updatePart(CableBusBlockEntity cableBus) {
        if (!(cableBus.getLevel() instanceof ServerLevel serverLevel)) return true;

        WirelessData data = readWirelessData(cableBus);
        if (data == null) {
            clear(serverLevel, cableBus.getBlockPos());
            return true;
        }

        Entry entry = getEntry(serverLevel, cableBus.getBlockPos());
        WirelessBlockLink link = entry.link;
        if (!(link instanceof WirelessPartLink) || !entry.matches(data)) {
            entry.destroy();
            link = new WirelessPartLink(serverLevel, cableBus.getBlockPos());
            configureLink(link, data);
            entry.link = link;
            entry.remember(data);
        }

        link.update();
        if (link instanceof WirelessPartLink partLink) {
            return partLink.isConnected();
        }
        return link.isConnected();
    }

    public static void updateWireless(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) return;
        update(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static void clear(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) return;
        Map<BlockPos, Entry> entries = ENTRIES.get(serverLevel);
        if (entries == null) return;

        Entry entry = entries.remove(pos.immutable());
        if (entry != null) {
            entry.destroy();
        }
        if (entries.isEmpty()) {
            ENTRIES.remove(serverLevel);
        }
    }

    public static void clear(BlockEntity blockEntity) {
        if (blockEntity == null) return;
        clear(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    @Nullable
    public static WirelessBlockLink getLink(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) return null;
        Map<BlockPos, Entry> entries = ENTRIES.get(serverLevel);
        if (entries == null) return null;
        Entry entry = entries.get(pos);
        return entry == null ? null : entry.link;
    }

    @Nullable
    public static WirelessBlockLink getLink(BlockEntity blockEntity) {
        if (blockEntity == null) return null;
        return getLink(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static boolean hasWirelessData(BlockEntity blockEntity) {
        return readWirelessData(blockEntity) != null;
    }

    @Nullable
    public static WirelessData readWirelessData(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity instanceof WirelessConnectBlockEntity) return null;

        CompoundTag data = blockEntity.getPersistentData();
        if (!data.contains(KEY_FREQUENCY) || !data.contains(KEY_UUID) || !data.contains(KEY_DIRECTION)) {
            return null;
        }

        String frequency = data.getString(KEY_FREQUENCY);
        if (frequency.isEmpty()) return null;

        int dirIndex = data.getInt(KEY_DIRECTION);
        Direction[] dirs = Direction.values();
        if (dirIndex < 0 || dirIndex >= dirs.length) return null;

        UUID uuid = data.getUUID(KEY_UUID);
        return new WirelessData(frequency, uuid, dirs[dirIndex]);
    }

    private static Entry getEntry(ServerLevel level, BlockPos pos) {
        return ENTRIES.computeIfAbsent(level, ignored -> new HashMap<>())
                .computeIfAbsent(pos.immutable(), ignored -> new Entry());
    }

    private static void configureLink(WirelessBlockLink link, WirelessData data) {
        link.setUuid(data.uuid());
        link.setFrequency(data.frequency());
    }

    private static void ensureHostNode(BlockEntity blockEntity, WirelessData data, WirelessBlockLink link) {
        if (link.getHostNode() != null) return;

        IGridNode node = resolveHostNode(blockEntity, data.direction());
        if (node != null) {
            link.setHostNode(node);
        }
    }

    @Nullable
    private static IGridNode resolveHostNode(BlockEntity blockEntity, Direction direction) {
        IInWorldGridNodeHost host = getNodeHost(blockEntity);
        return host != null ? host.getGridNode(direction) : null;
    }

    @Nullable
    private static IInWorldGridNodeHost getNodeHost(BlockEntity blockEntity) {
        if (blockEntity instanceof IInWorldGridNodeHost host) return host;
        Level level = blockEntity.getLevel();
        if (level == null) return null;
        return GridHelper.getNodeHost(level, blockEntity.getBlockPos());
    }

    private static final class Entry {
        private WirelessBlockLink link;
        private String frequency;
        private UUID uuid;
        private Direction direction;

        private boolean matches(WirelessData data) {
            return Objects.equals(frequency, data.frequency())
                    && Objects.equals(uuid, data.uuid())
                    && direction == data.direction();
        }

        private void remember(WirelessData data) {
            frequency = data.frequency();
            uuid = data.uuid();
            direction = data.direction();
        }

        private void destroy() {
            if (link != null) {
                link.destroyConnection();
                link = null;
            }
            frequency = null;
            uuid = null;
            direction = null;
        }
    }
}
