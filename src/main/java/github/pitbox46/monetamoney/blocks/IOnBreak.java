package github.pitbox46.monetamoney.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IOnBreak {
    void onBlockBreak(Level world, BlockPos pos);
}
