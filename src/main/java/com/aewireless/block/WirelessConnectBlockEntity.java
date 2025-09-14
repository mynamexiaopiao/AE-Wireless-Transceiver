package com.aewireless.block;

import appeng.api.networking.*;
import com.aewireless.gui.wireless.WirelessMenu;
import com.aewireless.register.ModRegister;
import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessLink;
import com.aewireless.wireless.WirelessMasterLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class WirelessConnectBlockEntity extends BlockEntity implements MenuProvider , IInWorldGridNodeHost , IWirelessEndpoint {
    private IManagedGridNode managedNode;

    private WirelessMasterLink masterLink;
    private WirelessLink slaveLink;
    private String frequency;

    private boolean mode = false;

    public WirelessConnectBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegister.WIRELESS_TRANSCEIVER_ENTITY.get(), pos, blockState);

        this.managedNode = GridHelper.createManagedNode(this, (nodeOwner, node) -> {nodeOwner.setChanged();});
        this.managedNode.setTagName("wireless");
        this.managedNode.setInWorldNode(true);
        this.managedNode.setExposedOnSides(EnumSet.allOf(Direction.class));

        masterLink = new WirelessMasterLink(this);
        slaveLink = new WirelessLink(this);
    }

    public String getFrequency() {
        return frequency;
    }

    public void setMasterMode(boolean masterMode){
        if (this.mode == masterMode)return;

        if (this.mode) {
            masterLink.unregister();
        } else {
            slaveLink.destroyConnection();
        }

        this.mode = masterMode;


        if (this.mode) {
            masterLink.setFrequency( frequency);
        } else {
            slaveLink.setFrequency( frequency);
        }

        setChanged();
    }


    public void setFrequency(String frequency) {
        this.frequency = frequency;
        if (isMode()) {
            masterLink.setFrequency(frequency);
        } else {
            slaveLink.setFrequency(frequency);
        }
        setChanged();
    }


    public void onRemoved() {
        if (this.mode) {
            masterLink.unregister();
        } else {
            slaveLink.destroyConnection();
        }
        if (managedNode != null) {
            managedNode.destroy();
        }
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {

        WirelessConnectBlockEntity blockEntity = (WirelessConnectBlockEntity)level.getBlockEntity(pos);

        //修复无法删除
        if (WirelessData.DATA.containsKey(blockEntity.getFrequency())){
            blockEntity.setFrequency(blockEntity.getFrequency());
        }

        //修复频道删除但保存问题
        if (!WirelessData.DATA.containsKey(blockEntity.getFrequency())){
            if (!blockEntity.mode){
                blockEntity.slaveLink.destroyConnection();
                blockEntity.slaveLink.realUnregister();
            }

            blockEntity.frequency = null;
        }

        if (managedNode.isOnline()){
            BlockState blockState = state.setValue(WirelessConnectBlock.CONNECTED, true);
            level.setBlock(pos, blockState, Block.UPDATE_ALL );
        }else {
            BlockState blockState = state.setValue(WirelessConnectBlock.CONNECTED, false);
            level.setBlock(pos, blockState, Block.UPDATE_ALL );
        }


        if (!(level instanceof ServerLevel)) return;

        if (blockEntity != null && !blockEntity.mode) {
            blockEntity.slaveLink.update();
        }
    }


    @Override
    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.aewireless.wireless_transceiver");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory arg, Player arg2) {
        return new WirelessMenu(i, arg, this);
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
        ServerLevel sl1 = super.getLevel() instanceof ServerLevel sl ? sl : null;
        if (sl1 == null) return;

        GridHelper.onFirstTick(this, be -> {
            be.managedNode.create(be.getLevel(), be.getBlockPos());
        });
    }



    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("mode", mode);

        tag.putString("frequency", frequency != null ? frequency : "");

        if (managedNode != null) {
            managedNode.saveToNBT(tag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mode = tag.getBoolean("mode");

        frequency = tag.getString("frequency");

        if (managedNode != null) {
            managedNode.loadFromNBT(tag);
        }

        if (isMode()) {
            masterLink.setFrequency(frequency);
        } else {
            slaveLink.setFrequency(frequency);
        }

    }

    public boolean isMode() {
        return mode;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
