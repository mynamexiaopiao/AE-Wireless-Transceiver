package com.aewireless.compat.jade.blockentity;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import com.aewireless.AeWireless;
import com.aewireless.api.IWirelessBlockEntity;
import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.UUID;

public enum BlockEntityProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(AeWireless.MOD_ID, "block_entity_jade");


    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        BlockEntity blockEntity = blockAccessor.getBlockEntity();
        CompoundTag persistentData = blockEntity.getPersistentData();
        if (persistentData.contains("uuid" ) && persistentData.contains("frequency") && persistentData.contains("direction")){
            String frequency = persistentData.getString("frequency");
            UUID uuid = persistentData.getUUID("uuid");
            Level level = blockAccessor.getLevel();
            if (level instanceof ServerLevel level1){
                Component ownerName = WirelessTeamUtil.getNetworkOwnerName(level1 , uuid);
                compoundTag.putString("uuid", ownerName.getString());
            }

            compoundTag.putString("frequency", frequency);
            compoundTag.putInt("direction", persistentData.getInt("direction"));

            boolean connected = ((IWirelessBlockEntity) blockEntity).getLink() != null && ((IWirelessBlockEntity) blockEntity).getLink().isConnected();
            compoundTag.putBoolean("wirelessConnected", connected);
        }
    }
}
