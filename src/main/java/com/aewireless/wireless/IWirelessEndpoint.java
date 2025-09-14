package com.aewireless.wireless;

import appeng.api.networking.IGridNode;

public interface IWirelessEndpoint {
    IGridNode getGridNode();

    boolean isEndpointRemoved();
}
