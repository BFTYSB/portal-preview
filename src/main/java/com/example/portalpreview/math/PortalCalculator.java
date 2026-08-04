package com.example.portalpreview.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class PortalCalculator {

    /**
     * 将下界坐标转换为主世界对应坐标。
     * 原版逻辑：下界方块中心浮点 (nx+0.5) × 8 = 8nx + 4，
     * 即下界方块西北角映射到主世界的目标落点方块西北角。
     */
    public static BlockPos netherToOverworld(BlockPos netherPos) {
        return new BlockPos(
            netherPos.getX() * 8 + 4,
            netherPos.getY(),
            netherPos.getZ() * 8 + 4
        );
    }

    /**
     * 将主世界坐标转换为下界对应坐标。
     * 原版逻辑：主世界方块中心浮点 (ox+0.5) ÷ 8，向下取整。
     * 用整数运算等价表示为 floorDiv(ox * 2 + 1, 16)。
     */
    public static BlockPos overworldToNether(BlockPos overworldPos) {
        return new BlockPos(
            Math.floorDiv(overworldPos.getX() * 2 + 1, 16),
            overworldPos.getY(),
            Math.floorDiv(overworldPos.getZ() * 2 + 1, 16)
        );
    }

    /**
     * 计算门框四个角坐标
     */
    public static BlockPos[] getFrameCorners(BlockPos basePos, Direction direction, int width, int height) {
        BlockPos[] corners = new BlockPos[4];
        int longSide = 5;
        int shortSide = 4;

        Direction longDir = direction;
        Direction shortDir = direction.getCounterClockWise();

        corners[0] = basePos;
        corners[1] = basePos.relative(shortDir, shortSide - 1);
        corners[2] = basePos.relative(longDir, longSide - 1);
        corners[3] = basePos.relative(longDir, longSide - 1).relative(shortDir, shortSide - 1);

        return corners;
    }

    /**
     * 计算门框中心点
     */
    public static BlockPos getFrameCenter(BlockPos basePos, Direction direction) {
        BlockPos[] corners = getFrameCorners(basePos, direction, 4, 5);
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : corners) {
            minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX());
            minZ = Math.min(minZ, p.getZ()); maxZ = Math.max(maxZ, p.getZ());
        }
        return new BlockPos((minX + maxX) / 2, basePos.getY(), (minZ + maxZ) / 2);
    }

    /**
     * 欧几里得距离
     */
    public static double euclideanDistance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 检查坐标是否在搜索菱形范围内
     */
    public static boolean isInSearchRange(BlockPos center, BlockPos pos, int radius) {
        int manhattan = Math.abs(pos.getX() - center.getX()) + Math.abs(pos.getZ() - center.getZ());
        return manhattan <= radius;
    }

    /**
     * 获取门框所有黑曜石方块位置（竖直门框）
     */
    public static List<BlockPos> getFrameBlocks(BlockPos basePos, Direction faceDir) {
        List<BlockPos> blocks = new ArrayList<>();
        Direction widthDir = faceDir.getCounterClockWise();

        for (int y = 0; y < 5; y++) {
            for (int w = 0; w < 4; w++) {
                if (y == 0 || y == 4 || w == 0 || w == 3) {
                    blocks.add(basePos.above(y).relative(widthDir, w));
                }
            }
        }
        return blocks;
    }
}
