package com.yiyihehe.portalpreview.render;

import com.yiyihehe.portalpreview.PortalPreviewClient;
import com.yiyihehe.portalpreview.config.ConfigManager;
import com.yiyihehe.portalpreview.config.ModConfig;
import com.yiyihehe.portalpreview.math.PortalCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PortalPreviewRenderer {

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(PortalPreviewRenderer::render);
    }

    public static void render(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        if (client.level.dimension() != Level.OVERWORLD) return;
        if (!PortalPreviewClient.overworldRenderEnabled) return;
        if (PortalPreviewClient.previewOverworldPos == null) return;

        ModConfig config = ConfigManager.get();
        BlockPos basePos = PortalPreviewClient.previewOverworldPos;
        Direction dir = PortalPreviewClient.previewDirection;

        // 距离检测（512格内显示）
        double dist = client.player.position().distanceTo(Vec3.atCenterOf(basePos));
        if (dist > 512.0) return;

        // 获取门框所有方块位置
        List<BlockPos> frameBlocks = PortalCalculator.getFrameBlocks(basePos, dir);

        PoseStack poseStack = context.poseStack();
        Vec3 cameraPos = client.gameRenderer.mainCamera().position();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // 26.2+ 渲染管线：MultiBufferSource 已移除，改用 submitCustomGeometry 提交自定义几何体
        // 轮廓线使用 RenderTypes.lines()，需要 POSITION + COLOR + NORMAL + LineWidth
        SubmitNodeCollector collector = context.submitNodeCollector();

        float alpha = config.previewOpacity / 255.0f;
        float r = 0.2f, g = 0.8f, b = 1.0f;

        // 提交轮廓线
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, lineConsumer) -> {
            for (BlockPos pos : frameBlocks) {
                if (PortalPreviewClient.builtPortalBlocks.contains(pos)) continue;
                renderBlockOutline(lineConsumer, pose, pos, r, g, b, alpha);
            }
        });

        // 26.2+ 填充面使用 debugQuads，只需 POSITION + COLOR
        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, fillConsumer) -> {
            for (BlockPos pos : frameBlocks) {
                if (PortalPreviewClient.builtPortalBlocks.contains(pos)) continue;
                renderGhostBlockFill(fillConsumer, pose, pos, r, g, b, alpha * 0.3f);
            }
        });

        poseStack.popPose();
    }

    private static void renderBlockOutline(VertexConsumer consumer, PoseStack.Pose pose, BlockPos pos, float red, float green, float blue, float alpha) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float x2 = x + 1.0f;
        float y2 = y + 1.0f;
        float z2 = z + 1.0f;

        // 底面
        line(consumer, pose, x, y, z, x2, y, z, red, green, blue, alpha);
        line(consumer, pose, x2, y, z, x2, y, z2, red, green, blue, alpha);
        line(consumer, pose, x2, y, z2, x, y, z2, red, green, blue, alpha);
        line(consumer, pose, x, y, z2, x, y, z, red, green, blue, alpha);

        // 顶面
        line(consumer, pose, x, y2, z, x2, y2, z, red, green, blue, alpha);
        line(consumer, pose, x2, y2, z, x2, y2, z2, red, green, blue, alpha);
        line(consumer, pose, x2, y2, z2, x, y2, z2, red, green, blue, alpha);
        line(consumer, pose, x, y2, z2, x, y2, z, red, green, blue, alpha);

        // 立柱
        line(consumer, pose, x, y, z, x, y2, z, red, green, blue, alpha);
        line(consumer, pose, x2, y, z, x2, y2, z, red, green, blue, alpha);
        line(consumer, pose, x2, y, z2, x2, y2, z2, red, green, blue, alpha);
        line(consumer, pose, x, y, z2, x, y2, z2, red, green, blue, alpha);
    }

    private static void renderGhostBlockFill(VertexConsumer consumer, PoseStack.Pose pose, BlockPos pos, float red, float green, float blue, float alpha) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float x2 = x + 1.0f;
        float y2 = y + 1.0f;
        float z2 = z + 1.0f;

        // 前面 (z = z2)
        quad(consumer, pose, x, y, z2, x2, y, z2, x2, y2, z2, x, y2, z2, red, green, blue, alpha);
        // 后面 (z = z)
        quad(consumer, pose, x2, y, z, x, y, z, x, y2, z, x2, y2, z, red, green, blue, alpha);
        // 左面 (x = x)
        quad(consumer, pose, x, y, z, x, y, z2, x, y2, z2, x, y2, z, red, green, blue, alpha);
        // 右面 (x = x2)
        quad(consumer, pose, x2, y, z2, x2, y, z, x2, y2, z, x2, y2, z2, red, green, blue, alpha);
        // 顶面 (y = y2)
        quad(consumer, pose, x, y2, z2, x2, y2, z2, x2, y2, z, x, y2, z, red, green, blue, alpha);
        // 底面 (y = y)
        quad(consumer, pose, x, y, z, x2, y, z, x2, y, z2, x, y, z2, red, green, blue, alpha);
    }

    /**
     * 26.1+ RenderTypes.lines() 需要 Normal 和 LineWidth
     */
    private static void line(VertexConsumer consumer, PoseStack.Pose pose, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        // 计算线段方向作为法线
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            dx /= len;
            dy /= len;
            dz /= len;
        } else {
            dx = 0; dy = 1; dz = 0;
        }

        consumer.addVertex(pose, x1, y1, z1)
            .setColor(r, g, b, a)
            .setNormal(dx, dy, dz)
            .setLineWidth(2.0f);
        consumer.addVertex(pose, x2, y2, z2)
            .setColor(r, g, b, a)
            .setNormal(dx, dy, dz)
            .setLineWidth(2.0f);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float r, float g, float b, float a) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a);
        consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, a);
    }
}
