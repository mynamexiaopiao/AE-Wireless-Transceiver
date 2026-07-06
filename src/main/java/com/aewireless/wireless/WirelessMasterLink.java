package com.aewireless.wireless;

import com.aewireless.AeWireless;

import java.util.Objects;
import java.util.UUID;

public class WirelessMasterLink {
    private final IWirelessEndpoint host;
    private String frequency;
    private boolean registered;
    private UUID uuid;

    public WirelessMasterLink(IWirelessEndpoint host) {
        this.host = host;
    }

    public void setUuid(UUID uuid) {
        this.uuid = WirelessTeamUtil.getNetworkOwnerUUID(uuid);

        if (this.uuid == null || !AeWireless.IS_FTB_TEAMS_LOADED) {
            this.uuid = AeWireless.PUBLIC_NETWORK_UUID;
        }
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency, UUID uuid) {
        if (frequency == null) return;

        UUID ownerId = WirelessTeamUtil.getNetworkOwnerUUID(uuid);
        if (ownerId == null || !AeWireless.IS_FTB_TEAMS_LOADED) {
            ownerId = AeWireless.PUBLIC_NETWORK_UUID;
        }

        if (registered && (!frequency.equals(this.frequency) || !Objects.equals(ownerId, this.uuid))) {
            unregister();
        }

        this.frequency = frequency;
        this.uuid = ownerId;

        if (!frequency.isEmpty() && !host.isEndpointRemoved()) {
            register();
        }
    }

    public boolean register() {
        if (frequency == null || frequency.isEmpty()) return false;
        boolean registeredNow = WirelessData.addData(frequency, uuid, host);
        this.registered = registeredNow;
        return registeredNow;
    }

    public void unregister() {
        unregister(true);
    }

    public void unregister(boolean keepChannel) {
        if (frequency != null && (!registered || frequency.isEmpty())) return;
        if (keepChannel && WirelessData.containsData(frequency, uuid)) {
            WirelessData.addData(frequency, uuid, null);
        }
        registered = false;
        if (host instanceof IWirelessMasterEndpoint master) {
            master.notifySlavesResync();
        }
    }
}
