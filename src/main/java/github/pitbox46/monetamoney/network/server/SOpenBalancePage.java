package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.screen.vault.BalancePage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SOpenBalancePage implements IPacket {
    public long personalBal;
    public long teamBal;

    public SOpenBalancePage() {}

    public SOpenBalancePage(long personalBal, long teamBal) {
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
        if(Minecraft.getInstance().world != null) {
            Minecraft.getInstance().displayGuiScreen(new BalancePage());
            ClientProxy.personalBalance = this.personalBal;
            ClientProxy.teamBalance = this.teamBal;
        }
    }

    public static Function<PacketBuffer,SOpenBalancePage> decoder() {
        return pb -> {
            SOpenBalancePage packet = new SOpenBalancePage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
