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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AeWireless.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        float relMasterX = (float) (masterX - camX);
        float relMasterY = (float) (masterY - camY);
        float relMasterZ = (float) (masterZ - camZ);
        var pose = poseStack.last();
        var poseMatrix = pose.pose();
        var normalMatrix = pose.normal();

        for (WirelessConnectBlockEntity.SlaveRef ref : WirelessLinkRenderState.getSlaves()) {
            if (!ref.dimension().equals(level.dimension())) continue;
            BlockPos pos = ref.pos();

            AABB box = new AABB(pos).inflate(0.01D).move(-camX, -camY, -camZ);
            LevelRenderer.renderLineBox(poseStack, consumer, box, 0.0F, 1.0F, 0.0F, 1.0F);

            double sx = pos.getX() + 0.5D - camX;
            double sy = pos.getY() + 0.5D - camY;
            double sz = pos.getZ() + 0.5D - camZ;

            consumer.vertex(poseMatrix, relMasterX, relMasterY, relMasterZ)
                    .color(0.0F, 1.0F, 0.0F, 1.0F)
                    .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                    .endVertex();
            consumer.vertex(poseMatrix, (float) sx, (float) sy, (float) sz)
                    .color(0.0F, 1.0F, 0.0F, 1.0F)
                    .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                    .endVertex();
        }

        bufferSource.endBatch(RenderType.lines());
    }
}
