
package com.aewireless.gui.wireless;

import com.aewireless.AeWireless;
import com.aewireless.gui.RenderButton;
import com.aewireless.gui.weights.CustomMaterialTextField;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * 创建一个二级窗口
 * 由于此窗口不需要确认获取Screen的信息从而执行操作，故继承Screen
 */
public class InputChannelNameScreen extends Screen {
    private CustomMaterialTextField channelNameField;
    WirelessScreen screen;
    protected static final Button.CreateNarration DEFAULT_NARRATION = (supplier) -> (MutableComponent)supplier.get();

    // 窗口尺寸
    protected final int windowWidth = 180;
    protected final int windowHeight = 64;

    // 按钮尺寸
    private final int buttonWidth = 40;
    private final int buttonHeight = 15;

    protected InputChannelNameScreen(WirelessScreen screen) {
        super(Component.translatable("gui.input_channel_name_screen.title"));
        this.screen = screen;

    }

    @Override
    protected void init() {
        super.init();

        int windowX = (this.width - windowWidth) / 2;
        int windowY = (this.height - windowHeight) / 2;

        // 创建自定义材质输入框
        channelNameField = new CustomMaterialTextField(
                Minecraft.getInstance().font,
                windowX + 20,
                windowY + 13,
                windowWidth - 40,
                12,
                Component.translatable("")//实测占位
        );

        // 设置占位符文本
        channelNameField.setPlaceholder(Component.translatable("gui.input_channel_name_screen.placeholder"));



        // 设置最大字符数
        channelNameField.setMaxLength(10);

        this.addRenderableWidget(channelNameField);



        this.addRenderableWidget(new RenderButton(
                windowX + 30,
                windowY + windowHeight - 25,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.yes"),
                (button) -> {
                    String value = this.channelNameField.getValue();

                    screen.addDataRow( value , UUID.fromString(screen.getMenu().getUUID()));
                    Minecraft.getInstance().popGuiLayer();
                },
                DEFAULT_NARRATION
        ));


        this.addRenderableWidget(new RenderButton(
                windowX + windowWidth - 90,
                windowY + windowHeight - 25,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.no"),
                (button) -> Minecraft.getInstance().popGuiLayer(),
                DEFAULT_NARRATION
        ));
    }

    @Override
    public void render(GuiGraphics arg, int i, int j, float f) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderBackground(arg);
        super.render(arg, i, j, f);

    }

    @Override
    @SuppressWarnings("all")
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        ResourceLocation resourceLocation = new ResourceLocation(AeWireless.MOD_ID, "textures/de.png");

        int windowX = (this.width - windowWidth) / 2;
        int windowY = (this.height - windowHeight) / 2;

        guiGraphics.blit(resourceLocation,
                windowX, windowY, 0, 0, windowWidth, windowHeight,220 , 140);
    }

}
