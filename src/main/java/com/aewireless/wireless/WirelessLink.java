package com.aewireless.wireless;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.service.helpers.ConnectionWrapper;
import com.aewireless.AeWireless;

import java.util.Objects;
import java.util.UUID;

public class WirelessLink {
    private final IWirelessEndpoint host;
    private String frequency ;
    private UUID uuid;

    private ConnectionWrapper connection = new ConnectionWrapper( null);

    public WirelessLink(IWirelessEndpoint host) {
        this.host = host;
    }

    public void setUuid(UUID uuid) {
        this.uuid = WirelessTeamUtil.getNetworkOwnerUUID(uuid);

        if (!AeWireless.IS_FTB_TEAMS_LOADED){
            this.uuid  = AeWireless.PUBLIC_NETWORK_UUID;
        }
    }

    public void setFrequency(String frequency) {
        if (frequency == null)return;
        if (Objects.equals(this.frequency, frequency)) return;
        this.frequency = frequency;
        //重连
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

        IWirelessEndpoint master = WirelessData.getData(frequency, uuid);
        if (master == null || master.isEndpointRemoved()) {
            destroyConnection();
            return;
        }

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
                    return; // 已经正确连接
                }
                // 连接不匹配，需要重新建立
                existingConnection.destroy();
            }

            // 建立新连接
            if (!hostNode.equals(masterNode)){
                IGridConnection newConnection = GridHelper.createConnection(hostNode, masterNode);
                connection = new ConnectionWrapper(newConnection);
            }

        } catch (IllegalStateException e) {
            // 记录错误日志
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
