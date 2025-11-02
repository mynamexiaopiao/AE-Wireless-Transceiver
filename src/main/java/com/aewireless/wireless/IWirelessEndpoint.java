package com.aewireless.wireless;

import appeng.api.networking.IGridNode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public interface IWirelessEndpoint {
    IGridNode getGridNode();

    boolean isEndpointRemoved();

    BlockPos getBlockPos();

    ResourceKey<Level> getDimension();
}
