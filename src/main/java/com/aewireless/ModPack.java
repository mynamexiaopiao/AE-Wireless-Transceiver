package com.aewireless;

/**
 * Resource pack handling is managed by ExternalResourcePackLoader in 1.21.x.
 * Keep this class as a lightweight shim for older call sites.
 */
public class ModPack {
    public void load() {
        // No-op: ExternalResourcePackLoader handles pack extraction/registration.
    }
}
