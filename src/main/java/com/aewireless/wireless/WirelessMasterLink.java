package com.aewireless.wireless;

import com.aewireless.gui.wireless.WirelessMenu;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public class WirelessMasterLink {
    private final IWirelessEndpoint host;
    private String  frequency;
    private boolean registered;

    public WirelessMasterLink(IWirelessEndpoint host) {
        this.host = host;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        if (frequency == null)return;

        if (registered) {
            unregister();
        }
        this.frequency = frequency;
        if (!frequency.isEmpty() && !host.isEndpointRemoved() && WirelessData.DATA.containsKey(frequency) ) {
            register();
        }

    }



    public boolean register() {
        if (frequency.isEmpty()) return false;
        if (WirelessData.DATA.containsKey(frequency)){
            boolean is = WirelessData.addData(frequency, host);
            this.registered = is;
            return is;
        }
        return false;
    }


    public void unregister() {
        if (frequency != null && (!registered || frequency.isEmpty())) return;
        if (WirelessData.DATA.containsKey(frequency)){
            WirelessData.DATA.put(frequency, null);
        }
        registered = false;
    }
}
