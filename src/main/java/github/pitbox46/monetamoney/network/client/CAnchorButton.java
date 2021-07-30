package github.pitbox46.monetamoney.network.client;

import net.minecraft.util.math.BlockPos;

public class CAnchorButton {
    public final BlockPos pos;
    public final boolean enable;

    public CAnchorButton(BlockPos pos, boolean enable) {
        this.pos = pos;
        this.enable = enable;
    }
}
