package com.aewireless.gui.wireless;

import com.aewireless.AeWireless;
import com.aewireless.gui.RenderButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ConfirmDeleteScreen extends Screen {
    private final Runnable onConfirm;
    protected static final Button.CreateNarration DEFAULT_NARRATION = (supplier) -> (MutableComponent)supplier.get();

    // 窗口尺寸
    private final int windowWidth = 180;
    private final int windowHeight = 60;

    // 按钮尺寸
    private final int buttonWidth = 40;
    private final int buttonHeight = 15;

    protected ConfirmDeleteScreen( Runnable onConfirm) {
        super(Component.translatable("gui.confirm_delete.title"));
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        super.init();


        int windowX = (this.width - windowWidth) / 2;
        int windowY = (this.height - windowHeight) / 2;


        this.addRenderableWidget(new RenderButton(
                windowX + 30,
                windowY + windowHeight - 25,
                buttonWidth,
                buttonHeight,
                Component.translatable("gui.yes"),
                (button) -> {
                    onConfirm.run();
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        this.renderBackground(guiGraphics);

        int windowX = (this.width - windowWidth) / 2;
        int windowY = (this.height - windowHeight) / 2;

        Component title = Component.translatable("gui.confirm_delete.message");
        int textWidth = this.font.width(title);
        guiGraphics.drawString(
                this.font,
                title,
                windowX + (windowWidth - textWidth) / 2,
                windowY + 10,
                0xFFFFFF,
                false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
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


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
