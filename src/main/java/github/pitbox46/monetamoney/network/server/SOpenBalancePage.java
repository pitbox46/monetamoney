package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;

public class SOpenBalancePage implements IPacket {
    public long personalBal;
    public long teamBal;

    public SOpenBalancePage() {
    }

    public SOpenBalancePage(long personalBal, long teamBal) {
        this.personalBal = personalBal;
        this.teamBal = teamBal;
    }

    public static Function<FriendlyByteBuf, SOpenBalancePage> decoder() {
        return pb -> {
            SOpenBalancePage packet = new SOpenBalancePage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.personalBal = buf.readLong();
        this.teamBal = buf.readLong();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeLong(this.personalBal);
        buf.writeLong(this.teamBal);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSOpenBalancePage(ctx, this);
    }
}
