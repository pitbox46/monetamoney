package github.pitbox46.monetamoney.network.client;

import net.minecraft.util.math.BlockPos;

public class CTeamButton {
    public final BlockPos pos;
    public final Button button;

    public CTeamButton(BlockPos pos, Button button) {
        this.pos = pos;
        this.button = button;
    }


    public enum Button {
        OPENPAGE,
        CREATE,
        JOIN,
        REMOVE,
        LEAVE,
        SWITCH,
        LOCK,
        UNLOCK
    }
}
