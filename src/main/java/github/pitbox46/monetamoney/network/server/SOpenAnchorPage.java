package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Function;

public class SOpenAnchorPage implements IPacket {
    public boolean active;

    public SOpenAnchorPage() {
    }

    public SOpenAnchorPage(boolean active) {
        this.active = active;
    }

    public static Function<FriendlyByteBuf, SOpenAnchorPage> decoder() {
        return pb -> {
            SOpenAnchorPage packet = new SOpenAnchorPage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeBoolean(this.active);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSOpenAnchorPage(ctx, this);
    }
}
