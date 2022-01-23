package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;

public class SDenyUseItem implements IPacket {
    public InteractionHand hand;
    public ItemStack item;

    public SDenyUseItem() {
    }

    public SDenyUseItem(InteractionHand hand, ItemStack item) {
        this.hand = hand;
        this.item = item;
    }

    public static Function<FriendlyByteBuf, SDenyUseItem> decoder() {
        return pb -> {
            SDenyUseItem packet = new SDenyUseItem();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.hand = buf.readEnum(InteractionHand.class);
        this.item = buf.readItem();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeItemStack(this.item, false);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSDenyUseItemPacket(ctx, this);
    }
}
