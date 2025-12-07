package com.aewireless.network.packet;

import com.aewireless.AeWireless;
import com.aewireless.gui.wireless.WirelessMenu;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record MenuDataPacket(String frequency, Boolean mode, Set<String> channels, ActionType action, UUID uuid) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("aewireless", "menu_data");
    public static final Type<MenuDataPacket> TYPE = new Type<>(ID);

    public enum ActionType {
        SET_FREQUENCY_MODE,
        ADD_CHANNEL,
        REMOVE_CHANNEL
    }

    public MenuDataPacket(String frequency, Boolean mode, UUID uuid) {
        this(frequency, mode, null, ActionType.SET_FREQUENCY_MODE, uuid);
    }

    public MenuDataPacket(String channel, ActionType action, UUID uuid) {
        this(null, null, Set.of(channel), action, uuid);
    }

    public MenuDataPacket(Set<String> channels, ActionType action, UUID uuid) {
        this(null, null, channels, action, uuid);
    }

    public static final StreamCodec<FriendlyByteBuf, MenuDataPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {
                buf.writeUtf(packet.action().name());

                switch (packet.action()) {
                    case SET_FREQUENCY_MODE:
                        buf.writeUtf(packet.frequency() == null ? "" : packet.frequency());
                        buf.writeBoolean(packet.mode() != null && packet.mode());
                        buf.writeUUID(packet.uuid()); // 写入UUID
                        break;
                    case ADD_CHANNEL:
                    case REMOVE_CHANNEL:
                        buf.writeInt(packet.channels() != null ? packet.channels().size() : 0);
                        if (packet.channels() != null) {
                            for (String channel : packet.channels()) {
                                buf.writeUtf(channel == null ? "" : channel);
                            }
                        }
                        buf.writeUUID(packet.uuid()); // 写入UUID
                        break;
                }
            }, buf -> {
                ActionType action = ActionType.valueOf(buf.readUtf());

                switch (action) {
                    case SET_FREQUENCY_MODE:
                        String freq = buf.readUtf();
                        Boolean mode = buf.readBoolean();
                        UUID uuid = buf.readUUID(); // 读取UUID
                        return new MenuDataPacket(freq, mode, uuid);
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
                        UUID uuid1 = buf.readUUID(); // 读取UUID
                        return new MenuDataPacket(channels, action, uuid1);
                    default:
                        return new MenuDataPacket("", false, UUID.randomUUID());
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
                            // 使用服务端计算出的队伍/网络拥有者 UUID，拒绝信任客户端发来的 uuid 字段
                            UUID teamId = AeWireless.IS_FTB_TEAMS_LOADED ?
                                    WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID()) :
                                    AeWireless.PUBLIC_NETWORK_UUID;
                            for (String channel : packet.channels()) {
                                if (channel != null && !channel.isEmpty() &&
                                        !WirelessData.containsData(channel, teamId)) {
                                    WirelessData.addData(channel, teamId, null);
                                }
                            }
                        }
                        break;
                    case REMOVE_CHANNEL:
                        // 在服务端删除频道
                        if (packet.channels() != null && !packet.channels().isEmpty()) {
                            // 使用服务端计算出的队伍/网络拥有者 UUID
                            UUID teamId = AeWireless.IS_FTB_TEAMS_LOADED ?
                                    WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID()) :
                                    AeWireless.PUBLIC_NETWORK_UUID;
                            for (String channel : packet.channels()) {
                                if (channel != null && !channel.isEmpty()) {
                                    WirelessData.removeData(channel, teamId);
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
