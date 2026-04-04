package com.aewireless.mixin;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.GridHelper;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.aewireless.api.IWirelessBlockEntity;
import com.aewireless.wireless.block.link.JoinWorldWireless;
import com.aewireless.wireless.block.link.WirelessBlockLink;
import com.aewireless.wireless.block.link.WirelessData;
import com.aewireless.wireless.block.link.WirelessPartLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin  extends AttachmentHolder implements IBlockEntityExtension,IWirelessBlockEntity{

    @Shadow @Nullable protected Level level;
    @Shadow @Final protected BlockPos worldPosition;

    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_DIRECTION = "direction";

    @Unique
    private WirelessBlockLink link;

    /* ---------------- 生命周期 ---------------- */

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    public void onLoadNBT(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        WirelessData data = readWirelessData();
        if (level instanceof ServerLevel &&
                (BlockEntity) (Object) this instanceof IInWorldGridNodeHost &&
                data != null) {
            if (link != null && !link.isConnected()) {
                updateWireless(data);
            }
        }
    }

    @Override
    public void onLoad() {
        IBlockEntityExtension.super.onLoad();
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
        CompoundTag tag = ((BlockEntity) (Object) this).getPersistentData();
        return tag.contains(KEY_FREQUENCY);
    }

    @Unique
    @Nullable
    private WirelessData readWirelessData() {
        CompoundTag tag = ((BlockEntity) (Object) this).getPersistentData();
        if (!tag.contains(KEY_FREQUENCY)) return null;

        int dirIndex = tag.getInt(KEY_DIRECTION);
        Direction[] dirs = Direction.values();

        if (dirIndex < 0 || dirIndex >= dirs.length) return null;

        return new WirelessData(
                tag.getString(KEY_FREQUENCY),
                tag.getUUID(KEY_UUID),
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
        Level lvl = be.getLevel();
        if (lvl == null) return null;
        return GridHelper.getNodeHost(lvl, be.getBlockPos());
    }

    @Unique
    private boolean isPart() {
        return (BlockEntity) (Object) this instanceof CableBusBlockEntity;
    }
}
