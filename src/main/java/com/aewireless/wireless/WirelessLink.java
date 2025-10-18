package com.aewireless.wireless;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.service.helpers.ConnectionWrapper;

import java.util.Objects;

public class WirelessLink {
    private final IWirelessEndpoint host;
    private String frequency ;

    private ConnectionWrapper connection = new ConnectionWrapper( null);

    public WirelessLink(IWirelessEndpoint host) {
        this.host = host;
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

        IWirelessEndpoint master = WirelessData.getData(frequency);
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
                if (newConnection != null) {
                    connection = new ConnectionWrapper(newConnection);
                }
            }

        } catch (IllegalStateException e) {
            // 记录错误日志
            destroyConnection();
        }
    }

    public void realUnregister() {
        frequency = null;
    }

//    public void update(){
//        if (frequency == null)return;
//
//        if (host.isEndpointRemoved()){
//            destroyConnection();
//            return;
//        }
//        if (frequency.isEmpty()){
//            destroyConnection();
//            return;
//        }
//
//        IWirelessEndpoint master = WirelessData.getData(frequency);
//
//        if (master != null && !master.isEndpointRemoved() ){
//            // 保持/建立连接
//            try {
//                IGridConnection connection1 = connection.getConnection();
//                IGridNode a = host.getGridNode(); //从
//                IGridNode b = master.getGridNode();  //主
//                if (!(a == null || b == null)){
//                    if (connection1 != null) {
//                        IGridNode a1 = connection1.a();
//                        IGridNode b1 = connection1.b();
//                        if ((a1 == a || b1 == a) && (a1 == b || b1 == b)) return;
//
//                        // 否则先断开，再重建
//                        connection1.destroy();
//                        connection = new ConnectionWrapper(null);
//                    }
//                    connection = new ConnectionWrapper(GridHelper.createConnection(a, b));
//                    return;
//                }
//            }catch (IllegalStateException ignore){
//
//            }
//        }
//        destroyConnection();
//    }

    public void destroyConnection() {
        var current = connection.getConnection();
        if (current != null) {
            current.destroy();
            connection.setConnection(null);
        }
        connection = new ConnectionWrapper(null);
    }
}
