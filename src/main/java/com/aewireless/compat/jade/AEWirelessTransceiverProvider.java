package com.aewireless.compat.jade;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum AEWirelessTransceiverProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;


    private static final ResourceLocation UID = new ResourceLocation(AeWireless.MOD_ID, "wireless_transceiver_jade");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof WirelessConnectBlockEntity blockEntity){

            if (blockEntity.getFrequency() != null){
                data.putString("channel_name" , blockEntity.getFrequency());
                data.putBoolean("masterMode" , blockEntity.isMode());
            }

            //参考etd plus
            IGridNode node = blockEntity.getGridNode();
            IGrid grid = node == null ? null : node.getGrid();

            boolean networkUsable = false;
            if (grid != null) {
                try {
                    networkUsable = grid.getEnergyService().isNetworkPowered();
                } catch (Throwable ignored) {
                    networkUsable = false;
                }
            }

            data.putBoolean("networkUsable", networkUsable);

            int usedChannels = 0;
            int maxChannels = 0;
            if (node != null && node.isActive()) {
                // 遍历该节点的所有连接，取使用频道数的最大值
                for (var connection : node.getConnections()) {
                    usedChannels = Math.max(connection.getUsedChannels(), usedChannels);
                }
                // 获取节点的最大频道容量（致密线缆为32）
                if (node instanceof appeng.me.GridNode gridNode) {
                    var channelMode = gridNode.getGrid().getPathingService().getChannelMode();
                    if (channelMode == appeng.api.networking.pathing.ChannelMode.INFINITE) {
                        maxChannels = -1; // 无限频道
                    } else {
                        maxChannels = gridNode.getMaxChannels();
                    }
                }
            }
            data.putInt("usedChannels", usedChannels);
            data.putInt("maxChannels", maxChannels);


            // 如果是从模式，查询主节点位置与维度
            if (!blockEntity.isMode() && blockEntity.getFrequency() != null) {
                var level = blockEntity.getLevel();
                String freq = blockEntity.getFrequency();

                IWirelessEndpoint master = WirelessData.getData(freq);
                if (master != null && !master.isEndpointRemoved()) {
//                    if (master instanceof WirelessConnectBlockEntity masterBlockEntity && masterBlockEntity.getCustomName() != null) {
//                        data.putString("customName", masterBlockEntity.getCustomName().getString());
//                    }
                    BlockPos pos = master.getBlockPos();
                    if (pos != null) {
                        data.putLong("masterPos", pos.asLong());
                    }

                    data.putString("masterDim", master.getDimension().location().toLanguageKey());

                }
            }
        }


    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
