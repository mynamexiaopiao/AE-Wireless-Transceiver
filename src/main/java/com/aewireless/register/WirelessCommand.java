package com.aewireless.register;

import com.aewireless.wireless.block.LevelManage;
import com.aewireless.wireless.block.WirelessBlockManage;
import com.aewireless.wireless.block.link.WirelessBlockLink;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
@Mod.EventBusSubscriber
public class WirelessCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        WirelessCommand.register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("aewireless")
                        .requires(source -> source.hasPermission(2)) // 所有玩家都可使用
                        .then(Commands.literal("clear")
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    if (!source.getLevel().isClientSide) {
                                        LevelManage.setIsLoad(false);

                                        Iterator<Map.Entry<WirelessBlockManage.PosAndDirection, WirelessBlockLink>> iterator =
                                                WirelessBlockManage.getBlockPosList().entrySet().iterator();

                                        while (iterator.hasNext()) {
                                            Map.Entry<WirelessBlockManage.PosAndDirection, WirelessBlockLink> entry = iterator.next();
                                            WirelessBlockLink wirelessBlockLink = entry.getValue();

                                            if (wirelessBlockLink == null) {
                                                iterator.remove(); // 可选：同时从迭代器中移除
                                            }
                                        }

                                        source.sendSuccess(
                                                new Supplier<Component>() {
                                                    @Override
                                                    public Component get() {
                                                        return Component.translatable("commands.aewireless.clear");
                                                    }
                                                },
                                                false
                                        );

                                        LevelManage.setIsLoad(true);

                                        return 1;

                                    }
                                    return 1;
                                })
                        )
        );
    }
}
