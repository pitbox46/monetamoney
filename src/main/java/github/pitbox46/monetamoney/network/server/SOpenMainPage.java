package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;

public class SOpenMainPage implements IPacket {
    public static Function<FriendlyByteBuf, SOpenMainPage> decoder() {
        return pb -> {
            SOpenMainPage packet = new SOpenMainPage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSOpenMainPage(ctx, this);
    }
}
