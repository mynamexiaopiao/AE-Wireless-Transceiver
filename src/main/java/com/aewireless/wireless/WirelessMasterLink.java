package com.aewireless.wireless;

public class WirelessMasterLink {
    private final IWirelessEndpoint host;
    private String  frequency;
    private boolean registered;

    public WirelessMasterLink(IWirelessEndpoint host) {
        this.host = host;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        if (frequency == null)return;

        if (registered) {
//            unregister();
            if (!registered || frequency .isEmpty()) return;
            WirelessData.DATA.put(this.frequency, null);
            registered = false;
        }
        this.frequency = frequency;
        if (!frequency.isEmpty() && !host.isEndpointRemoved() && WirelessData.DATA.containsKey(frequency)) {
            register();
        }

    }


    public boolean register() {
        if (frequency.isEmpty()) return false;
        boolean is = WirelessData.addData(frequency, host);
        this.registered = is;
        return is;
    }


    public void unregister() {
        if (!registered || frequency .isEmpty()) return;
        WirelessData.DATA.put(frequency, null);
        registered = false;
    }

    public void realUnregister() {
        registered = false;
        this.frequency = null;
    }
}
