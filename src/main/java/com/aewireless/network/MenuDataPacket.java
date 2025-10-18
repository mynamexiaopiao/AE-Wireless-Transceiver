package com.aewireless.network;

import com.aewireless.gui.wireless.WirelessMenu;
import com.aewireless.wireless.WirelessData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record MenuDataPacket(String frequency, Boolean mode, Set<String> channels, ActionType action) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "menu_data");

    public static final Type<MenuDataPacket> TYPE = new Type<>(ID);

    public enum ActionType {
        SET_FREQUENCY_MODE,
        ADD_CHANNEL,
        REMOVE_CHANNEL
    }

    public MenuDataPacket(String frequency, Boolean mode) {
        this(frequency, mode, null, ActionType.SET_FREQUENCY_MODE);
    }

    public MenuDataPacket(String channel, ActionType action) {
        this(null, null, Set.of(channel), action);
    }

    public MenuDataPacket(Set<String> channels, ActionType action) {
        this(null, null, channels, action);
    }

    public static final StreamCodec<FriendlyByteBuf, MenuDataPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeUtf(packet.action().name());

                switch (packet.action()) {
                    case SET_FREQUENCY_MODE:
                        buf.writeUtf(packet.frequency() == null ? "" : packet.frequency());
                        buf.writeBoolean(packet.mode() != null && packet.mode());
                        break;
                    case ADD_CHANNEL:
                    case REMOVE_CHANNEL:
                        buf.writeInt(packet.channels().size());
                        for (String channel : packet.channels()) {
                            buf.writeUtf(channel == null ? "" : channel);
                        }
                        break;
                }
            }, buf -> {
                ActionType action = ActionType.valueOf(buf.readUtf());

                switch (action) {
                    case SET_FREQUENCY_MODE:
                        String freq = buf.readUtf();
                        Boolean mode = buf.readBoolean();
                        return new MenuDataPacket(freq, mode);
                    case ADD_CHANNEL:
                    case REMOVE_CHANNEL:
                        int size = buf.readInt();
                        Set<String> channels = new HashSet<>();
                        for (int i = 0; i < size; i++) {
                            String channel = buf.readUtf();
                            if (!channel.isEmpty()) {
                                channels.add(channel);
                            }
                        }
                        return new MenuDataPacket(channels, action);
                    default:
                        return new MenuDataPacket("", false);
                }
            });

    public static void handle(MenuDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                switch (packet.action()) {
                    case SET_FREQUENCY_MODE:
                        AbstractContainerMenu menu = player.containerMenu;
                        if (menu instanceof WirelessMenu wirelessMenu) {
                            if (packet.frequency() != null) {
                                wirelessMenu.setFrequency(packet.frequency());
                            }
                            if (packet.mode() != null) {
                                wirelessMenu.setMode(packet.mode());
                            }
                        }
                        break;
                    case ADD_CHANNEL:
                        // 在服务端添加频道
                        if (packet.channels() != null && !packet.channels().isEmpty()) {
                            for (String channel : packet.channels()) {
                                if (channel != null && !channel.isEmpty() &&
                                        !WirelessData.DATA.containsKey(channel)) {
                                    WirelessData.DATA.put(channel, null);
                                }
                            }
                        }
                        break;
                    case REMOVE_CHANNEL:
                        // 在服务端删除频道
                        if (packet.channels() != null && !packet.channels().isEmpty()) {
                            for (String channel : packet.channels()) {
                                if (channel != null && !channel.isEmpty()) {
                                    WirelessData.DATA.remove(channel);
                                }
                            }
                        }
                        break;
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
