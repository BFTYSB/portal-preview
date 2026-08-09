package com.yiyihehe.portalpreview.math;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class PortalFrameScanner {

    private static final int MAX_SCAN_RADIUS = 24;
    private static final int MIN_WIDTH = 4;
    private static final int MIN_HEIGHT = 5;
    private static final int MAX_SIZE = 21;

    /**
     * 扫描玩家附近的黑曜石门框
     */
    public static PortalFrame findNearestFrame(World world, BlockPos center) {
        // 收集附近所有黑曜石位置
        Set<BlockPos> obsidianSet = new HashSet<>();
        for (int dx = -MAX_SCAN_RADIUS; dx <= MAX_SCAN_RADIUS; dx++) {
            for (int dy = -8; dy <= 24; dy++) {
                for (int dz = -MAX_SCAN_RADIUS; dz <= MAX_SCAN_RADIUS; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (world.getBlockState(pos).isOf(Blocks.OBSIDIAN)) {
                        obsidianSet.add(pos);
                    }
                }
            }
        }

        if (obsidianSet.isEmpty()) return null;

        PortalFrame bestFrame = null;
        double bestDist = Double.MAX_VALUE;

        // 尝试每个黑曜石作为右下角的候选
        for (BlockPos start : obsidianSet) {
            // 尝试两个水平方向
            for (Direction faceDir : new Direction[]{Direction.NORTH, Direction.EAST}) {
                Direction leftDir = faceDir.rotateYCounterclockwise(); // 从右下往左下
                PortalFrame frame = tryFrame(world, start, faceDir, leftDir, obsidianSet);
                if (frame != null) {
                    double dist = frame.center.getSquaredDistance(center);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestFrame = frame;
                    }
                }
            }
        }

        return bestFrame;
    }

    /**
     * 尝试以 start 为右下角构建门框
     * faceDir: 门框面朝方向（厚度方向）
     * leftDir: 从右下往左下（沿底边）
     */
    private static PortalFrame tryFrame(World world, BlockPos start, Direction faceDir, Direction leftDir, Set<BlockPos> obsidianSet) {
        Direction up = Direction.UP;

        // 1. 测量底边长度（从 start 往 leftDir 方向数）
        int width = countObsidian(start, leftDir, obsidianSet, MAX_SIZE);
        if (width < MIN_WIDTH) return null;

        // 2. 测量右边高度（从 start 往上数）
        int height = countObsidian(start, up, obsidianSet, MAX_SIZE);
        if (height < MIN_HEIGHT) return null;

        // 3. 确定四个角
        BlockPos bottomRight = start;
        BlockPos bottomLeft = start.offset(leftDir, width - 1);
        BlockPos topRight = start.offset(up, height - 1);
        BlockPos topLeft = bottomLeft.offset(up, height - 1);

        // 4. 验证四个角都是黑曜石
        if (!obsidianSet.contains(bottomRight)) return null;
        if (!obsidianSet.contains(bottomLeft)) return null;
        if (!obsidianSet.contains(topRight)) return null;
        if (!obsidianSet.contains(topLeft)) return null;

        // 5. 验证底边完整
        for (int i = 0; i < width; i++) {
            if (!obsidianSet.contains(bottomRight.offset(leftDir, i))) return null;
        }

        // 6. 验证顶边完整
        for (int i = 0; i < width; i++) {
            if (!obsidianSet.contains(topRight.offset(leftDir, i))) return null;
        }

        // 7. 验证左边完整
        for (int i = 0; i < height; i++) {
            if (!obsidianSet.contains(bottomLeft.offset(up, i))) return null;
        }

        // 8. 验证右边完整
        for (int i = 0; i < height; i++) {
            if (!obsidianSet.contains(bottomRight.offset(up, i))) return null;
        }

        // 9. 验证内部是空气或传送门
        if (!validateInterior(world, bottomLeft, leftDir.getOpposite(), up, width, height)) return null;

        // 门框中心
        BlockPos center = bottomLeft.offset(leftDir.getOpposite(), width / 2).offset(up, height / 2);
        return new PortalFrame(bottomLeft, center, faceDir, width, height);
    }

    private static int countObsidian(BlockPos start, Direction dir, Set<BlockPos> obsidianSet, int max) {
        int count = 1; // 包含起点
        for (int i = 1; i < max; i++) {
            if (obsidianSet.contains(start.offset(dir, i))) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private static boolean validateInterior(World world, BlockPos bottomLeft, Direction rightDir, Direction up, int width, int height) {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                BlockPos pos = bottomLeft.offset(rightDir, x).offset(up, y);
                BlockState state = world.getBlockState(pos);
                if (!state.isAir() && !state.isOf(Blocks.NETHER_PORTAL)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class PortalFrame {
        public final BlockPos baseCorner; // 左下角
        public final BlockPos center;
        public final Direction direction; // 门框面朝方向
        public final int width;
        public final int height;

        public PortalFrame(BlockPos base, BlockPos center, Direction dir, int w, int h) {
            this.baseCorner = base;
            this.center = center;
            this.direction = dir;
            this.width = w;
            this.height = h;
        }
    }
}
