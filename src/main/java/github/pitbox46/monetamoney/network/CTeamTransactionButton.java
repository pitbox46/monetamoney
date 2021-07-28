package github.pitbox46.monetamoney.network;

import net.minecraft.util.math.BlockPos;

public class CTeamTransactionButton {
    public final BlockPos pos;
    public final int amount;
    public final Button button;

    public CTeamTransactionButton(BlockPos pos, int amount, Button button) {
        this.pos = pos;
        this.amount = amount;
        this.button = button;
    }

    public enum Button {
        DEPOSIT,
        WITHDRAW
    }
}
