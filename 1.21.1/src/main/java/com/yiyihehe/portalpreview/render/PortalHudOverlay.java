package com.yiyihehe.portalpreview.render;

import com.yiyihehe.portalpreview.PortalPreviewClient;
import com.yiyihehe.portalpreview.config.ConfigManager;
import com.yiyihehe.portalpreview.config.ModConfig;
import com.yiyihehe.portalpreview.config.ModConfig.HudPosition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PortalHudOverlay {

    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 5;

    /**
     * 1.21.1: HudRenderCallback 回调（26.1 的 HudElement 接口在 1.21.1 不存在）
     */
    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ModConfig config = ConfigManager.get();
        if (!config.showHudInNether) return;

        String toggleKey = PortalPreviewClient.getToggleKeyName();
        String cycleKey = PortalPreviewClient.getCycleKeyName();
        String closeKey = PortalPreviewClient.getCloseRenderKeyName();

        List<String> lines = new ArrayList<>();

        if (client.world.getRegistryKey() == World.NETHER) {
            // 地狱：只在 previewActive 时显示
            if (!PortalPreviewClient.previewActive) return;

            if (PortalPreviewClient.currentNetherFrame == null) {
                lines.add("§c[BF煎饼猫] 未找到附近的地狱门框");
                lines.add("§7站在门框附近按 §e" + toggleKey + "§7 重试");
                lines.add("§7按 §e" + toggleKey + "§7 关闭 HUD");
            } else {
                BlockPos netherBase = PortalPreviewClient.currentNetherFrame.baseCorner;
                BlockPos overworldTarget = PortalPreviewClient.previewOverworldPos;

                lines.add("§b=== BF煎饼猫 ===");
                lines.add(String.format("§7地狱门框: §f%d, %d, %d",
                    netherBase.getX(), netherBase.getY(), netherBase.getZ()));
                lines.add(String.format("§7主世界对应: §a%d, %d, %d",
                    overworldTarget.getX(), overworldTarget.getY(), overworldTarget.getZ()));
                lines.add(String.format("§7朝向: §e%s §7(按 §e%s§7 切换)",
                    directionToChinese(PortalPreviewClient.previewDirection), cycleKey));

                double dist = client.player.getPos().distanceTo(netherBase.toCenterPos());
                lines.add(String.format("§7距离门框: §f%.1f 格", dist));

                lines.add("§7按 §e" + toggleKey + "§7 关闭 HUD");
            }
        } else if (client.world.getRegistryKey() == World.OVERWORLD) {
            // 主世界：显示搭建进度
            if (!PortalPreviewClient.overworldRenderEnabled) return;
            if (PortalPreviewClient.previewOverworldPos == null) return;

            int built = PortalPreviewClient.builtPortalBlocks.size();
            int total = 6; // 2×3 内部

            lines.add("§b=== BF煎饼猫 搭建进度 ===");
            lines.add(String.format("§7进度: §a%d§7/§f%d §7个传送门方块", built, total));
            lines.add(String.format("§7坐标: §a%d, %d, %d",
                PortalPreviewClient.previewOverworldPos.getX(),
                PortalPreviewClient.previewOverworldPos.getY(),
                PortalPreviewClient.previewOverworldPos.getZ()));
            lines.add(String.format("§7朝向: §e%s §7(按 §e%s§7 切换)",
                directionToChinese(PortalPreviewClient.previewDirection), cycleKey));
            lines.add("§7按 §e" + closeKey + "§7 关闭预览渲染");
        }

        if (lines.isEmpty()) return;

        // 1.21.1: 使用 client.textRenderer 获取字体渲染器
        TextRenderer font = client.textRenderer;

        int maxTextWidth = 0;
        for (String line : lines) {
            int w = font.getWidth(Text.literal(line));
            if (w > maxTextWidth) maxTextWidth = w;
        }
        int totalHeight = lines.size() * (LINE_HEIGHT + 2);

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        int startX, startY;
        HudPosition pos = config.hudPosition;
        switch (pos) {
            case TOP_RIGHT -> {
                startX = screenW - maxTextWidth - PADDING * 2;
                startY = PADDING;
            }
            case BOTTOM_LEFT -> {
                startX = PADDING;
                startY = screenH - totalHeight - PADDING * 2;
            }
            case BOTTOM_RIGHT -> {
                startX = screenW - maxTextWidth - PADDING * 2;
                startY = screenH - totalHeight - PADDING * 2;
            }
            case RIGHT_CENTER -> {
                startX = screenW - maxTextWidth - PADDING * 2;
                startY = screenH / 2 - totalHeight / 2;
            }
            default -> { // TOP_LEFT
                startX = PADDING;
                startY = PADDING;
            }
        }

        int bgX = startX - PADDING;
        int bgY = startY - PADDING;
        int bgW = maxTextWidth + PADDING * 2;
        int bgH = totalHeight + PADDING * 2;

        // 绘制半透明黑色背景
        context.fill(bgX, bgY, bgX + bgW, bgY + bgH, 0xCC000000);

        for (int i = 0; i < lines.size(); i++) {
            int y = startY + i * (LINE_HEIGHT + 2);
            context.drawTextWithShadow(font, Text.literal(lines.get(i)), startX, y, 0xFFFFFF);
        }
    }

    private static String directionToChinese(Direction dir) {
        return switch (dir) {
            case NORTH -> "北";
            case SOUTH -> "南";
            case EAST -> "东";
            case WEST -> "西";
            case UP -> "上";
            case DOWN -> "下";
            default -> dir.getName();
        };
    }
}
