package com.aewireless.wireless;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.service.helpers.ConnectionWrapper;
import com.aewireless.AeWireless;
import com.aewireless.AeWirelessConfig;
import net.minecraft.server.level.ServerLevel;

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
        UUID ownerId = WirelessTeamUtil.getNetworkOwnerUUID(uuid);
        if (ownerId == null || !AeWireless.IS_FTB_TEAMS_LOADED){
            ownerId  = AeWireless.PUBLIC_NETWORK_UUID;
        }
        if (Objects.equals(this.uuid, ownerId)) return;
        this.uuid = ownerId;
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

        ServerLevel level = host.getServerLevel();
        IWirelessEndpoint master = WirelessData.getData(frequency, uuid);

        boolean crossDimensional = AeWirelessConfig.INSTANCE.crossDimensional;
        ServerLevel masterLevel = master == null ? null : master.getServerLevel();

        if (master != null && !master.isEndpointRemoved() && (crossDimensional || masterLevel == level)) {
            double maxRange = AeWirelessConfig.INSTANCE.maxDistance;

            if (masterLevel == level){
                if (maxRange == 0 || master.getBlockPos().distSqr(host.getBlockPos()) <= maxRange * maxRange) {
                    connect(master);
                }
            }else if (crossDimensional){
                connect(master);
            }


        }else {
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
                // 连接不匹配，需要重新建立
                existingConnection.destroy();
            }

            // 建立新连接
            if (!hostNode.equals(masterNode)){
                IGridConnection newConnection = GridHelper.createConnection(hostNode, masterNode);
                connection = new ConnectionWrapper(newConnection);
                updateEnergy();
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
            updateEnergy();
        }
        connection = new ConnectionWrapper(null);
    }

    private void updateEnergy() {
        if (host instanceof com.aewireless.block.WirelessConnectBlockEntity be) {
            be.getManagedNode().setIdlePowerUsage(be.getEnergy());
        }
    }
}
