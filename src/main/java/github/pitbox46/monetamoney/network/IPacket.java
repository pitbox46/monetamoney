package github.pitbox46.monetamoney.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public interface IPacket {
    void readPacketData(FriendlyByteBuf buf);

    void writePacketData(FriendlyByteBuf buf);

    void processPacket(NetworkEvent.Context ctx);
}
