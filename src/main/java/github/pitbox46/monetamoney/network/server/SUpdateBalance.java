package github.pitbox46.monetamoney.network.server;

public class SUpdateBalance {
    public final long personalBal;
    public final long teamBal;

    public SUpdateBalance(long personalBal, long teamBal) {
        this.personalBal = personalBal;
        this.teamBal = teamBal;
    }
}
