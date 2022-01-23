package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;

public class SUpdateBalance implements IPacket {
    public long personalBal;
    public long teamBal;
    public long dailyChunks;
    public long dailyListings;

    public SUpdateBalance() {
    }

    public SUpdateBalance(long personalBal, long teamBal, long dailyChunks, long dailyListings) {
        this.personalBal = personalBal;
        this.teamBal = teamBal;
        this.dailyChunks = dailyChunks;
        this.dailyListings = dailyListings;
    }

    public static Function<FriendlyByteBuf, SUpdateBalance> decoder() {
        return pb -> {
            SUpdateBalance packet = new SUpdateBalance();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.personalBal = buf.readLong();
        this.teamBal = buf.readLong();
        this.dailyChunks = buf.readLong();
        this.dailyListings = buf.readLong();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeLong(this.personalBal);
        buf.writeLong(this.teamBal);
        buf.writeLong(this.dailyChunks);
        buf.writeLong(this.dailyListings);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSUpdateBalance(ctx, this);
    }
}
