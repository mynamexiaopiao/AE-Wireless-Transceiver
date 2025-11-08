package com.aewireless.gui.wireless;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.*;
import appeng.core.AppEng;
import com.aewireless.AeWireless;
import com.aewireless.ModConfig;
import com.aewireless.gui.RenderButton;
import com.aewireless.network.MenuDataPacket;
import com.aewireless.network.NetworkHandler;
import com.aewireless.wireless.WirelessData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WirelessScreen extends AEBaseScreen<WirelessMenu> {
    Scrollbar scrollbar;
    AETextField input;

    //可见行数
    private final int visibleRows = 8;
    //每行的像素
    protected static final Button.CreateNarration DEFAULT_NARRATION = (supplier) -> (MutableComponent)supplier.get();
    private final int rowHeight = 12;
    private final ArrayList<String> allDataRows = new ArrayList<>();
    private final ArrayList<String> filteredDataRows = new ArrayList<>(); // 新增：用于存储过滤后的数据


    private int highlightedRowIndex = -1;
    private int highlightColor = 0x3300FF00;
    private int highlightBorderColor = 0xFF00CC00;

    private int listX;
    private int listY;
    private final int listWidth = 80; //列表宽度
    private int listHeight; // 列表总高度

    private final float fontScale = 1f;

    private RenderButton addButton;
    private RenderButton removeButton;
    private RenderButton modeButton;
    private RenderButton disconnect;

    @SuppressWarnings("all")
    public WirelessScreen(WirelessMenu menu, Inventory playerInventory, Component title, ScreenStyle style){
        super(menu, playerInventory, title, style);


        try{
            String highlightColor1 = ModConfig.highlightColor;
            String highlightBorderColor1 = ModConfig.highlightBorderColor;
            int i = parseHexColor(highlightColor1);
            int i1 = parseHexColor(highlightBorderColor1);
            highlightColor = i;
            highlightBorderColor = i1;
        }catch (Exception e){
            e.printStackTrace();
            highlightColor = 0x3300FF00;
            highlightBorderColor = 0xFF00CC00;
        }


        scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.Style.create(
                AppEng.makeId("textures/guis/modes.png"),
                3,
                10,
                242, 0,
                249, 0));
        input = this.widgets.addTextField("input");

        input.setFilter(s -> {
            return Minecraft.getInstance().font.width(s) <= listWidth;
        });

        input.setPlaceholder(Component.translatable("gui.wireless.input.placeholder"));
        input.setResponder(this::onInputChanged);


        try {
            Class<? extends AETextField> aClass = input.getClass();
            Method setTextColor = aClass.getMethod("setNew", boolean.class);

            setTextColor.invoke(input, true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        addButton = new RenderButton(0, 0, 40, 15,
                Component.translatable("gui.wireless.add"),
                arg -> {
                    showInputScreen();
                },
                DEFAULT_NARRATION) ;

        removeButton = new RenderButton(0, 0, 40, 15,
                Component.translatable("gui.wireless.remove"),
                arg ->{
                    if (this.getMenu().getFrequency() != null && !this.getMenu().getFrequency().equals("")){
                    removeDataRow(this.getMenu().getFrequency());
                    }
                },
                DEFAULT_NARRATION) {};

        modeButton = new RenderButton(0, 0, 40, 15,
                Component.translatable("gui.wireless.mode.toggle"),
                arg -> {
                    NetworkHandler.sendToServer(new MenuDataPacket(null, !this.getMenu().isMode()));
                    this.getMenu().setMode(!this.getMenu().isMode());
                },
                DEFAULT_NARRATION) {};

        disconnect = new RenderButton(0, 0, 40, 15,
                Component.translatable("gui.wireless.mode.disconnect"),
                arg -> {
                    NetworkHandler.sendToServer(new MenuDataPacket("", this.getMenu().isMode()));
                },
                DEFAULT_NARRATION) {};

        refreshList();
    }

    private int parseHexColor(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            throw new IllegalArgumentException("Color string cannot be null or empty");
        }

        // 移除可能存在的 "0x" 或 "#" 前缀
        if (hexColor.startsWith("0x")) {
            hexColor = hexColor.substring(2);
        } else if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }

        // 验证字符串是否为有效的十六进制格式
        if (!hexColor.matches("[0-9A-Fa-f]+")) {
            throw new IllegalArgumentException("Invalid hex color format: " + hexColor);
        }

        // 解析十六进制字符串
        return (int) Long.parseLong(hexColor, 16);
    }

    @Override
    public void init() {
        super.init();
        listHeight = visibleRows * rowHeight;
        listX = (this.width - listWidth) /   2  -  50;
        // 将整个框向下移一点
        listY = this.height - this.topPos - listHeight - 35;

        // 添加按钮到屏幕，调整到合适位置
        int buttonY = this.getGuiTop() + 16;
        modeButton.setPosition(this.getGuiLeft() + listWidth + 20, buttonY + 110);
        addButton.setPosition(this.getGuiLeft()+listWidth + 20, buttonY + 90);
        disconnect.setPosition(this.getGuiLeft() + listWidth + 65, buttonY + 110);
        removeButton.setPosition(this.getGuiLeft() + listWidth + 65, buttonY + 90);

        this.addRenderableWidget(addButton);
        this.addRenderableWidget(removeButton);
        this.addRenderableWidget(modeButton);
        this.addRenderableWidget(disconnect);
    }

    private void refreshList() {
        resetScrollbar();
    }

    private void resetScrollbar() {
        scrollbar.setHeight(listHeight);
        int maxScroll = Math.max(0, filteredDataRows.size() - visibleRows);
        scrollbar.setRange(0, maxScroll, 1);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        updateData();
        refreshList();
        updateHoveredRow(mouseX, mouseY);

        renderListBackground(guiGraphics);
        renderHighlightedRow(guiGraphics);
        renderHoveredRow(guiGraphics);
        renderListContent(guiGraphics);
        renderScrollbarBackground(guiGraphics);
        renderMode(guiGraphics);
    }

    private int hoveredRowIndex = -1;


    private void updateHoveredRow(double mouseX, double mouseY) {
        hoveredRowIndex = -1;

        if (isMouseOverList(mouseX, mouseY)) {
            int relativeY = (int) (mouseY - listY);
            int hoveredIndex = (relativeY / rowHeight) + (int) scrollbar.getCurrentScroll();

            // 使用过滤后的列表而不是原始列表
            if (hoveredIndex >= 0 && hoveredIndex < filteredDataRows.size()) {
                // 我们需要找到在原始列表中的索引位置
                String hoveredItem = filteredDataRows.get(hoveredIndex);
                hoveredRowIndex = allDataRows.indexOf(hoveredItem);
            }
        }
    }


    // 修改：渲染悬停行的方法
    private void renderHoveredRow(GuiGraphics guiGraphics) {
        if (hoveredRowIndex != -1 && hoveredRowIndex != highlightedRowIndex) {
            // 查找悬停项在过滤列表中的位置
            if (hoveredRowIndex < allDataRows.size()) {
                String hoveredItem = allDataRows.get(hoveredRowIndex);
                int filteredIndex = filteredDataRows.indexOf(hoveredItem);

                if (filteredIndex != -1) {
                    final int scrollLevel = scrollbar.getCurrentScroll();

                    if (filteredIndex >= scrollLevel && filteredIndex < scrollLevel + visibleRows) {
                        int visibleIndex = filteredIndex - scrollLevel;
                        int yPos = listY + (visibleIndex * rowHeight);

                        // 绘制半透明的悬停效果
                        guiGraphics.fill(listX + 1, yPos, listX + listWidth - 1, yPos + rowHeight, 0x33FFFFFF);
                    }
                }
            }
        }
    }


    private void renderScrollbarBackground(GuiGraphics guiGraphics) {
        // 滚动条背景框的位置和尺寸
        int scrollbarX = listX + listWidth; // 滚动条在列表右侧
        int scrollbarY = listY;
        int scrollbarWidth = 3; // 滚动条宽度
        int scrollbarHeight = listHeight;


        // 绘制滚动条边框
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + 1, 0xFFAAAAAA); // 上边框
        guiGraphics.fill(scrollbarX, scrollbarY + scrollbarHeight - 1, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0xFFAAAAAA); // 下边框
        guiGraphics.fill(scrollbarX + scrollbarWidth - 1, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0xFFAAAAAA); // 右边框

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


        // 更新过滤后的列表
        filterDataRows(input.getValue());

        this.highlightedRowIndex =  keys.indexOf(this.getMenu().getFrequency());

    }

    private void renderListBackground(GuiGraphics guiGraphics) {
        // 使用更现代的半透明背景
        guiGraphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0x88000000);

        // 改进边框颜色和样式
        guiGraphics.fill(listX, listY, listX + listWidth, listY + 1, 0xFFAAAAAA); // 上边框
        guiGraphics.fill(listX, listY + listHeight - 1, listX + listWidth, listY + listHeight, 0xFFAAAAAA); // 下边框
        guiGraphics.fill(listX, listY, listX + 1, listY + listHeight, 0xFFAAAAAA); // 左边框
        guiGraphics.fill(listX + listWidth - 1, listY, listX + listWidth, listY + listHeight, 0xFFAAAAAA); // 右边框
    }

    private void renderListContent(GuiGraphics guiGraphics) {
        final int scrollLevel = (int) scrollbar.getCurrentScroll();
        final int fontColor = 0xE0E0E0; // 更亮的字体颜色

        guiGraphics.pose().pushPose();

        for (int i = 0; i < visibleRows; i++) {
            int dataIndex = scrollLevel + i;
            // 使用过滤后的列表而不是原始列表
            if (dataIndex >= 0 && dataIndex < filteredDataRows.size()) {
                String text = filteredDataRows.get(dataIndex);
                int yPos = listY + (i * rowHeight);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(fontScale, fontScale, 1.0f);

                float scaledX = (listX + 5) / fontScale; // 增加左边距
                float scaledY = (yPos + 4) / fontScale;  // 调整垂直居中

                String displayText = Minecraft.getInstance().font.plainSubstrByWidth(
                        text,
                        (int) ((listWidth - 10) / fontScale) // 减少可用宽度以适应边距
                );

                if (!displayText.equals(text) && displayText.length() > 3) {
                    displayText = displayText.substring(0, displayText.length() ) + "...";
                }

                // 添加阴影效果的文本
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


    // 修改：渲染高亮行的方法
    private void renderHighlightedRow(GuiGraphics guiGraphics) {
        if (highlightedRowIndex != -1 && allDataRows.size() > highlightedRowIndex) {
            // 查找高亮项在过滤列表中的位置
            String highlightedItem = allDataRows.get(highlightedRowIndex);
            int filteredIndex = filteredDataRows.indexOf(highlightedItem);

            if (filteredIndex != -1) {
                final int scrollLevel = scrollbar.getCurrentScroll();

                if (filteredIndex >= scrollLevel && filteredIndex < scrollLevel + visibleRows) {
                    int visibleIndex = filteredIndex - scrollLevel;
                    int yPos = listY + (visibleIndex * rowHeight);

                    // 改进高亮效果，使用渐变色和更明显的边框
                    guiGraphics.fill(listX + 1, yPos, listX + listWidth - 1, yPos + rowHeight, highlightColor);

                    // 绘制更粗的边框
                    guiGraphics.fill(listX + 1, yPos, listX + listWidth - 1, yPos + 1, highlightBorderColor);
                    guiGraphics.fill(listX + 1, yPos + rowHeight - 1, listX + listWidth - 1, yPos + rowHeight, highlightBorderColor);
                    guiGraphics.fill(listX + 1, yPos, listX + 2, yPos + rowHeight, highlightBorderColor);
                    guiGraphics.fill(listX + listWidth - 2, yPos, listX + listWidth - 1, yPos + rowHeight, highlightBorderColor);
                }
            }
        }
    }


    public void clearHighlight() {
        highlightedRowIndex = -1;
        NetworkHandler.sendToServer(new MenuDataPacket(null, this.getMenu().isMode()));
    }

    private void renderMode(GuiGraphics guiGraphics){
        boolean mode = this.menu.isMode();
        boolean online = this.getMenu().isOnline();
        String modeText = mode ? " true" : " false";
        String modeText1 = online ? " true" : " false";



        int baseX = this.getGuiLeft() + 100;
        int baseY = this.getGuiTop() + 60;

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("gui.wireless.mode").append(":"),
                baseX,
                baseY,
                0xFFFFFF,
                false
        );

        // 用不同颜色显示模式状态
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(modeText),
                baseX + Minecraft.getInstance().font.width(Component.translatable("gui.wireless.mode").append(":").getString()),
                baseY,
                mode ? 0x00FF00 : 0xFF0000,
                false
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("gui.wireless.mode.channel").append(": ").append(highlightedRowIndex == -1 ? "" : allDataRows.get(highlightedRowIndex)),
                baseX,
                baseY - 20,
                0xFFFFFF,
                false
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("gui.wireless.mode.connect").append(": "),
                baseX,
                baseY + 20,
                0xFFFFFF,
                false
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(modeText1),
                baseX + Minecraft.getInstance().font.width(Component.translatable("gui.wireless.mode.connect").append(":").getString()),
                baseY + 20,
                online ? 0x00FF00 : 0xFF0000,
                false
        );
    }


    // 修改：鼠标点击处理方法
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverList(mouseX, mouseY)) {
            int relativeY = (int) (mouseY - listY);
            int clickedRowIndex = (relativeY / rowHeight) + (int) scrollbar.getCurrentScroll();

            // 使用过滤后的列表而不是原始列表
            if (clickedRowIndex >= 0 && clickedRowIndex < filteredDataRows.size()) {
                // 添加视觉反馈
                // 注意：我们需要找到在原始列表中的索引位置
                String clickedItem = filteredDataRows.get(clickedRowIndex);
                highlightedRowIndex = allDataRows.indexOf(clickedItem);

                NetworkHandler.sendToServer(new MenuDataPacket(clickedItem, this.getMenu().isMode()));
                this.getMenu().setFrequency(clickedItem);

                if (this.getMenu().blockEntity.getLevel() != null) {
                    this.getMenu().blockEntity.getLevel().playLocalSound(
                            this.getMenu().blockEntity.getBlockPos(),
                            SoundEvents.UI_BUTTON_CLICK.get(),
                            SoundSource.MASTER,
                            1.0f,
                            1.0f,
                            false
                    );
                }

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


        // 发送到服务端
        if (isPlayingOnServer())NetworkHandler.sendToServer(new MenuDataPacket(data, MenuDataPacket.ActionType.ADD_CHANNEL));

        WirelessData.DATA.put(data, null);

        updateData();
        refreshList();
    }
    public void showConfirmDeleteScreen(String itemToDelete, Runnable onConfirm) {
        Minecraft.getInstance().pushGuiLayer(new ConfirmDeleteScreen(itemToDelete, onConfirm));
    }

    public void showInputScreen() {
        Minecraft.getInstance().pushGuiLayer(new InputChannelNameScreen(this));
    }

    public void removeDataRow(String data) {
        showConfirmDeleteScreen(data, () -> {
            allDataRows.remove(data);

            // 发送到服务端
            if (isPlayingOnServer()) NetworkHandler.sendToServer(new MenuDataPacket(data, MenuDataPacket.ActionType.REMOVE_CHANNEL));

            WirelessData.removeData(data);

            updateData();
            refreshList();
        });
    }

    private void onInputChanged(String text) {
        filterDataRows(text);
    }

    // 新增：根据搜索文本过滤数据行
    private void filterDataRows(String searchText) {
        filteredDataRows.clear();

        if (searchText == null || searchText.isEmpty() || searchText.trim().isEmpty()) {
            // 如果搜索文本为空，则显示所有行
            filteredDataRows.addAll(allDataRows);
        } else {
            // 根据搜索文本过滤行
            String lowerSearchText = searchText.toLowerCase();
            for (String row : allDataRows) {
                if (row.toLowerCase().contains(lowerSearchText)) {
                    filteredDataRows.add(row);
                }
            }
        }

        // 重置滚动条以适应新的列表大小
        resetScrollbar();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isPlayingOnServer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getSingleplayerServer() == null && mc.getCurrentServer() != null;
    }
}
