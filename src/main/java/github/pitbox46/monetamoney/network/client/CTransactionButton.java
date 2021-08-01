package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.Config;
import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.data.Ledger;
import github.pitbox46.monetamoney.data.Outstanding;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SGuiStatusMessage;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import github.pitbox46.monetamoney.network.server.SUpdateBalance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CTransactionButton implements IPacket {
    public int amount;
    public Button button;

    public CTransactionButton() {}

    public CTransactionButton(int amount, Button button) {
        this.amount = amount;
        this.button = button;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.amount = buf.readInt();
        this.button = buf.readEnumValue(Button.class);
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeInt(this.amount);
        buf.writeEnumValue(this.button);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() != null) {
            String player = ctx.getSender().getGameProfile().getName();

            if(ctx.getSender().openContainer instanceof AccountTransactionContainer) {
                AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().openContainer;
                long balance = Ledger.readBalance(Ledger.jsonFile, player);
                switch(this.button) {
                    case WITHDRAW: {
                        if (this.amount > Coin.MAX_SIZE || this.amount <= 0) {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidsize")));
                        } else if (balance >= this.amount) {
                            Ledger.addBalance(Ledger.jsonFile, player, -this.amount);
                            ItemStack coins = Coin.createCoin(this.amount, Outstanding.newCoin(Outstanding.jsonFile, this.amount, player));
                            if (container.handler.getStackInSlot(0).isEmpty()) {
                                container.handler.setStackInSlot(0, coins);
                            } else {
                                ctx.getSender().inventory.placeItemBackInInventory(ctx.getSender().getEntityWorld(), coins);
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
                        }
                    } break;
                    case DEPOSIT: {
                        if (container.handler.getStackInSlot(0).getItem() instanceof Coin) {
                            ItemStack coins = container.handler.getStackInSlot(0);
                            if (Outstanding.redeemCoin(Outstanding.jsonFile, player, coins.getOrCreateTag().getUniqueId("uuid"))) {
                                container.handler.setStackInSlot(0, ItemStack.EMPTY);
                            } else {
                                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidcoin")));
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invaliditem")));
                        }
                    } break;
                }
            }
            else if (ctx.getSender().openContainer instanceof AuctionBuyContainer) {
                if(this.button == CTransactionButton.Button.PURCHASE) {
                    AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().openContainer;
                    CompoundNBT itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
                    int price = itemNBT.getInt("price");
                    if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.listingerror")));
                    }
                    else if (Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                        Ledger.addBalance(Ledger.jsonFile, player, -price);

                        CompoundNBT removedTagNBT = container.handler.getStackInSlot(0).write(new CompoundNBT());
                        removedTagNBT.getCompound("tag").remove("uuid");
                        removedTagNBT.getCompound("tag").remove("owner");
                        removedTagNBT.getCompound("tag").remove("price");
                        if (removedTagNBT.getCompound("tag").isEmpty()) {
                            removedTagNBT.remove("tag");
                        }
                        ctx.getSender().inventory.placeItemBackInInventory(ctx.getSender().getEntityWorld(), ItemStack.read(removedTagNBT));
                        String owner = itemNBT.getString("owner");
                        if (!owner.equals("shop listing")) {
                            Ledger.addBalance(Ledger.jsonFile, owner, price);
                            Auctioned.deleteListing(Auctioned.auctionedNBT, itemNBT.getUniqueId("uuid"));
                            container.handler.setStackInSlot(0, ItemStack.EMPTY);
                        }
                    }
                    else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
                    }
                }
                else if(this.button == CTransactionButton.Button.REMOVE) {
                    AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().openContainer;
                    CompoundNBT itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
                    int price = itemNBT.getInt("price");
                    if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.listingerror")));
                    }
                    else if(!Auctioned.confirmOwner(Auctioned.auctionedNBT, itemNBT, player)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.notowner")));
                    }
                    else {
                        CompoundNBT removedTagNBT = container.handler.getStackInSlot(0).write(new CompoundNBT());
                        removedTagNBT.getCompound("tag").remove("uuid");
                        removedTagNBT.getCompound("tag").remove("owner");
                        removedTagNBT.getCompound("tag").remove("price");
                        if (removedTagNBT.getCompound("tag").isEmpty()) {
                            removedTagNBT.remove("tag");
                        }
                        ctx.getSender().inventory.placeItemBackInInventory(ctx.getSender().getEntityWorld(), ItemStack.read(removedTagNBT));
                        Auctioned.deleteListing(Auctioned.auctionedNBT, itemNBT.getUniqueId("uuid"));
                        container.handler.setStackInSlot(0, ItemStack.EMPTY);
                    }
                }
            }
            else if (ctx.getSender().openContainer instanceof AuctionListItemContainer) {
                if(this.button == CTransactionButton.Button.LIST_ITEM) {
                    AuctionListItemContainer container = (AuctionListItemContainer) ctx.getSender().openContainer;
                    if(container.handler.getStackInSlot(0).isEmpty() || container.handler.getStackInSlot(0).getItem().getClass() == Coin.class) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invaliditem")));
                    } else {
                        ListNBT auctionList = (ListNBT) Auctioned.auctionedNBT.get("auction");
                        assert auctionList != null;

                        int items = auctionList.stream().mapToInt((inbt) -> {
                            CompoundNBT nbt = ((CompoundNBT) inbt);
                            if(nbt.getString("owner").equals(player)) {
                                return 1;
                            }
                            return 0;
                        }).sum();;

                        long price = ServerEvents.calculateListCost(items);

                        if(Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                            Auctioned.addListing(Auctioned.auctionedNBT, container.handler.getStackInSlot(0), this.amount, player);
                            container.handler.setStackInSlot(0, ItemStack.EMPTY);
                            Ledger.addBalance(Ledger.jsonFile, player, -price);
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncFeesPacket(ServerEvents.calculateListCost(items + 1), ServerEvents.calculateDailyListCost(items + 1) * (items + 1)));
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
                        }
                    }
                }
            }
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SUpdateBalance(Ledger.readBalance(Ledger.jsonFile, player), Teams.getPlayersTeam(Teams.jsonFile, player).balance));
        }
    }

    public static Function<PacketBuffer, CTransactionButton> decoder() {
        return pb -> {
            CTransactionButton packet = new CTransactionButton();
            packet.readPacketData(pb);
            return packet;
        };
    }

    public enum Button {
        DEPOSIT,
        WITHDRAW,
        PURCHASE,
        REMOVE,
        LIST_ITEM,
    }
}
