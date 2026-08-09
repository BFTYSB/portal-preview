package com.yiyihehe.portalpreview;

import com.yiyihehe.portalpreview.config.ConfigManager;
import com.yiyihehe.portalpreview.config.ModConfig;
import com.yiyihehe.portalpreview.math.PortalCalculator;
import com.yiyihehe.portalpreview.math.PortalFrameScanner;
import com.yiyihehe.portalpreview.render.PortalHudOverlay;
import com.yiyihehe.portalpreview.render.PortalPreviewRenderer;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class PortalPreviewClient implements ClientModInitializer {

    public static final String MOD_ID = "portal-preview";

    // 1.20.1 KeyBinding 使用字符串分类 key（无 Category 对象）
    private static final String PP_CATEGORY = "category.portal-preview";

    // 地狱门框数据
    public static PortalFrameScanner.PortalFrame currentNetherFrame = null;
    public static BlockPos previewOverworldPos = null;
    public static Direction previewDirection = Direction.NORTH;

    // previewActive = HUD 显示开关（地狱坐标提示）
    public static boolean previewActive = false;
    // overworldRenderEnabled = 主世界预览渲染开关
    public static boolean overworldRenderEnabled = false;
    // 用户手动切换过方向（避免被自动扫描结果覆盖）
    public static boolean directionOverridden = false;
    // 扫描冷却（每 20 tick 扫描一次，避免每帧全量扫描导致卡顿）
    private static int scanCooldown = 0;
    // 已搭建的传送门方块位置
    public static final Set<BlockPos> builtPortalBlocks = new HashSet<>();

    // 按键绑定
    private static KeyBinding togglePreviewKey;
    private static KeyBinding cycleDirectionKey;
    private static KeyBinding openConfigKey;
    private static KeyBinding closeRenderKey;

    public static String getToggleKeyName() {
        return togglePreviewKey.getBoundKeyLocalizedText().getString();
    }

    public static String getCycleKeyName() {
        return cycleDirectionKey.getBoundKeyLocalizedText().getString();
    }

    public static String getCloseRenderKeyName() {
        return closeRenderKey.getBoundKeyLocalizedText().getString();
    }

    @Override
    public void onInitializeClient() {
        ConfigManager.init();

        togglePreviewKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.portal-preview.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            PP_CATEGORY
        ));
        cycleDirectionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.portal-preview.cycle-dir",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            PP_CATEGORY
        ));
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.portal-preview.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            PP_CATEGORY
        ));
        closeRenderKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.portal-preview.close-render",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            PP_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // 打开配置
            if (openConfigKey.wasPressed()) {
                client.setScreen(AutoConfig.getConfigScreen(ModConfig.class, null).get());
                return;
            }

            // P 键：切换 HUD 显示（地狱坐标提示）
            if (togglePreviewKey.wasPressed()) {
                previewActive = !previewActive;
                if (previewActive) {
                    updatePreview(client);
                    sendOverlayMessage(client, "§a[BF煎饼猫] HUD 已开启");
                } else {
                    sendOverlayMessage(client, "§c[BF煎饼猫] HUD 已关闭");
                }
            }

            // [ 键：切换方向（地狱或主世界都可用）
            if (cycleDirectionKey.wasPressed()) {
                previewDirection = switch (previewDirection) {
                    case NORTH -> Direction.SOUTH;
                    case SOUTH -> Direction.EAST;
                    case EAST -> Direction.WEST;
                    case WEST -> Direction.NORTH;
                    default -> Direction.NORTH;
                };
                directionOverridden = true;
                // 清空已搭建记录，因为方向变了
                builtPortalBlocks.clear();
                // 不重新扫描：手动方向优先，避免被自动扫描的 frame.direction 覆盖
                sendOverlayMessage(client, "§e[BF煎饼猫] 方向: " + directionToChinese(previewDirection));
            }

            // R 键：开关主世界预览渲染
            if (closeRenderKey.wasPressed()) {
                if (overworldRenderEnabled) {
                    overworldRenderEnabled = false;
                    sendOverlayMessage(client, "§c[BF煎饼猫] 已关闭主世界预览渲染");
                } else if (previewOverworldPos != null) {
                    overworldRenderEnabled = true;
                    sendOverlayMessage(client, "§a[BF煎饼猫] 已重新开启主世界预览渲染");
                } else {
                    sendOverlayMessage(client, "§c[BF煎饼猫] 没有可用的预览数据，请先去地狱扫描门框");
                }
            }

            // 持续扫描（只在 HUD 开启时，在地狱）— 每 20 tick 一次，避免每帧全量扫描
            if (previewActive && client.world.getRegistryKey() == World.NETHER) {
                if (scanCooldown <= 0) {
                    updatePreview(client);
                    scanCooldown = 20;
                } else {
                    scanCooldown--;
                }
            }

            // 主世界：检测已搭建的方块，逐个减少渲染
            if (client.world.getRegistryKey() == World.OVERWORLD
                    && overworldRenderEnabled
                    && previewOverworldPos != null) {
                checkBuiltBlocks(client);
            }
        });

        // 1.20.1 世界渲染事件
        PortalPreviewRenderer.register();

        // 1.20.1 HUD 渲染回调（HudElementRegistry 是 26.1+ API，1.20.1 用 HudRenderCallback）
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> PortalHudOverlay.render(drawContext));
    }

    /**
     * 发送 Action Bar 消息（1.20.1: client.inGameHud.setOverlayMessage）
     */
    private void sendOverlayMessage(MinecraftClient client, String msg) {
        if (client.inGameHud != null) {
            client.inGameHud.setOverlayMessage(Text.literal(msg), true);
        }
    }

    private void updatePreview(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        World world = client.world;
        BlockPos playerPos = client.player.getBlockPos();

        if (world.getRegistryKey() == World.NETHER) {
            PortalFrameScanner.PortalFrame frame = PortalFrameScanner.findNearestFrame(world, playerPos);
            if (frame != null) {
                currentNetherFrame = frame;
                previewOverworldPos = PortalCalculator.netherToOverworld(frame.baseCorner);
                // 用户手动切换过方向时，保留手动方向，不覆盖
                if (!directionOverridden) {
                    previewDirection = frame.direction;
                }
                overworldRenderEnabled = true;
                builtPortalBlocks.clear();
            }
        }
    }

    /**
     * 检查主世界目标位置已搭建了多少传送门方块
     */
    private void checkBuiltBlocks(MinecraftClient client) {
        Direction widthDir = previewDirection.rotateYCounterclockwise();
        // 每次重建集合：玩家拆掉方块后计数同步减少
        builtPortalBlocks.clear();
        // 内部是 2×3 区域
        for (int y = 1; y <= 3; y++) {
            for (int w = 1; w <= 2; w++) {
                BlockPos pos = previewOverworldPos.up(y).offset(widthDir, w);
                if (client.world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) {
                    builtPortalBlocks.add(pos);
                }
            }
        }

        // 全部 6 个内部方块都搭建完成，关闭渲染
        if (builtPortalBlocks.size() >= 6) {
            overworldRenderEnabled = false;
            builtPortalBlocks.clear();
            sendOverlayMessage(client, "§a[BF煎饼猫] 传送门搭建完成，预览自动关闭");
        }
    }

    public static String directionToChinese(Direction dir) {
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
