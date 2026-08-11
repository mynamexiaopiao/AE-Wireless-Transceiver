package com.aewireless.client;

import com.aewireless.AeWireless;
import com.aewireless.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

@EventBusSubscriber(modid = AeWireless.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class WirelessConnectorClientEvents {
    private static final int ROW_HEIGHT = 12;
    private static final int LIST_WIDTH = 104;
    private static final int PANEL_PADDING = 5;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFC7C8D2;

    private WirelessConnectorClientEvents() {
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        WirelessConnectorChannelState.clearChannels();
        WirelessConnectorChannelState.requestChannels();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        WirelessConnectorChannelState.clearChannels();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        if (WirelessConnectorChannelState.getHeldConnector(minecraft) == null) return;
        if (!WirelessConnectorChannelState.getChannels().isEmpty()) return;

        WirelessConnectorChannelState.requestChannels();
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!ModConfig.shiftScrollChannelSwitch()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isShiftKeyDown()) return;

        if (WirelessConnectorChannelState.cycleSelectedChannel(minecraft, event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!WirelessConnectorChannelState.shouldShowOverlay()) return;

        Minecraft minecraft = Minecraft.getInstance();
        WirelessConnectorChannelState.HeldConnector held = WirelessConnectorChannelState.getHeldConnector(minecraft);
        if (minecraft.player == null || held == null) return;

        renderChannelList(event.getGuiGraphics(), minecraft, held.stack());
    }

    private static void renderChannelList(GuiGraphics graphics, Minecraft minecraft, ItemStack stack) {
        List<String> channels = WirelessConnectorChannelState.getChannels();
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int hotbarLeft = (screenWidth - 182) / 2;
        int x = Math.max(6, hotbarLeft - LIST_WIDTH - 8);
        int maxRows = Math.max(3, Math.min(5, (screenHeight - 62) / ROW_HEIGHT));
        int selectedIndex = channels.indexOf(WirelessConnectorChannelState.getFrequency(stack));
        int rows = Math.min(channels.size(), maxRows);
        int start = 0;

        if (selectedIndex >= 0 && channels.size() > rows) {
            start = selectedIndex - rows / 2;
            if (start < 0) start = 0;
            if (start + rows > channels.size()) start = channels.size() - rows;
        }

        int y = screenHeight - 20 - rows * ROW_HEIGHT;
        String title = Component.translatable("tooltip.aewireless_connect.channel_list").getString();
        int panelTop = y - 20;
        int panelBottom = y + Math.max(rows, 3) * ROW_HEIGHT + PANEL_PADDING;
        int panelRight = x + LIST_WIDTH;

        graphics.fill(x, panelTop, panelRight, panelBottom, 0xFF4E4E68);
        graphics.fill(x + 1, panelTop + 1, panelRight - 1, panelBottom - 1, WHITE);
        graphics.fill(x + 2, panelTop + 2, panelRight - 2, panelBottom - 2, GRAY);

        int titleX = x + 7;
        int titleY = panelTop + 7;
        graphics.drawString(font, title, titleX, titleY, 0xFF4E4E68, false);

        int listX = x + 6;
        int listTop = y - 2;
        int listRight = panelRight - 6;
        int listBottom = panelBottom - 5;
        graphics.fill(listX - 1, listTop - 1, listRight + 1, listBottom + 1, WHITE);
        graphics.fill(listX, listTop, listRight, listBottom, 0xFF4E4E68);
        graphics.fill(listX + 1, listTop + 1, listRight - 1, listBottom - 1, GRAY);

        if (channels.isEmpty()) {
            graphics.drawString(font, Component.translatable("tooltip.aewireless_connect.channel_list_empty"), listX + 6, listTop + 5, WHITE, false);
            return;
        }

        for (int i = 0; i < rows; i++) {
            int channelIndex = start + i;
            String channel = channels.get(channelIndex);
            int rowY = listTop + 3 + i * ROW_HEIGHT;
            boolean selected = channelIndex == selectedIndex;
            if (selected) {
                graphics.fill(listX + 3, rowY - 1, listRight - 3, rowY + ROW_HEIGHT - 1, WHITE);
                graphics.fill(listX + 4, rowY, listRight - 4, rowY + ROW_HEIGHT - 2, GRAY);
            }
            String display = font.plainSubstrByWidth(channel, LIST_WIDTH - 30);
            graphics.drawString(font, display, listX + 7, rowY + 1, selected ? WHITE : 0xFF4E4E68, false);
        }

        if (start > 0) {
            graphics.drawString(font, "...", listRight - 15, listTop + 1, WHITE, false);
        }
        if (start + rows < channels.size()) {
            graphics.drawString(font, "...", listRight - 15, listBottom - 9, WHITE, false);
        }
    }
}
