package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SUpdateBalance implements IPacket {
    public long personalBal;
    public long teamBal;

    public SUpdateBalance() {}

    public SUpdateBalance(long personalBal, long teamBal) {
        this.personalBal = personalBal;
        this.teamBal = teamBal;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.personalBal = buf.readLong();
        this.teamBal = buf.readLong();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeLong(this.personalBal);
        buf.writeLong(this.teamBal);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        ClientProxy.personalBalance = this.personalBal;
        ClientProxy.teamBalance = this.teamBal;
    }

    public static Function<PacketBuffer,SUpdateBalance> decoder() {
        return pb -> {
            SUpdateBalance packet = new SUpdateBalance();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
