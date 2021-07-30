package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SDenyUseItem implements IPacket {
    public Hand hand;
    public ItemStack item;

    public SDenyUseItem() {}

    public SDenyUseItem(Hand hand, ItemStack item) {
        this.hand = hand;
        this.item = item;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.hand = buf.readEnumValue(Hand.class);
        this.item = buf.readItemStack();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeEnumValue(this.hand);
        buf.writeItemStack(this.item, false);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setHeldItem(this.hand, this.item);
        }
    }

    public static Function<PacketBuffer,SDenyUseItem> decoder() {
        return pb -> {
            SDenyUseItem packet = new SDenyUseItem();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
