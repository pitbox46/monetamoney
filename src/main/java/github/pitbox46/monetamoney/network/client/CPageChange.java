package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.containers.vault.ShopHomeContainer;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SSyncAuctionNBT;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Function;

public class CPageChange implements IPacket {
    public short page;
    public int subpage;

    public CPageChange() {
    }

    public CPageChange(short page, int subpage) {
        this.page = page;
        this.subpage = subpage;
    }

    public static Function<FriendlyByteBuf, CPageChange> decoder() {
        return pb -> {
            CPageChange packet = new CPageChange();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.page = buf.readShort();
        this.subpage = buf.readInt();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeShort(this.page);
        buf.writeInt(this.subpage);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if (ctx.getSender() == null) return;

        String player = ctx.getSender().getGameProfile().getName();

        short page = this.page;
        int subpage = this.subpage;

        switch (page) {
            //Change balance
            case 3: {
                NetworkHooks.openGui(ctx.getSender(), new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return new TranslatableComponent("screen.monetamoney.accounttransaction");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                        return new AccountTransactionContainer(id, inv);
                    }
                });
            }
            break;
            //Auction
            case 5:
            case 6: {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
                if (ctx.getSender().containerMenu instanceof AuctionHomeContainer) {
                    ((AuctionHomeContainer) ctx.getSender().containerMenu).changePage(page == 6, subpage);
                } else {
                    NetworkHooks.openGui(ctx.getSender(), new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return new TranslatableComponent("screen.monetamoney.auctionhome");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                            return new AuctionHomeContainer(id, inv, page == 6, subpage);
                        }
                    }, buf -> buf.writeInt(this.subpage));
                }
            }
            break;
            //Auction list item
            case 7: {
                ListTag auctionList = (ListTag) Auctioned.auctionedNBT.get("auction");
                assert auctionList != null;

                int items = auctionList.stream().mapToInt((inbt) -> {
                    CompoundTag nbt = ((CompoundTag) inbt);
                    if (nbt.getString("owner").equals(player)) {
                        return 1;
                    }
                    return 0;
                }).sum();

                long price = ServerEvents.calculateListCost(items);
                //Send total new daily price instead of just the extra cost
                long dailyPrice = ServerEvents.calculateDailyListCost(items) * items;

                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncFeesPacket(price, dailyPrice));
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
                NetworkHooks.openGui(ctx.getSender(), new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return new TranslatableComponent("screen.monetamoney.auctionlist");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                        return new AuctionListItemContainer(id, inv);
                    }
                });
            }
            break;
            //Shop
            case 8: {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
                if (ctx.getSender().containerMenu instanceof ShopHomeContainer) {
                    ((ShopHomeContainer) ctx.getSender().containerMenu).changePage(subpage);
                } else {
                    NetworkHooks.openGui(ctx.getSender(), new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return new TranslatableComponent("screen.monetamoney.shophome");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                            return new ShopHomeContainer(id, inv, subpage);
                        }
                    }, buf -> buf.writeInt(this.subpage));
                }
            }
            break;
        }
    }
}
