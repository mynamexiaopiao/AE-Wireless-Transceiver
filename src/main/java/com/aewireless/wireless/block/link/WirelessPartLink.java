package com.aewireless.wireless.block.link;

import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.me.service.helpers.ConnectionWrapper;
import com.aewireless.AeWirelessConfig;
import com.aewireless.wireless.IWirelessEndpoint;
import com.aewireless.wireless.WirelessData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WirelessPartLink extends WirelessBlockLink{

    List<IGridNode> gridNodes = new ArrayList<>(6);
    Map<IGridNode , ConnectionWrapper> connectionWrappers = new HashMap<>();

    public WirelessPartLink(ServerLevel level, BlockPos pos) {
        super(level, pos);
    }

    @Override
    public void update() {
        if (frequency == null || frequency.isEmpty()) {
            destroyConnection();
            return;
        }

        getParts();

        IWirelessEndpoint master = WirelessData.getData(frequency, uuid);

        boolean crossDimensional = AeWirelessConfig.INSTANCE.crossDimensional;
        ServerLevel masterLevel = master == null ? null : master.getServerLevel();

        if (master != null && !master.isEndpointRemoved() && (crossDimensional || masterLevel == level)) {
            double maxRange = AeWirelessConfig.INSTANCE.maxDistance;

            if (masterLevel == level){
                if (maxRange == 0 || master.getBlockPos().distSqr(pos) <= maxRange * maxRange) {
                    connectParts(master);
                }
            }else if (crossDimensional){
                connectParts(master);
            }
        }else {
            destroyConnection();
        }
    }

    private void connectParts(IWirelessEndpoint master){
        for (IGridNode gridNode : gridNodes) {
            ConnectionWrapper connectionWrapper1 = connectionWrappers.computeIfAbsent(gridNode, ignored -> new ConnectionWrapper(null));
            connect(master , gridNode , connectionWrapper1);
        }
    }

    @Override
    public void destroyConnection() {
        for (Map.Entry<IGridNode, ConnectionWrapper> iGridNodeConnectionWrapperEntry : connectionWrappers.entrySet()) {
            IGridConnection connection1 = iGridNodeConnectionWrapperEntry.getValue().getConnection();
            if (connection1 != null){
                connection1.destroy();
            }
        }
        connectionWrappers.clear();
        unregisterMaster();
    }

    public void getParts(){
        gridNodes.clear();
        for (Direction value : Direction.values()) {
            IPart part = PartHelper.getPart(level, pos, value);
            if (part != null ) {
                IGridNode gridNode = part.getGridNode();
                if (gridNode!= null && !gridNodes.contains(gridNode)){
                    gridNodes.add(gridNode);
                }
            }
        }
        connectionWrappers.entrySet().removeIf(entry -> {
            if (gridNodes.contains(entry.getKey())) {
                return false;
            }
            IGridConnection connection = entry.getValue().getConnection();
            if (connection != null) {
                connection.destroy();
            }
            return true;
        });
    }

    public boolean isConnected(){
        for (Map.Entry<IGridNode, ConnectionWrapper> iGridNodeConnectionWrapperEntry : connectionWrappers.entrySet()) {
            IGridConnection connection1 = iGridNodeConnectionWrapperEntry.getValue().getConnection();
            if (connection1 != null){
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty(){
        return gridNodes.isEmpty();
    }
}
