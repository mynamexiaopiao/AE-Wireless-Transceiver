package com.aewireless.block;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import com.aewireless.AeWireless;
import com.aewireless.ModConfig;
import com.aewireless.gui.wireless.WirelessMenu;
import com.aewireless.register.ModRegister;
import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.IWirelessMasterEndpoint;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessLink;
import com.aewireless.wireless.WirelessMasterLink;
import com.aewireless.wireless.WirelessTeamUtil;
import com.aewireless.wireless.block.link.JoinWorldWireless;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class WirelessConnectBlockEntity extends BlockEntity implements MenuProvider, IInWorldGridNodeHost, IWirelessEndpoint, IWirelessMasterEndpoint {
    private final IManagedGridNode managedNode;
    protected final ContainerData data;

    private final WirelessMasterLink masterLink;
    private final WirelessLink slaveLink;
    private final Set<SlaveRef> slaveRefs = new LinkedHashSet<>();

    private String frequency = null;
    private UUID placerId;
    private String placerName;
    private boolean mode = false;

    int usedChannels = 0;
    int maxChannels = 0;

    public WirelessConnectBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegister.WIRELESS_TRANSCEIVER_ENTITY.get(), pos, blockState);

        this.managedNode = GridHelper.createManagedNode(this, (nodeOwner, node) -> nodeOwner.setChanged())
                .setFlags(GridFlags.DENSE_CAPACITY);
        this.managedNode.setVisualRepresentation(ModRegister.WIRELESS_TRANSCEIVER.get());
        this.managedNode.setTagName("wireless_connect");
        this.managedNode.setInWorldNode(true);
        this.managedNode.setExposedOnSides(EnumSet.allOf(Direction.class));

        masterLink = new WirelessMasterLink(this);
        slaveLink = new WirelessLink(this);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                if (i == 0) {
                    return managedNode.isOnline() ? 1 : 0;
                } else if (i == 1) {
                    return usedChannels;
                } else if (i == 2) {
                    return maxChannels;
                }
                return 0;
            }

            @Override
            public void set(int i, int j) {
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public String getFrequency() {
        return frequency;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        if (this.level == null) return AECableType.GLASS;
        BlockPos relative = this.worldPosition.relative(dir);
        if (!Objects.requireNonNull(this.getLevel()).hasChunkAt(relative)) return AECableType.GLASS;
        IInWorldGridNodeHost nodeHost = GridHelper.getNodeHost(level, relative);
        if (nodeHost != null) {
            AECableType cableType = nodeHost.getCableConnectionType(dir.getOpposite());
            if (cableType != null) return cableType;
        }
        return AECableType.GLASS;
    }

    public void setMasterMode(boolean masterMode) {
        if (this.mode == masterMode) return;

        if (this.mode) {
            masterLink.unregister();
        } else {
            slaveLink.destroyConnection();
        }

        this.mode = masterMode;

        if (this.mode) {
            masterLink.setFrequency(frequency, placerId);
        } else {
            slaveLink.setFrequency(frequency);
        }

        this.frequency = null;
        if (this.managedNode != null) {
            this.managedNode.setIdlePowerUsage(getEnergy());
        }
        setChanged();
    }

    public void setFrequency(String frequency) {
        if (Objects.equals(frequency, this.frequency)) return;
        this.frequency = frequency;
        usedChannels = 0;

        if (isMode()) {
            masterLink.setFrequency(frequency, placerId);
        } else {
            slaveLink.setFrequency(frequency);
        }

        if (this.managedNode != null) {
            this.managedNode.setIdlePowerUsage(getEnergy());
        }
        setChanged();
    }

    public void clearDeletedChannel(String channel) {
        if (!Objects.equals(this.frequency, channel)) return;

        if (this.mode) {
            masterLink.unregister(false);
            notifySlavesResync();
        } else {
            slaveLink.destroyConnection();
            slaveLink.realUnregister();
        }

        this.frequency = null;
        usedChannels = 0;

        if (this.managedNode != null) {
            this.managedNode.setIdlePowerUsage(getEnergy());
        }
        setChanged();
    }

    public void setPlacerId(@Nullable UUID placerId, @Nullable String placerName) {
        if (this.placerId != null && !this.placerId.equals(placerId)) {
            if (this.mode) {
                masterLink.unregister();
            } else {
                slaveLink.destroyConnection();
            }
        }
        this.placerId = placerId;
        this.placerName = placerName;
        this.masterLink.setUuid(placerId);
        this.slaveLink.setUuid(placerId);
        setChanged();
    }

    public double getEnergy() {
        if (!ModConfig.INSTANCE.isEnergy || frequency == null) return 0;
        if (this.mode) return ModConfig.INSTANCE.baseEnergy;

        UUID ownerId = !AeWireless.IS_FTB_TEAMS_LOADED ? AeWireless.PUBLIC_NETWORK_UUID : WirelessTeamUtil.getNetworkOwnerUUID(placerId);
        IWirelessEndpoint master = WirelessData.getData(frequency, ownerId);

        if (master != null) {
            BlockPos masterPos = master.getBlockPos();
            BlockPos thisPos = this.getBlockPos();
            if (masterPos != null && thisPos != null) {
                double distance;
                if (master.getDimension() == this.getDimension()) {
                    double dx = masterPos.getX() - thisPos.getX();
                    double dy = masterPos.getY() - thisPos.getY();
                    double dz = masterPos.getZ() - thisPos.getZ();
                    distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                } else {
                    double dx = thisPos.getX();
                    double dz = thisPos.getZ();
                    distance = Math.sqrt(dx * dx + dz * dz);
                }
                return distance * ModConfig.INSTANCE.batteryMultiplier;
            }
        }
        return 0;
    }

    public void onRemoved() {
        if (this.mode) {
            masterLink.unregister();
        } else {
            slaveLink.destroyConnection();
        }
        notifySlavesResync();
        if (managedNode != null) {
            managedNode.destroy();
        }
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel)) return;
        if (((level.getGameTime() + pos.asLong()) % 20L) != 0L) return;

        UUID id = placerId == null ? AeWireless.PUBLIC_NETWORK_UUID : WirelessTeamUtil.getNetworkOwnerUUID(placerId);
        if (!AeWireless.IS_FTB_TEAMS_LOADED) {
            id = AeWireless.PUBLIC_NETWORK_UUID;
        }

        if (WirelessData.isDataReady() && frequency != null && !frequency.isEmpty()) {
            if (mode) {
                IWirelessEndpoint master = WirelessData.getData(frequency, id);
                if (master != this) {
                    masterLink.register();
                }
            } else if (!WirelessData.containsData(frequency, id)) {
                slaveLink.destroyConnection();
                slaveLink.realUnregister();
                frequency = null;
            }
        }

        boolean connected = managedNode.isOnline();
        if (state.getValue(WirelessConnectBlock.CONNECTED) != connected) {
            BlockState blockState = state.setValue(WirelessConnectBlock.CONNECTED, connected);
            level.setBlock(pos, blockState, Block.UPDATE_ALL);
        }

        updateChannelUsedAndMax();

        if (!mode) {
            slaveLink.update();
        }
    }

    private void updateChannelUsedAndMax() {
        usedChannels = 0;
        maxChannels = 0;

        IGridNode node = getGridNode();
        IGrid grid = node == null ? null : node.getGrid();

        if (grid != null) {
            try {
                if (node.isOnline()) {
                    for (var connection : node.getConnections()) {
                        usedChannels = Math.max(connection.getUsedChannels(), usedChannels);
                    }
                }

                if (node instanceof appeng.me.GridNode gridNode) {
                    var channelMode = gridNode.getGrid().getPathingService().getChannelMode();
                    if (channelMode == appeng.api.networking.pathing.ChannelMode.INFINITE) {
                        maxChannels = -1;
                    } else {
                        maxChannels = gridNode.getMaxChannels();
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public @NotNull BlockPos getBlockPos() {
        return this.worldPosition;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.aewireless.wireless_transceiver");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new WirelessMenu(i, inventory, this, data);
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir) {
        return getGridNode();
    }

    @Override
    public IGridNode getGridNode() {
        return managedNode == null ? null : managedNode.getNode();
    }

    @Override
    public boolean isEndpointRemoved() {
        return super.isRemoved();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        ServerLevel serverLevel = super.getLevel() instanceof ServerLevel sl ? sl : null;
        if (serverLevel == null) return;

        GridHelper.onFirstTick(this, be -> be.managedNode.create(be.getLevel(), be.getBlockPos()));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("mode", mode);
        tag.putString("frequency", frequency != null ? frequency : "");

        if (placerId != null) {
            tag.putUUID("placerId", placerId);
        }

        if (managedNode != null) {
            managedNode.saveToNBT(tag);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mode = tag.getBoolean("mode");
        frequency = tag.getString("frequency");

        if (tag.hasUUID("placerId")) {
            placerId = tag.getUUID("placerId");
            this.masterLink.setUuid(this.placerId);
            this.slaveLink.setUuid(this.placerId);
        }

        if (managedNode != null) {
            managedNode.loadFromNBT(tag);
        }

        if (isMode()) {
            masterLink.setFrequency(frequency, placerId);
        } else {
            slaveLink.setFrequency(frequency);
        }

        if (this.managedNode != null) {
            this.managedNode.setIdlePowerUsage(getEnergy());
        }
    }

    @Override
    public ResourceKey<Level> getDimension() {
        if (this.getLevel() != null) {
            return this.getLevel().dimension();
        }
        return Level.OVERWORLD;
    }

    @Override
    public ServerLevel getServerLevel() {
        Level lvl = super.getLevel();
        return lvl instanceof ServerLevel sl ? sl : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public IManagedGridNode getManagedNode() {
        return managedNode;
    }

    public java.util.List<SlaveRef> getSlaveRefsSnapshot() {
        synchronized (slaveRefs) {
            return new ArrayList<>(slaveRefs);
        }
    }

    @Override
    public void registerSlave(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) return;
        synchronized (slaveRefs) {
            slaveRefs.add(new SlaveRef(level.dimension(), pos.immutable()));
        }
    }

    @Override
    public void unregisterSlave(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) return;
        synchronized (slaveRefs) {
            slaveRefs.remove(new SlaveRef(level.dimension(), pos.immutable()));
        }
    }

    @Override
    public void notifySlavesResync() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        java.util.List<SlaveRef> snapshot;
        synchronized (slaveRefs) {
            if (slaveRefs.isEmpty()) return;
            snapshot = new ArrayList<>(slaveRefs);
        }

        for (SlaveRef ref : snapshot) {
            ServerLevel level = server.getLevel(ref.dimension);
            if (level != null) {
                if (level.getBlockEntity(ref.pos) != null) {
                    JoinWorldWireless.add(level, ref.pos);
                } else {
                    synchronized (slaveRefs) {
                        slaveRefs.remove(ref);
                    }
                }
            } else {
                synchronized (slaveRefs) {
                    slaveRefs.remove(ref);
                }
            }
        }
    }

    public UUID getPlacerId() {
        return placerId;
    }

    public boolean isMode() {
        return mode;
    }

    public record SlaveRef(ResourceKey<Level> dimension, BlockPos pos) {}
}
