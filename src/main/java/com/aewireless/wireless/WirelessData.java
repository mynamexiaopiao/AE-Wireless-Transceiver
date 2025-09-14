package com.aewireless.wireless;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WirelessData {
    public WirelessData() {}

    public static Map<String , IWirelessEndpoint> DATA = new HashMap<>();

    public static synchronized boolean addData(String s, IWirelessEndpoint endpoint){
        Objects.requireNonNull(endpoint, "endpoint");
        if (s.isEmpty())return false;
        if (endpoint.isEndpointRemoved())return false;
        DATA.put(s, endpoint);
        return true;
    }

    public static synchronized void removeData(String s){
        if (s.isEmpty())return;
        DATA.remove(s);
    }

    public static synchronized IWirelessEndpoint getData(String s){
        if (s.isEmpty())return null;
        return DATA.get(s);
    }




}
