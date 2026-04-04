package com.aewireless.wireless;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.service.helpers.ConnectionWrapper;
import com.aewireless.AeWireless;
import com.aewireless.ModConfig;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.UUID;

public class WirelessLink {
    private final IWirelessEndpoint host;
    private String frequency;
    private UUID uuid;

    private ConnectionWrapper connection = new ConnectionWrapper(null);

    public WirelessLink(IWirelessEndpoint host) {
        this.host = host;
    }

    public void setUuid(UUID uuid) {
        if (this.uuid == uuid) return;
        this.uuid = WirelessTeamUtil.getNetworkOwnerUUID(uuid);

        if (!AeWireless.IS_FTB_TEAMS_LOADED) {
            this.uuid = AeWireless.PUBLIC_NETWORK_UUID;
        }
    }

    public void setFrequency(String frequency) {
        if (frequency == null) return;
        if (Objects.equals(this.frequency, frequency)) return;
        this.frequency = frequency;
        // 重连
        update();
    }

    public void update() {
        if (frequency == null || frequency.isEmpty()) {
            destroyConnection();
            return;
        }

        if (host.isEndpointRemoved()) {
            destroyConnection();
            return;
        }

        setUuid(uuid);

        ServerLevel level = host.getServerLevel();
        IWirelessEndpoint master = WirelessData.getData(frequency, uuid);
        boolean crossDimensional = ModConfig.INSTANCE.crossDimensional;

        if (master != null && !master.isEndpointRemoved() && (crossDimensional || master.getServerLevel() == level)) {
            double distance = Math.sqrt(master.getBlockPos().distSqr(host.getBlockPos()));
            double maxRange = ModConfig.INSTANCE.maxDistance;

            if (master.getServerLevel() == level) {
                if (distance <= maxRange || maxRange == 0) {
                    connect(master);
                } else {
                    destroyConnection();
                }
            } else if (crossDimensional) {
                connect(master);
            }
        } else {
            destroyConnection();
        }
    }

    private void connect(IWirelessEndpoint master) {
        try {
            IGridConnection existingConnection = connection.getConnection();
            IGridNode hostNode = host.getGridNode();
            IGridNode masterNode = master.getGridNode();

            if (hostNode == null || masterNode == null) {
                destroyConnection();
                return;
            }

            // 检查是否已经连接
            if (existingConnection != null) {
                IGridNode a = existingConnection.a();
                IGridNode b = existingConnection.b();
                if ((a == hostNode || b == hostNode) && (a == masterNode || b == masterNode)) {
                    return;
                }
                existingConnection.destroy();
            }

            // 建立新连接
            if (!hostNode.equals(masterNode)) {
                IGridConnection newConnection = GridHelper.createConnection(hostNode, masterNode);
                connection = new ConnectionWrapper(newConnection);
            }

        } catch (IllegalStateException e) {
            destroyConnection();
        }
    }

    public void realUnregister() {
        frequency = null;
    }

    public void destroyConnection() {
        var current = connection.getConnection();
        if (current != null) {
            current.destroy();
            connection.setConnection(null);
        }
        connection = new ConnectionWrapper(null);
    }
}
