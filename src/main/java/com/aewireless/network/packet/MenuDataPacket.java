package com.aewireless.network.packet;

import com.aewireless.gui.wireless.WirelessMenu;
import com.aewireless.wireless.WirelessData;
import com.aewireless.wireless.WirelessTeamUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class MenuDataPacket {
    private String frequency;
    private Boolean mode;
    private Set<String> channels;
    private ActionType action;
    private UUID uuid;

    public enum ActionType {
        SET_FREQUENCY_MODE,
        ADD_CHANNEL,
        REMOVE_CHANNEL
    }

    public MenuDataPacket(String frequency, Boolean mode , UUID uuid) {
        this.frequency = frequency;
        this.mode = mode;
        this.action = ActionType.SET_FREQUENCY_MODE;
        this.uuid = uuid;
    }

    public MenuDataPacket(String channel, ActionType action , UUID uuid) {
        this.channels = new HashSet<>();
        this.channels.add(channel);
        this.action = action;
        this.uuid = uuid;
    }

    public MenuDataPacket(Set<String> channels, ActionType action , UUID uuid) {
        this.channels = channels;
        this.action = action;
        this.uuid = uuid;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action.name());

        switch (action) {
            case SET_FREQUENCY_MODE:
                buf.writeUtf(frequency == null ? "" : frequency);
                buf.writeBoolean(mode != null && mode);
                buf.writeUUID(uuid);
                break;
            case ADD_CHANNEL:
            case REMOVE_CHANNEL:
                buf.writeInt(channels.size());
                for (String channel : channels) {
                    buf.writeUtf(channel == null ? "" : channel);
                }
                buf.writeUUID(uuid);
                break;
        }
    }

    public static MenuDataPacket decode(FriendlyByteBuf buf) {
        ActionType action = ActionType.valueOf(buf.readUtf());

        switch (action) {
            case SET_FREQUENCY_MODE:
                String freq = buf.readUtf();
                Boolean mode = buf.readBoolean();
                UUID uuid = buf.readUUID();
                return new MenuDataPacket(freq, mode , uuid);
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
                UUID uuid1 = buf.readUUID();
                return new MenuDataPacket(channels, action ,uuid1);
            default:
                return new MenuDataPacket("", false , null);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                switch (action) {
                    case SET_FREQUENCY_MODE:
                        AbstractContainerMenu menu = player.containerMenu;
                        if (menu instanceof WirelessMenu wirelessMenu) {
                            if (frequency != null) {
                                wirelessMenu.setFrequency(frequency);
                            }
                            if (mode != null) {
                                wirelessMenu.setMode(mode);
                            }
                        }
                        break;
                    case ADD_CHANNEL:
                        // 在服务端添加频道
                        if (channels != null && !channels.isEmpty()) {
                            // 使用服务端计算出的队伍/网络拥有者 UUID，拒绝信任客户端发来的 uuid 字段
                            UUID teamId = WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID());
                            for (String channel : channels) {
                                if (channel != null && !channel.isEmpty() &&
                                        !WirelessData.containsData(channel , teamId)) {
                                    WirelessData.addData(channel, teamId, null);
                                }
                            }
                        }
                        break;
                    case REMOVE_CHANNEL:
                        // 在服务端删除频道
                        if (channels != null && !channels.isEmpty()) {
                            // 使用服务端计算出的队伍/网络拥有者 UUID
                            UUID teamId = WirelessTeamUtil.getNetworkOwnerUUID(player.getUUID());
                            for (String channel : channels) {
                                if (channel != null && !channel.isEmpty()) {
                                    WirelessData.removeData(channel , teamId);
                                }
                            }
                        }
                        break;
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
