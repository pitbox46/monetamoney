package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.screen.IStatusable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SGuiStatusMessage implements IPacket {
    public ITextComponent message;

    public SGuiStatusMessage() {}

    public SGuiStatusMessage(ITextComponent message) {
        this.message = message;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.message = buf.readTextComponent();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeTextComponent(this.message);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(Minecraft.getInstance().currentScreen instanceof IStatusable) {
            ((IStatusable) Minecraft.getInstance().currentScreen).setStatus(this.message);
        }
    }

    public static Function<PacketBuffer,SGuiStatusMessage> decoder() {
        return pb -> {
            SGuiStatusMessage packet = new SGuiStatusMessage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
