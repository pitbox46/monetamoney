package github.pitbox46.monetamoney.utils;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;

public class BlockUtils {
    public static VoxelShape cuboidWithRotation(Direction facing, double x1, double y1, double z1, double x2, double y2, double z2) {
        switch (facing)
        {
            case NORTH:
                return Block.makeCuboidShape(x1, y1, z1, x2, y2, z2);
            case EAST:
                return Block.makeCuboidShape(16 - z2, y1, x1, 16 - z1, y2, x2);
            case SOUTH:
                return Block.makeCuboidShape(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case WEST:
                return Block.makeCuboidShape(z1, y1, 16 - x2, z2, y2, 16 - x1);
        }
        return Block.makeCuboidShape(x1, y1, z1, x2, y2, z2);
    }
}
