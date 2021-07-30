package github.pitbox46.monetamoney.network.server;

public class SOpenBalancePage {
    public final long personalBal;
    public final long teamBal;

    public SOpenBalancePage(long personalBal, long teamBal) {
        this.personalBal = personalBal;
        this.teamBal = teamBal;
    }
}
