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

        setUuid(uuid);
        getParts();

        Double distance = 0.0D;

        IWirelessEndpoint master = WirelessData.getData(frequency, uuid);

        boolean crossDimensional = AeWirelessConfig.INSTANCE.crossDimensional;

        if (master != null && !master.isEndpointRemoved() && (crossDimensional || master.getServerLevel() == level)) {

            distance = master.getBlockPos().distSqr(pos);


            double maxRange = AeWirelessConfig.INSTANCE.maxDistance;

            if (master.getServerLevel() == level){
                if ( distance <= maxRange*maxRange || maxRange == 0) {
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
            ConnectionWrapper connectionWrapper1 = connectionWrappers.get(gridNode);
            if (connectionWrapper1 == null){
                ConnectionWrapper connectionWrapper = new ConnectionWrapper(null);
                connectionWrappers.put(gridNode,connectionWrapper);
                return;
            }
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
    }

    public void getParts(){
        for (Direction value : Direction.values()) {
            IPart part = PartHelper.getPart(level, pos, value);
            if (part != null ) {
                IGridNode gridNode = part.getGridNode();
                if (gridNode!= null && !gridNodes.contains(gridNode)){
                    gridNodes.add(gridNode);
                }
            }
        }
    }

    public boolean isEmpty(){
        return gridNodes.isEmpty();
    }
}
