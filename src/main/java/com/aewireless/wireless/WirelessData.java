package com.aewireless.wireless;

import com.aewireless.AeWireless;
import com.aewireless.network.NetworkHandler;
import com.aewireless.network.packet.WirelessDataUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * 注册中心
 */
public class WirelessData {
    public WirelessData() {}

    private static Map<Key , IWirelessEndpoint> DATA = new HashMap<>();
    private static volatile boolean DATA_READY = false;

    public static synchronized void setDATAMap(Map<Key , IWirelessEndpoint> map){
        DATA = map;
        DATA_READY = true;
    }

    public static synchronized Map<Key , IWirelessEndpoint> getDATAMap(){
        return DATA;
    }

    public static boolean addData(String s , UUID uuid ,  IWirelessEndpoint endpoint){
        if (s == null || s.isEmpty())return false;
        if (endpoint != null){
            if (endpoint.isEndpointRemoved())return false;
        }
        Key key = new Key(s, uuid);
        synchronized (WirelessData.class) {
            if (DATA.containsKey(key) && DATA.get(key) == endpoint) {
                return true;
            }
            DATA.put(key, endpoint);
        }

        // 通知所有相关客户端
        //判断是否为客户端环境，避免在服务端调用客户端代码
        notifyClients(uuid, s, true);

        return true;
    }

    public static synchronized ArrayList<Key> getKeys(){
        return new ArrayList<>(DATA.keySet());
    }

    public static synchronized void clearData(){
        DATA.clear();
        DATA_READY = false;
    }

    public static boolean isDataReady() {
        return DATA_READY;
    }

    public static synchronized boolean containsData(String s , UUID uuid){
        if (s == null || s.isEmpty())return false;
        return DATA.containsKey(new Key(s, uuid));
    }

    public static void removeData(String s , UUID uuid){
        if (s == null || s.isEmpty())return;
        synchronized (WirelessData.class) {
            DATA.remove(new Key(s, uuid));
        }

            // 通知所有相关客户端
            notifyClients(uuid, s, false);

    }


    public static synchronized IWirelessEndpoint getData(String s , UUID uuid){
        if (s == null || s.isEmpty())return null;
        return DATA.get(new Key(s, uuid));
    }

    public record Key(String string , UUID uuid){}

    // 添加通知客户端的方法
    private static void notifyClients(UUID teamId, String data, boolean isAdd) {
        // 获取服务器实例和玩家列表
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            PlayerList playerList = server.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                if (AeWireless.IS_FTB_TEAMS_LOADED) {
                    UUID playerTeamId = WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID());
                    if (playerTeamId.equals(teamId)) {
                        NetworkHandler.sendToPlayer(
                                new WirelessDataUpdatePacket(data, isAdd),
                                player
                        );
                    }
                } else {
                    NetworkHandler.sendToPlayer(
                            new WirelessDataUpdatePacket(data, isAdd),
                            player);

                }
            }
        }
    }


}
