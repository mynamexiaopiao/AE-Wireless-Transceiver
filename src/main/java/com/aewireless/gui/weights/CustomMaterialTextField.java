//#file:C:\Users\Administrator\Desktop\AE-Wireless-Transceiver\src\main\java\com\aewireless\gui\weights\CustomMaterialTextField.java
package com.aewireless.gui.weights;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IResizableWidget;
import appeng.client.gui.widgets.ITooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 参照AETextField，由于这个ae的组件只有在AEBaseScreen中才能使用，我这里要添加一个二级菜单，故重写一份。
 * 貌似高度只能为14，不然会有渲染问题，但也没事，现在能用，有事的时候在修。
 */
public class CustomMaterialTextField extends EditBox implements IResizableWidget, ITooltip {

    private static final int PADDING = 2;
    Font font = Minecraft.getInstance().font;

    private Blitter blitter = Blitter.texture("guis/new_text.png", 128, 128);
    private Component placeholder;
    private List<Component> tooltip = Collections.emptyList();

    public CustomMaterialTextField(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x + PADDING, y + PADDING,
                width - 2 * PADDING - font.width("_"), height - 2 * PADDING,
                narration);
        this.setBordered(false); // 关闭默认边框，以便使用自定义纹理
    }

    // 设置背景纹理
    public void setTexture(String texturePath, int textureWidth, int textureHeight) {
        this.blitter = Blitter.texture(texturePath, textureWidth, textureHeight);
    }

    public void setPlaceholder(Component placeholder) {
        this.placeholder = placeholder;
    }

    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip == null ? Collections.emptyList() : tooltip;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        // 先绘制自定义背景（如果设置了纹理）
        if (this.blitter != null) {
            // 计算实际背景区域（包含padding）
            int left = getX() - PADDING;
            int top = getY() - PADDING;
            int bgWidth = width + 2 * PADDING + font.width("_");
            int bgHeight = height + 2 * PADDING;

            this.blitter.src(0, 0, Math.min(128, bgWidth), Math.min(128, bgHeight))
                    .dest(left, top, bgWidth, bgHeight)
                    .blit(guiGraphics);
        }

        // 绘制文本和光标（EditBox 自身负责）
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 如果没有输入文本且未聚焦，绘制占位符
        if (this.placeholder != null && !this.isFocused() && this.getValue().isEmpty()) {
            int placeholderColor = 0xFF9B9B9B; // 灰色占位符颜色
            guiGraphics.drawString(this.font, this.placeholder, this.getX(), this.getY(), placeholderColor, false);
        }
    }

    // 扩展鼠标点击区域以包含padding
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int left = getX() - PADDING;
        int top = getY() - PADDING;
        int right = left + width + 2 * PADDING + font.width("_");
        int bottom = top + height + 2 * PADDING;

        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 扩展可点击区域以包含padding
        if (isMouseOver(mouseX, mouseY)) {
            mouseX = Math.max(getX(), Math.min(mouseX, getX() + width - 1));
            mouseY = Math.max(getY(), Math.min(mouseY, getY() + height - 1));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public List<Component> getTooltip(int mouseX, int mouseY) {
        if (!this.visible) return Collections.emptyList();

        int left = getX() - PADDING;
        int top = getY() - PADDING;
        int right = left + width + 2 * PADDING + font.width("_");
        int bottom = top + height + 2 * PADDING;

        if (mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom) {
            return this.tooltip;
        }
        return Collections.emptyList();
    }

    // 封装：向上查找并设置整数字段（兼容不同映射/子类）
    private void setIntFieldByName(String name, int value) {
        Class<?> c = this.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.setInt(this, value);
                return;
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            } catch (Exception e) {
                return;
            }
        }
    }

    // IResizableWidget 实现：设置宽高
    @Override
    public void setWidth(int width) {
        // 调整内部宽度以考虑padding和光标宽度
        int internalWidth = width - 2 * PADDING - font.width("_");
        setIntFieldByName("width", Math.max(internalWidth, 0));
    }

    @Override
    public void setHeight(int height) {
        // 调整内部高度以考虑padding
        int internalHeight = height - 2 * PADDING;
        setIntFieldByName("height", Math.max(internalHeight, 0));
    }

    // IResizableWidget: 移动位置（使用 appeng.client.Point）
    @Override
    public void move(Point pos) {
        if (pos == null) return;
        setIntFieldByName("x", pos.getX() + PADDING);
        setIntFieldByName("y", pos.getY() + PADDING);
    }

    @Override
    public void resize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    @Override
    public Rect2i getTooltipArea() {
        int left = getX() - PADDING;
        int top = getY() - PADDING;
        int bgWidth = width + 2 * PADDING + font.width("_");
        int bgHeight = height + 2 * PADDING;
        return new Rect2i(left, top, bgWidth, bgHeight);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }
}
