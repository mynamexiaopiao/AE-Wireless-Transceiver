package com.aewireless.wireless;

import com.aewireless.AeWireless;
import com.aewireless.network.NetworkHandler;
import com.aewireless.network.packet.WirelessDataUpdatePacket;
import net.minecraft.client.Minecraft;
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

    public static synchronized void setDATAMap(Map<Key , IWirelessEndpoint> map){
        DATA = map;
    }

    public static synchronized Map<Key , IWirelessEndpoint> getDATAMap(){
        return DATA;
    }

    public static synchronized boolean addData(String s , UUID uuid ,  IWirelessEndpoint endpoint){
        if (s.isEmpty())return false;
        if (endpoint != null){
            if (endpoint.isEndpointRemoved())return false;
        }
        DATA.put(new Key(s, uuid), endpoint);

        // 通知所有相关客户端
        //判断是否为客户端环境，避免在服务端调用客户端代码
        notifyClients(uuid, s, true); // 修正：使用参数s而不是未定义的data变量

        return true;
    }

    public static synchronized ArrayList<Key> getKeys(){
        return new ArrayList<>(DATA.keySet());
    }

    public static synchronized void clearData(){
        DATA.clear();
    }

    public static synchronized boolean containsData(String s , UUID uuid){
        return DATA.containsKey(new Key(s, uuid));
    }

    public static synchronized void removeData(String s , UUID uuid){
        if (s.isEmpty())return;
        DATA.remove(new Key(s, uuid));

            // 通知所有相关客户端
            notifyClients(uuid, s, false);

    }


    public static synchronized IWirelessEndpoint getData(String s , UUID uuid){
        if (s.isEmpty())return null;
        return DATA.get(new Key(s, uuid));
    }

    public record Key(String string , UUID uuid){}

    // 添加通知客户端的方法
    private static void notifyClients(UUID teamId, String data, boolean isAdd) {
        // 获取服务器实例和玩家列表
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            PlayerList playerList = server.getPlayerList();
            if (playerList != null) {
                for (ServerPlayer player : playerList.getPlayers()) {
                    if (AeWireless.IS_FTB_TEAMS_LOADED){
                        UUID playerTeamId = WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID());
                        if (playerTeamId.equals(teamId) ) {
                            NetworkHandler.sendToPlayer(
                                    new WirelessDataUpdatePacket(data, isAdd),
                                    player
                            );
                        }
                    }else {
                        NetworkHandler.sendToPlayer(
                                new WirelessDataUpdatePacket(data, isAdd),
                                player);

                    }
                }
            }
        }
    }


    // 添加判断是否为客户端的辅助方法
    private static boolean isClientSide() {
        return Minecraft.getInstance() != null;
    }
}
