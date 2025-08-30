package com.aewireless.gui.wireless;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.*;
import com.aewireless.ModIcon;
import com.aewireless.ModIconButton;
import com.aewireless.network.MenuDataPacket;
import com.aewireless.network.NetworkHandler;
import com.aewireless.wireless.WirelessData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class WirelessScreen extends AEBaseScreen<WirelessMenu> {
    Scrollbar scrollbar;
    AETextField input;

    //可见行数
    private final int visibleRows = 5;
    //每行的像素

    private final int rowHeight = 12;
    private final ArrayList<String> allDataRows = new ArrayList<>();


    private int highlightedRowIndex = -1;
    private final int highlightColor = 0x3366CCFF; // 高亮背景色（淡蓝色）
    private final int highlightBorderColor = 0xFF3399FF; // 高亮边框色（亮蓝色）

    private int listX;
    private int listY;
    private final int listWidth = 130; //列表宽度
    private int listHeight; // 列表总高度

    // 字体缩放
    private final float fontScale = 1f; // 字体缩放比例

    public WirelessScreen(WirelessMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);


        scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.SMALL);
        input = this.widgets.addTextField("input");
        Button addButton = new ModIconButton(arg -> addDataRow(input.getValue())) {
            @Override
            protected ModIcon getIcon() {
                return ModIcon.ADD;
            }
        };
        Button removeButton = new ModIconButton(arg -> removeDataRow(input.getValue())) {
            @Override
            protected ModIcon getIcon() {
                return ModIcon.SUBTRACT;
            }
        };

        Button mode = new ModIconButton(arg -> {
            NetworkHandler.sendToServer(new MenuDataPacket(null, !this.getMenu().isMode()));
            this.getMenu().setMode(!this.getMenu().isMode());
        }) {
            @Override
            protected ModIcon getIcon() {
                return ModIcon.CHANGE;
            }
        };

        this.widgets.add("add", addButton);
        this.widgets.add("remove", removeButton);
        this.widgets.add("mode", mode);


        refreshList();


    }

    @Override
    public void init() {
        super.init();
        listHeight = visibleRows * rowHeight;
        listX = (this.width - listWidth) / 2  - 6;
        listY = this.height - this.topPos - listHeight - 100;

    }

    private void refreshList() {
        resetScrollbar();
    }

    private void resetScrollbar() {
        scrollbar.setHeight(listHeight);
        int maxScroll = Math.max(0, allDataRows.size() - visibleRows);
        scrollbar.setRange(0, maxScroll, 1);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        updateData();
        refreshList();

        renderListBackground(guiGraphics);
        renderHighlightedRow(guiGraphics);
        renderListContent(guiGraphics);
        renderMode(guiGraphics);


    }

    private void updateData(){
        ArrayList<String> keys = new ArrayList<>(WirelessData.DATA.keySet());

        if (highlightedRowIndex != -1) {
            if (highlightedRowIndex >= keys.size()) {
                clearHighlight();
            } else {
                String highlightedKey = allDataRows.size() > highlightedRowIndex ?
                        allDataRows.get(highlightedRowIndex) : null;
                if (highlightedKey != null && !keys.contains(highlightedKey)) {
                    clearHighlight();
                }
            }
        }

        allDataRows.clear();
        allDataRows.addAll(keys);

        this.highlightedRowIndex =  keys.indexOf(this.getMenu().getFrequency());

    }

    private void renderListBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0x66000000);

        guiGraphics.fill(listX, listY, listX + listWidth, listY + 1, 0xFF555555); // 上边框
        guiGraphics.fill(listX, listY + listHeight - 1, listX + listWidth, listY + listHeight, 0xFF555555); // 下边框
        guiGraphics.fill(listX, listY, listX + 1, listY + listHeight, 0xFF555555); // 左边框
        guiGraphics.fill(listX + listWidth - 1, listY, listX + listWidth, listY + listHeight, 0xFF555555); // 右边框
    }

    private void renderListContent(GuiGraphics guiGraphics) {
        final int scrollLevel = (int) scrollbar.getCurrentScroll();
        final int fontColor = 0xCCCCCC;

        guiGraphics.pose().pushPose();

        for (int i = 0; i < visibleRows; i++) {
            int dataIndex = scrollLevel + i;
            if (dataIndex >= 0 && dataIndex < allDataRows.size()) {
                String text = allDataRows.get(dataIndex);

                int yPos = listY + (i * rowHeight);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(fontScale, fontScale, 1.0f);

                float scaledX = (listX + 3) / fontScale;
                float scaledY = (yPos + 3) / fontScale;

                String displayText = Minecraft.getInstance().font.plainSubstrByWidth(
                        text,
                        (int) ((listWidth - 1) / fontScale)
                );

                if (!displayText.equals(text) && displayText.length() > 3) {
                    displayText = displayText.substring(0, displayText.length() - 3) + "...";
                }

                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        Component.literal(displayText),
                        (int) scaledX,
                        (int) scaledY,
                        fontColor,
                        false
                );

                guiGraphics.pose().popPose();
            }
        }

        guiGraphics.pose().popPose();
    }

    private void renderHighlightedRow(GuiGraphics guiGraphics) {
        if (highlightedRowIndex != -1) {
            final int scrollLevel = scrollbar.getCurrentScroll();

            if (highlightedRowIndex >= scrollLevel && highlightedRowIndex < scrollLevel + visibleRows) {
                int visibleIndex = highlightedRowIndex - scrollLevel;
                int yPos = listY + (visibleIndex * rowHeight);

                guiGraphics.fill(listX + 1, yPos, listX + listWidth - 1, yPos + rowHeight, highlightColor);

                guiGraphics.fill(listX + 1, yPos, listX + listWidth - 1, yPos + 1, highlightBorderColor); // 上边框
                guiGraphics.fill(listX + 1, yPos + rowHeight - 1, listX + listWidth - 1, yPos + rowHeight, highlightBorderColor); // 下边框
                guiGraphics.fill(listX, yPos, listX + 1, yPos + rowHeight, highlightBorderColor); // 左边框
                guiGraphics.fill(listX + listWidth - 1, yPos, listX + listWidth, yPos + rowHeight, highlightBorderColor); // 右边框
            }
        }
    }


    public void clearHighlight() {
        highlightedRowIndex = -1;
        NetworkHandler.sendToServer(new MenuDataPacket(null, this.getMenu().isMode()));
    }

    private void renderMode(GuiGraphics guiGraphics){
        boolean mode = this.menu.isMode();
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("gui.wireless.mode").append(": "+mode),
                this.getGuiLeft() + 17,
                this.getGuiTop() + 31,
                0xFFFFFFFF,
                false
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverList(mouseX, mouseY)) {
            int relativeY = (int) (mouseY - listY);
            int clickedRowIndex = (relativeY / rowHeight) + (int) scrollbar.getCurrentScroll();

            if (clickedRowIndex >= 0 && clickedRowIndex < allDataRows.size()) {

                NetworkHandler.sendToServer(new MenuDataPacket(this.allDataRows.get(clickedRowIndex), this.getMenu().isMode()));
                this.getMenu().setFrequency(this.allDataRows.get(clickedRowIndex));

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverList(double mouseX, double mouseY) {
        return mouseX >= listX && mouseX <= listX + listWidth &&
                mouseY >= listY && mouseY <= listY + listHeight;
    }

    public void addDataRow(String data) {
        if (data.isEmpty() || allDataRows.contains(data)) return;
        allDataRows.add(data);

        WirelessData.DATA.put(data, null);


        updateData();
        refreshList();
    }

    public void removeDataRow(String data) {
        allDataRows.remove(data);
        WirelessData.removeData(data);
        updateData();
        refreshList();
    }
}
