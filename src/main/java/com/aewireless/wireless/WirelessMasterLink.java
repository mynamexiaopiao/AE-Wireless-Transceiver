package com.aewireless.wireless;

import com.aewireless.AeWireless;

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
        // 设置网络所有者UUID
        this.uuid =WirelessTeamUtil.getNetworkOwnerUUID(uuid);

        if (!AeWireless.IS_FTB_TEAMS_LOADED){
            this.uuid  = AeWireless.PUBLIC_NETWORK_UUID;
        }
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

        uuid =WirelessTeamUtil.getNetworkOwnerUUID(uuid);

        if (!AeWireless.IS_FTB_TEAMS_LOADED){
            uuid  = AeWireless.PUBLIC_NETWORK_UUID;
        }

        if (!frequency.isEmpty() && !host.isEndpointRemoved() &&
                WirelessData.containsData(frequency , uuid) ) {
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
