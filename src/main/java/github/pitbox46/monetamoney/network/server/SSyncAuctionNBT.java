package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SSyncAuctionNBT implements IPacket {
    public CompoundNBT nbt;

    public SSyncAuctionNBT() {}

    public SSyncAuctionNBT(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.nbt = buf.readCompoundTag();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeCompoundTag(nbt);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        Auctioned.auctionedNBT = this.nbt;
    }

    public static Function<PacketBuffer,SSyncAuctionNBT> decoder() {
        return pb -> {
            SSyncAuctionNBT packet = new SSyncAuctionNBT();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
