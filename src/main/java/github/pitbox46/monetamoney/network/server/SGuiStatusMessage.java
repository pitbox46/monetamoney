package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Function;

public class SGuiStatusMessage implements IPacket {
    public Component message;

    public SGuiStatusMessage() {
    }

    public SGuiStatusMessage(Component message) {
        this.message = message;
    }

    public static Function<FriendlyByteBuf, SGuiStatusMessage> decoder() {
        return pb -> {
            SGuiStatusMessage packet = new SGuiStatusMessage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.message = buf.readComponent();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeComponent(this.message);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSGuiStatusMessage(ctx, this);
    }
}
