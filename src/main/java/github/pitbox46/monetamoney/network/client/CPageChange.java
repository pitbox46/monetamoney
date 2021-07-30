package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.Config;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SSyncAuctionNBT;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Function;

public class CPageChange implements IPacket {
    public short page;
    public int subpage;

    public CPageChange() {}

    public CPageChange(short page, int subpage) {
        this.page = page;
        this.subpage = subpage;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.page = buf.readShort();
        this.subpage = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeShort(this.page);
        buf.writeInt(this.subpage);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() == null) return;

        short page = this.page;
        int subpage = this.subpage;

        switch(page) {
            //Change balance
            case 3: {
                NetworkHooks.openGui(ctx.getSender(), new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("screen.monetamoney.accounttransaction");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                        return new AccountTransactionContainer(id, inv);
                    }
                });
            } break;
            //Auction
            case 5: case 6: {
                if(ctx.getSender().openContainer instanceof AuctionHomeContainer) {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
                    ((AuctionHomeContainer) ctx.getSender().openContainer).changePage(page == 6, subpage);
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
                    NetworkHooks.openGui(ctx.getSender(), new INamedContainerProvider() {
                        @Override
                        public ITextComponent getDisplayName() {
                            return new TranslationTextComponent("screen.monetamoney.auctionhome");
                        }

                        @Override
                        public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                            return new AuctionHomeContainer(id, inv, page == 6, subpage);
                        }
                    }, buf -> buf.writeInt(this.subpage));
                }
            } break;
            //Auction list item
            case 7: {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncFeesPacket(Config.LIST_FEE.get(), Config.DAILY_LIST_FEE.get()));
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
                NetworkHooks.openGui(ctx.getSender(), new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("screen.monetamoney.auctionlist");
                    }

                    @Override
                    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                        return new AuctionListItemContainer(id, inv);
                    }
                });
            } break;
        }
    }

    public static Function<PacketBuffer, CPageChange> decoder() {
        return pb -> {
            CPageChange packet = new CPageChange();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
