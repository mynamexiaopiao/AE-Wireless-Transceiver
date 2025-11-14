package com.aewireless.wireless;

import com.aewireless.gui.wireless.WirelessMenu;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.UUID;

public class WirelessMasterLink {
    private final IWirelessEndpoint host;
    private String  frequency;
    private boolean registered;
    private UUID uuid;

    public WirelessMasterLink(IWirelessEndpoint host) {
        this.host = host;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency , UUID uuid) {
        if (frequency == null)return;

        if (registered) {
            unregister();
        }
        this.frequency = frequency;
        if (!frequency.isEmpty() && !host.isEndpointRemoved() && WirelessData.containsData(frequency , uuid) ) {
            register();
        }

    }

    public boolean register() {
        if (frequency.isEmpty()) return false;
        if (WirelessData.containsData(frequency , uuid)){
            boolean is = WirelessData.addData(frequency, uuid,  host);
            this.registered = is;
            return is;
        }
        return false;
    }


    public void unregister() {
        if (frequency != null && (!registered || frequency.isEmpty())) return;
        if (WirelessData.containsData(frequency , uuid)){
            WirelessData.addData(frequency, uuid , null);
        }
        registered = false;
    }
}
