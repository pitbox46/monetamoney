package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SSyncFeesPacket implements IPacket {
    public long fee;
    public long dailyFee;

    public SSyncFeesPacket() {}

    public SSyncFeesPacket(long fee, long dailyFee) {
        this.fee = fee;
        this.dailyFee = dailyFee;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.fee = buf.readLong();
        this.dailyFee = buf.readLong();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeLong(this.fee);
        buf.writeLong(this.dailyFee);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSSyncFeesPacket(ctx, this);
    }

    public static Function<PacketBuffer,SSyncFeesPacket> decoder() {
        return pb -> {
            SSyncFeesPacket packet = new SSyncFeesPacket();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
