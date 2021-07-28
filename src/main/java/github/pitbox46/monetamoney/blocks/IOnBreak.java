package github.pitbox46.monetamoney.blocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IOnBreak {
    void onBlockBreak(World world, BlockPos pos);
}
