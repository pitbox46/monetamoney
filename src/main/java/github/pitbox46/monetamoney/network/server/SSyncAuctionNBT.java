package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;

public class SSyncAuctionNBT implements IPacket {
    public CompoundTag nbt;

    public SSyncAuctionNBT() {
    }

    public SSyncAuctionNBT(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public static Function<FriendlyByteBuf, SSyncAuctionNBT> decoder() {
        return pb -> {
            SSyncAuctionNBT packet = new SSyncAuctionNBT();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSSyncAuctionNBT(ctx, this);
    }
}
