package com.aewireless.api;

import com.aewireless.wireless.block.link.WirelessBlockLink;

public interface IWirelessBlockEntity {
    boolean updatePart();

    boolean updateHost();

    void updateWireless();

    void clearLink();

    WirelessBlockLink getLink();

}
