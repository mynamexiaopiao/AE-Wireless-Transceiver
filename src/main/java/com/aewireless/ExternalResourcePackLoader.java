package com.aewireless;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@EventBusSubscriber(modid = AeWireless.MOD_ID)
public class ExternalResourcePackLoader {

    @SubscribeEvent
    public static void addExternalPackFinder(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            // 1. 从模组JAR内提取ZIP到游戏目录
            Path gameDir = FMLPaths.GAMEDIR.get();
            Path resourcepacksDir = gameDir.resolve("resourcepacks");

            try {
                // 确保目录存在
                Files.createDirectories(resourcepacksDir);

                // ZIP在JAR内的路径
                String zipPathInJar = "/assets/aewireless/aewireless_resources.zip";

                // 目标路径：游戏目录/resourcepacks/aewireless_resources.zip
                Path targetZip = resourcepacksDir.resolve("aewireless_resources.zip");

                // 2. 提取ZIP文件（如果不存在）
                if (!Files.exists(targetZip)) {
                    try (var inputStream = ExternalResourcePackLoader.class.getResourceAsStream(zipPathInJar)) {
                        if (inputStream != null) {
                            Files.copy(inputStream, targetZip);
                            System.out.println("已提取材质包到: " + targetZip);
                        } else {
                            System.err.println("JAR内未找到材质包: " + zipPathInJar);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("设置外部材质包失败: " + e.getMessage());
            }
        }
    }
}