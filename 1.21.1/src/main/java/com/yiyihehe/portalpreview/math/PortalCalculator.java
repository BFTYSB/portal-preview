package com.yiyihehe.portalpreview.math;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
     * 获取门框所有黑曜石方块位置（竖直门框）
     */
    public static List<BlockPos> getFrameBlocks(BlockPos basePos, Direction faceDir) {
        List<BlockPos> blocks = new ArrayList<>();
        Direction widthDir = faceDir.rotateYCounterclockwise();

        for (int y = 0; y < 5; y++) {
            for (int w = 0; w < 4; w++) {
                if (y == 0 || y == 4 || w == 0 || w == 3) {
                    blocks.add(basePos.up(y).offset(widthDir, w));
                }
            }
        }
        return blocks;
    }
}
