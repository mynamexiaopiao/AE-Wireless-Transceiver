package com.aewireless.wireless.block;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.me.service.helpers.ConnectionWrapper;
import com.aewireless.AeWireless;
import com.aewireless.AeWirelessConfig;
import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.UUID;

public class WirelessBlockLink {
    private IInWorldGridNodeHost host;
    private ServerLevel level;
    private BlockPos pos;
    private String frequency ;
    private UUID uuid;
    private Direction direction;

    private ConnectionWrapper connection = new ConnectionWrapper( null);

    public WirelessBlockLink(IInWorldGridNodeHost host , ServerLevel level , BlockPos pos) {
        this.host = host;
        this.level = level;
        this.pos = pos;
    }

    public void setUuid(UUID uuid) {
        this.uuid = WirelessTeamUtil.getNetworkOwnerUUID(uuid);

        if (!AeWireless.IS_FTB_TEAMS_LOADED){
            this.uuid  = AeWireless.PUBLIC_NETWORK_UUID;
        }
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
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

        setUuid(uuid);

        Double distance = 0.0D;

        IWirelessEndpoint master = WirelessData.getData(frequency, uuid);

        boolean crossDimensional = AeWirelessConfig.INSTANCE.crossDimensional;

        if (master != null && !master.isEndpointRemoved() && (crossDimensional || master.getServerLevel() == level)) {

            distance = Math.sqrt(master.getBlockPos().distSqr(pos));


            double maxRange = AeWirelessConfig.INSTANCE.maxDistance;

            if (master.getServerLevel() == level){
                if ( distance <= maxRange || maxRange == 0) {
                    connect(master , direction);
                }
            }else if (crossDimensional){
                connect(master , direction);
            }
        }else {
            destroyConnection();
        }
    }

    private void connect(IWirelessEndpoint master , Direction direction) {
        try {
            IGridConnection existingConnection = connection.getConnection();

            if (host == null) return;

            IGridNode hostNode = host.getGridNode(direction);
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
                connection.setConnection(newConnection);
            }

        } catch (IllegalStateException e) {
            destroyConnection();
        }
    }

    public IInWorldGridNodeHost getHost() {
        return host;
    }

    public void setHost(IInWorldGridNodeHost host) {
        this.host = host;
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
