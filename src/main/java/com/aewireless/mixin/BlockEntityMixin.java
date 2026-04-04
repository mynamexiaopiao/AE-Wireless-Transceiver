package com.aewireless.mixin;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.capabilities.Capabilities;
import com.aewireless.api.IWirelessBlockEntity;
import com.aewireless.compat.gtceu.GTCeuPacketUtil;
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
public abstract class BlockEntityMixin extends CapabilityProvider<BlockEntity>
        implements IForgeBlockEntity, IWirelessBlockEntity {

    @Shadow private CompoundTag customPersistentData;
    @Shadow @Nullable protected Level level;
    @Shadow @Final protected BlockPos worldPosition;

    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_DIRECTION = "direction";


    @Unique
    private WirelessBlockLink link;

    protected BlockEntityMixin(Class<BlockEntity> baseClass) {
        super(baseClass);
    }

    /* ---------------- 生命周期 ---------------- */

    @Inject(method = "load", at = @At("TAIL"))
    public void onLoadNBT(CompoundTag tag, CallbackInfo ci) {
        WirelessData data = readWirelessData();
        if (level instanceof ServerLevel &&
                (BlockEntity)(Object)this instanceof IInWorldGridNodeHost &&
                data != null ) {
            if (link != null && !link.isConnected()) {
                updateWireless(data);
            }
        }
    }


    @Override
    public void onLoad() {
        IForgeBlockEntity.super.onLoad();

        if (!(level instanceof ServerLevel)) return;
        if (!hasWirelessData()) return;
        JoinWorldWireless.add(level, worldPosition);
    }

    /* ---------------- 对外更新接口 ---------------- */

    @Override
    public WirelessBlockLink getLink() {
        return link;
    }

    @Unique
    public boolean updatePart() {
        if (!((Object) this instanceof CableBusBlockEntity)) return true;

        if (link == null) {
            updateWireless();
            return false;
        }

        if (link instanceof WirelessPartLink partLink) {
            link.update();
            return partLink.isConnected();
        }

        return false;
    }

    @Unique
    public boolean updateHost() {
        WirelessData data = readWirelessData();
        if (data == null) return true;

        if (link == null) {
            link = createLink(data);
            if (link == null) {
                return false;
            }
        }

        ensureHostNode(data);

        link.update();
        return link.isConnected();
    }

    @Unique
    public void clearLink() {
        if (link == null) return;

        link.destroyConnection();
        link = null;
    }

    /* ---------------- 核心逻辑 ---------------- */

    @Unique
    public void updateWireless() {
        WirelessData data = readWirelessData();
        updateWireless(data);
    }

    @Unique
    private void updateWireless(@Nullable WirelessData data) {
        if (data == null) return;

        if (link == null) {
            link = createLink(data);
            if (link == null) return;
        }

        ensureHostNode(data);
        link.update();
    }


    @Unique
    private void ensureHostNode(WirelessData data) {
        if (link.getHostNode() != null) return;

        IGridNode node = resolveHostNode(data.direction());
        if (node != null) {
            link.setHostNode(node);
        }
    }

    /* ---------------- 数据处理 ---------------- */

    @Unique
    private boolean hasWirelessData() {
        return customPersistentData != null
                && customPersistentData.contains(KEY_FREQUENCY);
    }

    @Unique
    @Nullable
    private WirelessData readWirelessData() {
        if (!hasWirelessData()) return null;

        int dirIndex = customPersistentData.getInt(KEY_DIRECTION);
        Direction[] dirs = Direction.values();

        if (dirIndex < 0 || dirIndex >= dirs.length) return null;

        return new WirelessData(
                customPersistentData.getString(KEY_FREQUENCY),
                customPersistentData.getUUID(KEY_UUID),
                dirs[dirIndex]
        );
    }

    /* ---------------- Link 创建 ---------------- */

    @Unique
    @Nullable
    private WirelessBlockLink createLink(WirelessData data) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        WirelessBlockLink newLink;

        if (isPart()) {
            newLink = new WirelessPartLink(serverLevel, worldPosition);
        } else {
            IGridNode node = resolveHostNode(data.direction());
            if (node == null) return null;

            newLink = new WirelessBlockLink(node, serverLevel, worldPosition);
        }

        newLink.setUuid(data.uuid());
        newLink.setFrequency(data.frequency());

        return newLink;
    }

    /* ---------------- AE2 相关 ---------------- */

    @Unique
    @Nullable
    private IGridNode resolveHostNode(Direction direction) {
        IInWorldGridNodeHost host = getNodeHost((BlockEntity) (Object) this);
        return host != null ? host.getGridNode(direction) : null;
    }

    @Unique
    @Nullable
    private IInWorldGridNodeHost getNodeHost(BlockEntity be) {
        if (be instanceof IInWorldGridNodeHost host) return host;

        return be.getCapability(Capabilities.IN_WORLD_GRID_NODE_HOST)
                .orElse(null);
    }

    @Unique
    private boolean isPart() {
        if (level == null || worldPosition == null) return false;

        if ((BlockEntity)(Object)this instanceof CableBusBlockEntity){
            return true;
        }

        return false;
    }
}
