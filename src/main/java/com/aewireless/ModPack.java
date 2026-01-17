package com.aewireless;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ModPack {
    public void load() throws IOException {

        InputStream zipStream = getClass().getResourceAsStream("/assets/aewireless.zip");
        if (zipStream == null) {
            throw new IllegalStateException("ZIP 资源未找到！");
        }
        Path tempZip = Files.createTempFile("aewireless", ".zip");
        Files.copy(zipStream, tempZip, StandardCopyOption.REPLACE_EXISTING);


        Pack.ResourcesSupplier resources = id -> new FilePackResources(id, tempZip.toFile(), true);
        Minecraft.getInstance().getResourcePackRepository().addPackFinder(map -> {
            final Pack packInfo = Pack.create(AeWireless.MOD_ID,
                    Component.literal("AeWireless Builtin"),
                    false,
                    resources,
                    Pack.readPackInfo(AeWireless.MOD_ID, resources),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    false,
                    PackSource.DEFAULT
            );

            map.accept(packInfo);
        });
    }
}
