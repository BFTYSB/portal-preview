package com.yiyihehe.portalpreview.render;

import com.yiyihehe.portalpreview.PortalPreviewClient;
import com.yiyihehe.portalpreview.config.ConfigManager;
import com.yiyihehe.portalpreview.config.ModConfig;
import com.yiyihehe.portalpreview.math.PortalCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PortalPreviewRenderer {

    public static void register() {
        // 1.21.1: WorldRenderEvents（26.1 的 LevelRenderEvents 在 1.21.1 不存在）
        WorldRenderEvents.AFTER_TRANSLUCENT.register(PortalPreviewRenderer::render);
    }

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        if (client.world.getRegistryKey() != World.OVERWORLD) return;
        if (!PortalPreviewClient.overworldRenderEnabled) return;
        if (PortalPreviewClient.previewOverworldPos == null) return;

        ModConfig config = ConfigManager.get();
        BlockPos basePos = PortalPreviewClient.previewOverworldPos;
        Direction dir = PortalPreviewClient.previewDirection;

        // 距离检测（512格内显示）
        double dist = client.player.getPos().distanceTo(basePos.toCenterPos());
        if (dist > 512.0) return;

        // 获取门框所有方块位置
        List<BlockPos> frameBlocks = PortalCalculator.getFrameBlocks(basePos, dir);

        MatrixStack matrixStack = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();

        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumerProvider consumers = context.consumers();

        // 1.21.1: RenderLayer.getLines() 对应 26.1 的 RenderTypes.lines()
        VertexConsumer lineConsumer = consumers.getBuffer(RenderLayer.getLines());

        float alpha = config.previewOpacity / 255.0f;
        float r = 0.2f, g = 0.8f, b = 1.0f;

        for (BlockPos pos : frameBlocks) {
            if (PortalPreviewClient.builtPortalBlocks.contains(pos)) continue;
            renderBlockOutline(lineConsumer, matrixStack.peek(), pos, r, g, b, alpha);
        }

        // 1.21.1: RenderLayer.getDebugQuads() 对应 26.1 的 RenderTypes.debugQuads()
        // 仅需要 POSITION + COLOR，无需法线
        VertexConsumer fillConsumer = consumers.getBuffer(RenderLayer.getDebugQuads());
        for (BlockPos pos : frameBlocks) {
            if (PortalPreviewClient.builtPortalBlocks.contains(pos)) continue;
            renderGhostBlockFill(fillConsumer, matrixStack.peek(), pos, r, g, b, alpha * 0.3f);
        }

        matrixStack.pop();
    }

    private static void renderBlockOutline(VertexConsumer consumer, MatrixStack.Entry pose, BlockPos pos, float red, float green, float blue, float alpha) {
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

    private static void renderGhostBlockFill(VertexConsumer consumer, MatrixStack.Entry pose, BlockPos pos, float red, float green, float blue, float alpha) {
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
     * 1.21.1: VertexConsumer 链式 API —— vertex(matrix, x,y,z).color(r,g,b,a).normal(...)
     * 1.21.1 无终结方法（26.1 的 addVertex/setColor/setNormal/setLineWidth 与 next() 均不存在，
     * 顶点在最后一个元素写入后自动提交）；lines 层线宽固定，无 per-vertex setLineWidth
     */
    private static void line(VertexConsumer consumer, MatrixStack.Entry pose, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
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

        consumer.vertex(pose, x1, y1, z1)
            .color(r, g, b, a)
            .normal(pose, dx, dy, dz);
        consumer.vertex(pose, x2, y2, z2)
            .color(r, g, b, a)
            .normal(pose, dx, dy, dz);
    }

    private static void quad(VertexConsumer consumer, MatrixStack.Entry pose,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float r, float g, float b, float a) {
        consumer.vertex(pose, x1, y1, z1).color(r, g, b, a);
        consumer.vertex(pose, x2, y2, z2).color(r, g, b, a);
        consumer.vertex(pose, x3, y3, z3).color(r, g, b, a);
        consumer.vertex(pose, x4, y4, z4).color(r, g, b, a);
    }
}
