package com.aewireless.network;

import com.aewireless.gui.wireless.WirelessMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MenuDataPacket {
    private String frequency;
    private Boolean mode;

    public MenuDataPacket(String frequency, Boolean mode) {
        this.frequency = frequency;
        this.mode = mode;
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeUtf(frequency == null ? "" : frequency);
        buf.writeBoolean(mode);
    }

    public static MenuDataPacket decode(FriendlyByteBuf buf) {
        String freq = buf.readUtf();
        Boolean mode = buf.readBoolean();
        return new MenuDataPacket(
                freq.isEmpty() ? "" : freq,
                mode
        );
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu instanceof WirelessMenu wirelessMenu) {
                    // 更新频率（如果提供了有效频率）
                    if (frequency != null && !frequency.isEmpty()) {
                        wirelessMenu.setFrequency(frequency);
                    }

                    // 更新模式
                    wirelessMenu.setMode(mode);

                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
