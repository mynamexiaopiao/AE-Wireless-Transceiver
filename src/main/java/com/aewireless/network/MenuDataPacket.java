package com.aewireless.network;

import com.aewireless.gui.wireless.WirelessMenu;
import com.aewireless.wireless.WirelessData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MenuDataPacket {
    private String frequency;
    private Boolean mode;
    private Set<String> channels;
    private ActionType action;

    public enum ActionType {
        SET_FREQUENCY_MODE,
        ADD_CHANNEL,
        REMOVE_CHANNEL
    }

    public MenuDataPacket(String frequency, Boolean mode) {
        this.frequency = frequency;
        this.mode = mode;
        this.action = ActionType.SET_FREQUENCY_MODE;
    }

    public MenuDataPacket(String channel, ActionType action) {
        this.channels = new HashSet<>();
        this.channels.add(channel);
        this.action = action;
    }

    public MenuDataPacket(Set<String> channels, ActionType action) {
        this.channels = channels;
        this.action = action;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action.name());

        switch (action) {
            case SET_FREQUENCY_MODE:
                buf.writeUtf(frequency == null ? "" : frequency);
                buf.writeBoolean(mode != null && mode);
                break;
            case ADD_CHANNEL:
            case REMOVE_CHANNEL:
                buf.writeInt(channels.size());
                for (String channel : channels) {
                    buf.writeUtf(channel == null ? "" : channel);
                }
                break;
        }
    }

    public static MenuDataPacket decode(FriendlyByteBuf buf) {
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
                            for (String channel : channels) {
                                if (channel != null && !channel.isEmpty() &&
                                        !WirelessData.DATA.containsKey(channel)) {
                                    WirelessData.DATA.put(channel, null);
                                }
                            }
                        }
                        break;
                    case REMOVE_CHANNEL:
                        // 在服务端删除频道
                        if (channels != null && !channels.isEmpty()) {
                            for (String channel : channels) {
                                if (channel != null && !channel.isEmpty()) {
                                    WirelessData.DATA.remove(channel);
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
