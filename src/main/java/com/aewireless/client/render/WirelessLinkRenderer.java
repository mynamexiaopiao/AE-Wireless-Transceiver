package com.aewireless.client.render;

import com.aewireless.AeWireless;
import com.aewireless.block.WirelessConnectBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber
public class WirelessLinkRenderer {
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        if (!WirelessLinkRenderState.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;
        if (WirelessLinkRenderState.getMasterDim() == null || !WirelessLinkRenderState.getMasterDim().equals(level.dimension())) {
            return;
        }

        BlockPos masterPos = WirelessLinkRenderState.getMasterPos();
        if (masterPos == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        var cam = mc.gameRenderer.getMainCamera();
        double camX = cam.getPosition().x;
        double camY = cam.getPosition().y;
        double camZ = cam.getPosition().z;

        double masterX = masterPos.getX() + 0.5D;
        double masterY = masterPos.getY() + 0.5D;
        double masterZ = masterPos.getZ() + 0.5D;

        for (WirelessConnectBlockEntity.SlaveRef ref : WirelessLinkRenderState.getSlaves()) {
            if (!ref.dimension().equals(level.dimension())) continue;
            BlockPos pos = ref.pos();

            AABB box = new AABB(pos).inflate(0.01D).move(-camX, -camY, -camZ);
            LevelRenderer.renderLineBox(poseStack, consumer, box, 0.0F, 1.0F, 0.0F, 1.0F);

            double sx = pos.getX() + 0.5D - camX;
            double sy = pos.getY() + 0.5D - camY;
            double sz = pos.getZ() + 0.5D - camZ;

            consumer.addVertex(poseStack.last().pose(), (float) (masterX - camX), (float) (masterY - camY), (float) (masterZ - camZ))
                    .setColor(0.0F, 1.0F, 0.0F, 1.0F)
                    .setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
            consumer.addVertex(poseStack.last().pose(), (float) sx, (float) sy, (float) sz)
                    .setColor(0.0F, 1.0F, 0.0F, 1.0F)
                    .setNormal(poseStack.last(), 0.0F, 1.0F, 0.0F);
        }

        bufferSource.endBatch(RenderType.lines());
    }
}
