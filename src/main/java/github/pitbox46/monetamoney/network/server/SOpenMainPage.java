package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.screen.vault.MainPage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SOpenMainPage implements IPacket {
    @Override
    public void readPacketData(PacketBuffer buf) {
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSOpenMainPage(ctx, this);
    }

    public static Function<PacketBuffer,SOpenMainPage> decoder() {
        return pb -> {
            SOpenMainPage packet = new SOpenMainPage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
