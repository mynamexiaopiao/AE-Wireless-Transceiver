package com.aewireless.wireless;

import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.aewireless.network.NetworkHandler;
import com.aewireless.network.packet.WirelessDataUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WirelessData {
    private static Map<Key, IWirelessEndpoint> DATA = new HashMap<>();
    private static volatile boolean DATA_READY = false;

    public WirelessData() {
    }

    public static synchronized void setDATAMap(Map<Key, IWirelessEndpoint> map) {
        DATA = map;
        DATA_READY = true;
    }

    public static synchronized Map<Key, IWirelessEndpoint> getDATAMap() {
        return DATA;
    }

    public static boolean addData(String channel, UUID uuid, IWirelessEndpoint endpoint) {
        if (channel == null || channel.isEmpty()) return false;
        if (endpoint != null && endpoint.isEndpointRemoved()) return false;

        Key key = new Key(channel, uuid);
        synchronized (WirelessData.class) {
            if (DATA.containsKey(key) && DATA.get(key) == endpoint) {
                return true;
            }
            DATA.put(key, endpoint);
        }

        notifyClients(uuid, channel, true);
        return true;
    }

    public static synchronized ArrayList<Key> getKeys() {
        return new ArrayList<>(DATA.keySet());
    }

    public static synchronized void clearData() {
        DATA.clear();
        DATA_READY = false;
    }

    public static boolean isDataReady() {
        return DATA_READY;
    }

    public static synchronized boolean containsData(String channel, UUID uuid) {
        if (channel == null || channel.isEmpty()) return false;
        return DATA.containsKey(new Key(channel, uuid));
    }

    public static void removeData(String channel, UUID uuid) {
        if (channel == null || channel.isEmpty()) return;

        IWirelessEndpoint removed;
        synchronized (WirelessData.class) {
            removed = DATA.remove(new Key(channel, uuid));
        }

        if (removed instanceof WirelessConnectBlockEntity blockEntity) {
            blockEntity.clearDeletedChannel(channel);
        }

        notifyClients(uuid, channel, false);
    }

    public static synchronized IWirelessEndpoint getData(String channel, UUID uuid) {
        if (channel == null || channel.isEmpty()) return null;
        return DATA.get(new Key(channel, uuid));
    }

    public record Key(String string, UUID uuid) {}

    private static void notifyClients(UUID teamId, String data, boolean isAdd) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        PlayerList playerList = server.getPlayerList();
        for (ServerPlayer player : playerList.getPlayers()) {
            if (AeWireless.IS_FTB_TEAMS_LOADED) {
                UUID playerTeamId = WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID());
                if (playerTeamId.equals(teamId)) {
                    NetworkHandler.sendToPlayer(new WirelessDataUpdatePacket(data, isAdd), player);
                }
            } else {
                NetworkHandler.sendToPlayer(new WirelessDataUpdatePacket(data, isAdd), player);
            }
        }
    }
}
