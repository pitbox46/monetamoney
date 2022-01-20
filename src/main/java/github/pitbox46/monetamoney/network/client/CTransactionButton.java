package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.containers.vault.*;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SGuiStatusMessage;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import github.pitbox46.monetamoney.network.server.SUpdateBalance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

            this.button.process.accept(ctx, this);

            Team team = Teams.getPlayersTeam(Teams.jsonFile, player);
            int chunks = ServerEvents.CHUNK_MAP.containsKey(team.toString()) ? (int) ServerEvents.CHUNK_MAP.get(team.toString()).stream().filter(c -> c.status == ChunkLoader.Status.ON || c.status == ChunkLoader.Status.STUCK).count() : 0;
            long dailyChunkFee = ServerEvents.calculateChunksCost(chunks) * chunks;

            ListNBT auctionList = (ListNBT) Auctioned.auctionedNBT.get("auction");
            assert auctionList != null;

            int listings = (int) auctionList.stream().filter(inbt -> ((CompoundNBT) inbt).getString("owner").equals(player)).count();

            long dailyListingFee = ServerEvents.calculateDailyListCost(listings) * listings;

            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SUpdateBalance(Ledger.readBalance(Ledger.jsonFile, player), team.balance, dailyChunkFee, dailyListingFee));
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
        DEPOSIT((ctx, packet) -> {
            if(!(ctx.getSender().openContainer instanceof AccountTransactionContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            long balance = Ledger.readBalance(Ledger.jsonFile, player);
            AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().openContainer;
            if (packet.amount > Coin.MAX_SIZE || packet.amount <= 0) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidsize")));
            } else if (balance >= packet.amount) {
                Ledger.addBalance(Ledger.jsonFile, player, -packet.amount);
                ItemStack coins = Coin.createCoin(packet.amount, Outstanding.newCoin(Outstanding.jsonFile, packet.amount, player));
                if (container.handler.getStackInSlot(0).isEmpty()) {
                    container.handler.setStackInSlot(0, coins);
                } else {
                    ctx.getSender().inventory.placeItemBackInInventory(ctx.getSender().getEntityWorld(), coins);
                }
            } else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
            }
        }),
        WITHDRAW((ctx, packet) -> {
            if(!(ctx.getSender().openContainer instanceof AccountTransactionContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().openContainer;
            if (container.handler.getStackInSlot(0).getItem() instanceof Coin) {
                ItemStack coins = container.handler.getStackInSlot(0);
                if (coins.getOrCreateTag().hasUniqueId("uuid") && Outstanding.redeemCoin(Outstanding.jsonFile, player, coins.getTag().getUniqueId("uuid"))) {
                    container.handler.setStackInSlot(0, ItemStack.EMPTY);
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidcoin")));
                }
            } else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invaliditem")));
            }
        }),
        BUY((ctx, packet) -> {
            if(!(ctx.getSender().openContainer instanceof AbstractBuyContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            AbstractBuyContainer container = (AbstractBuyContainer) ctx.getSender().openContainer;
            CompoundNBT itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
            int price = itemNBT.getInt("price");
            boolean shopItem = price == 0;
            if(shopItem) {
                price = itemNBT.getInt("buyPrice");
                if(!Auctioned.buyFromShop(itemNBT)) {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nostock")));
                    return;
                }
            }
            if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.listingerror")));
                return;
            }
            if (Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                Ledger.addBalance(Ledger.jsonFile, player, -price);

                CompoundNBT removedTagNBT = container.handler.getStackInSlot(0).write(new CompoundNBT());
                removedTagNBT.getCompound("tag").remove("uuid");
                if(container.getClass().equals(AuctionBuyContainer.class)) {
                    removedTagNBT.getCompound("tag").remove("owner");
                    removedTagNBT.getCompound("tag").remove("price");
                }
                else if(container.getClass().equals(ShopBuyContainer.class)) {
                    removedTagNBT.getCompound("tag").remove("buyPrice");
                    removedTagNBT.getCompound("tag").remove("sellPrice");
                }
                if (removedTagNBT.getCompound("tag").isEmpty()) {
                    removedTagNBT.remove("tag");
                }
                ctx.getSender().inventory.placeItemBackInInventory(ctx.getSender().getEntityWorld(), ItemStack.read(removedTagNBT));
                String owner = itemNBT.getString("owner");
                if (!owner.equals("shop listing") && !owner.isEmpty()) {
                    Ledger.addBalance(Ledger.jsonFile, owner, price);
                    Auctioned.deleteListing(Auctioned.auctionedNBT, itemNBT.getUniqueId("uuid"));
                    container.handler.setStackInSlot(0, ItemStack.EMPTY);
                }
            }
            else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
            }
        }),
        REMOVE((ctx, packet) -> {
            if(!(ctx.getSender().openContainer instanceof AuctionBuyContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().openContainer;
            CompoundNBT itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
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
        }),
        SELL((ctx, packet) -> {
            if(!(ctx.getSender().openContainer instanceof ShopBuyContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            ShopBuyContainer container = (ShopBuyContainer) ctx.getSender().openContainer;
            ItemStack item = container.handler.getStackInSlot(0);
            CompoundNBT itemNBT = item.getOrCreateTag();
            int price = itemNBT.getInt("sellPrice");
            if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.listingerror")));
                return;
            }
            int quantity = 0;
            List<ItemStack> slots = new ArrayList<>();
            PlayerInventory inv = ctx.getSender().inventory;
            for(int i = 0; i < inv.getSizeInventory(); i++) {
                if(!inv.getStackInSlot(i).hasTag()) {
                    if (inv.getStackInSlot(i).getItem().equals(item.getItem())) {
                        quantity += inv.getStackInSlot(i).getCount();
                        slots.add(inv.getStackInSlot(i));
                    }
                }
                else {
                    if (ItemStack.areItemStacksEqual(inv.getStackInSlot(i), item)) {
                        quantity += inv.getStackInSlot(i).getCount();
                        slots.add(inv.getStackInSlot(i));
                    }
                }
            }
            if(quantity >= item.getCount()) {
                if(!Auctioned.sellToShop(itemNBT)) {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    return;
                }
                int itemsLeft = item.getCount();
                for(ItemStack stack : slots) {
                    int maxShrink = Math.min(stack.getCount(), itemsLeft);
                    stack.shrink(maxShrink);
                    itemsLeft -= maxShrink;
                    if(itemsLeft == 0) break;
                }
            } else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.noitems")));
                return;
            }
            Ledger.addBalance(Ledger.jsonFile, player, price);
        }),
        LIST_ITEM((ctx, packet) -> {
            if(!(ctx.getSender().openContainer instanceof AuctionListItemContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
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
                }).sum();

                long price = ServerEvents.calculateListCost(items);

                if(Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                    Auctioned.addListing(Auctioned.auctionedNBT, container.handler.getStackInSlot(0), packet.amount, player);
                    container.handler.setStackInSlot(0, ItemStack.EMPTY);
                    Ledger.addBalance(Ledger.jsonFile, player, -price);
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncFeesPacket(ServerEvents.calculateListCost(items + 1), ServerEvents.calculateDailyListCost(items + 1) * (items + 1)));
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
                }
            }
        });

        public final BiConsumer<NetworkEvent.Context, CTransactionButton> process;
        Button(BiConsumer<NetworkEvent.Context, CTransactionButton> consumer) {
            process = consumer;
        }
    }
}
