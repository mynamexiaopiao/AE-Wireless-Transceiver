package com.aewireless.wireless;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface IWirelessMasterEndpoint {
    void registerSlave(ServerLevel level, BlockPos pos);

    void unregisterSlave(ServerLevel level, BlockPos pos);

    void notifySlavesResync();
}
