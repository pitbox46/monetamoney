package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.screen.AnchorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SOpenAnchorPage implements IPacket {
    public boolean active;

    public SOpenAnchorPage() {}

    public SOpenAnchorPage(boolean active) {
        this.active = active;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.active = buf.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeBoolean(this.active);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSOpenAnchorPage(ctx, this);
    }

    public static Function<PacketBuffer,SOpenAnchorPage> decoder() {
        return pb -> {
            SOpenAnchorPage packet = new SOpenAnchorPage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
