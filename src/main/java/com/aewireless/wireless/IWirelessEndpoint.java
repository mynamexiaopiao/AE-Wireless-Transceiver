package com.aewireless.wireless;

import appeng.api.networking.IGridNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface IWirelessEndpoint {
    IGridNode getGridNode();

    boolean isEndpointRemoved();
}
