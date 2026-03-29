package com.aewireless.mixin;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.capabilities.Capabilities;
import com.aewireless.wireless.block.link.JoinWorldWireless;
import com.aewireless.wireless.block.link.WirelessBlockLink;
import com.aewireless.wireless.block.link.WirelessData;
import com.aewireless.wireless.block.link.WirelessPartLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin extends CapabilityProvider<BlockEntity> implements IForgeBlockEntity {

    @Shadow private CompoundTag customPersistentData;
    @Shadow @Nullable protected Level level;
    @Shadow @Final protected BlockPos worldPosition;

    @Unique
    private WirelessBlockLink aewireless$link;

    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_DIRECTION = "direction";

    protected BlockEntityMixin(Class<BlockEntity> baseClass) {
        super(baseClass);
    }

    @Inject(method = "load", at = @At("TAIL"))
    public void load(CompoundTag tag, CallbackInfo ci) {
        if (level instanceof ServerLevel) {
            aewireless$updateWireless();
        }
    }

    @Override
    public void onLoad() {
        IForgeBlockEntity.super.onLoad();

        if (!(level instanceof ServerLevel)) {
            return;
        }

        if (hasWirelessData()) {
            JoinWorldWireless.add(level, worldPosition);
        }
    }

    @Unique
    public void aewireless$updateWireless() {

        WirelessData data = readWirelessData();
        if (data == null) {
            return;
        }

        if (aewireless$link == null) {
            aewireless$link = createLink(data);
            if (aewireless$link == null) {
                return;
            }
        } else if (aewireless$link.getHostNode() == null) {
            IGridNode hostNode = resolveHostNode(data.direction());
            if (hostNode != null) {
                aewireless$link.setHostNode(hostNode);
            }
        }

        aewireless$link.update();
    }

    @Unique
    public boolean updatePart(){
        if (!((BlockEntity) (Object) this instanceof CableBusBlockEntity)) return true;

        if (aewireless$link == null){
            aewireless$updateWireless();
            return false;
        }

        if (aewireless$link instanceof WirelessPartLink partLink){
            aewireless$link.update();

            return partLink.isConnected();
        }


        return false;
    }

    @Unique
    public boolean updateHost() {
        WirelessData data = readWirelessData();

//        if ((BlockEntity)(Object)this instanceof CableBusBlockEntity) return true;

        if (data == null) {
            return true;
        }

        if (aewireless$link == null) {
            aewireless$updateWireless();
            return aewireless$link != null && aewireless$link.getHostNode() != null;
        }

        if (aewireless$link.getHostNode() == null) {
            IGridNode hostNode = resolveHostNode(data.direction());
            if (hostNode == null) {
                return false;
            }

            aewireless$link.setHostNode(hostNode);
        }

        aewireless$link.update();
        return aewireless$link.getHostNode() != null;
    }

    @Unique
    public void clearLink() {
        if (aewireless$link != null) {
            aewireless$link.destroyConnection();
            aewireless$link = null;
        }
    }

    @Unique
    private boolean hasWirelessData() {
        return customPersistentData != null && customPersistentData.contains(KEY_FREQUENCY);
    }

    @Unique
    @Nullable
    private WirelessData readWirelessData() {
        if (!hasWirelessData()) {
            return null;
        }

        CompoundTag data = customPersistentData;

        int dirIndex = data.getInt(KEY_DIRECTION);
        Direction[] directions = Direction.values();
        if (dirIndex < 0 || dirIndex >= directions.length) {
            return null;
        }

        return new WirelessData(
                data.getString(KEY_FREQUENCY),
                data.getUUID(KEY_UUID),
                directions[dirIndex]
        );
    }

    @Unique
    @Nullable
    private WirelessBlockLink createLink(WirelessData data) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        WirelessBlockLink newLink;
        if (isPart(level, worldPosition)) {
            newLink = new WirelessPartLink(serverLevel, worldPosition);
        } else {
            IGridNode hostNode = resolveHostNode(data.direction());
            if (hostNode == null) {
                return null;
            }
            newLink = new WirelessBlockLink(hostNode, serverLevel, worldPosition);
        }

        newLink.setUuid(data.uuid());
        newLink.setFrequency(data.frequency());
        return newLink;
    }

    @Unique
    @Nullable
    private IGridNode resolveHostNode(Direction direction) {
        IInWorldGridNodeHost nodeHost = getNodeHost((BlockEntity) (Object) this);
        return nodeHost != null ? nodeHost.getGridNode(direction) : null;
    }

    @Unique
    @Nullable
    private IInWorldGridNodeHost getNodeHost(BlockEntity blockEntity) {
        if (blockEntity instanceof IInWorldGridNodeHost host) {
            return host;
        }

        return blockEntity == null
                ? null
                : blockEntity.getCapability(Capabilities.IN_WORLD_GRID_NODE_HOST).orElse(null);
    }

    @Unique
    private boolean isPart(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            IPart part = PartHelper.getPart(level, pos, direction);
            if (part != null) {
                return true;
            }
        }

        return false;
    }


}