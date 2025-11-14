package com.aewireless.wireless;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    }



    public static synchronized IWirelessEndpoint getData(String s , UUID uuid){
        if (s.isEmpty())return null;
        return DATA.get(new Key(s, uuid));
    }

    public record Key(String string , UUID uuid){}


}
