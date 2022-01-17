package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.ShopBuyContainer;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SSyncAuctionNBT;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Function;

public class COpenBuyPage implements IPacket {
    public Type type;
    public CompoundNBT nbt;

    public COpenBuyPage() {}

    public COpenBuyPage(CompoundNBT nbt, Type type) {
        this.nbt = nbt;
        this.type = type;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.nbt = buf.readCompoundTag();
        this.type = buf.readEnumValue(Type.class);
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeCompoundTag(this.nbt);
        buf.writeEnumValue(this.type);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() == null) return;
        CompoundNBT nbt = this.nbt;
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
        switch(this.type) {
            case AUCTION: {
                NetworkHooks.openGui(ctx.getSender(), new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("screen.monetamoney.auctionbuy");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                        return new AuctionBuyContainer(id, inv, nbt);
                    }
                }, buf -> buf.writeCompoundTag(nbt));
            } break;
            case SHOP: {
                NetworkHooks.openGui(ctx.getSender(), new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("screen.monetamoney.shopbuy");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                        return new ShopBuyContainer(id, inv, nbt, Auctioned.getStock(nbt.getCompound("tag")));
                    }
                }, buf -> {
                    buf.writeCompoundTag(nbt);
                    buf.writeInt(Auctioned.getStock(nbt.getCompound("tag")));
                });
            } break;
        }
    }

    public static Function<PacketBuffer, COpenBuyPage> decoder() {
        return pb -> {
            COpenBuyPage packet = new COpenBuyPage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    public enum Type {
        SHOP,
        AUCTION;
    }
}
