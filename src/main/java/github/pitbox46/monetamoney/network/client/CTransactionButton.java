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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CTransactionButton implements IPacket {
    public int amount;
    public Button button;

    public CTransactionButton() {
    }

    public CTransactionButton(int amount, Button button) {
        this.amount = amount;
        this.button = button;
    }

    public static Function<FriendlyByteBuf, CTransactionButton> decoder() {
        return pb -> {
            CTransactionButton packet = new CTransactionButton();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
        this.button = buf.readEnum(Button.class);
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeEnum(this.button);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if (ctx.getSender() != null) {
            String player = ctx.getSender().getGameProfile().getName();

            this.button.process.accept(ctx, this);

            Team team = Teams.getPlayersTeam(Teams.jsonFile, player);
            int chunks = ServerEvents.CHUNK_MAP.containsKey(team.toString()) ? (int) ServerEvents.CHUNK_MAP.get(team.toString()).stream().filter(c -> c.status == ChunkLoader.Status.ON || c.status == ChunkLoader.Status.STUCK).count() : 0;
            long dailyChunkFee = ServerEvents.calculateChunksCost(chunks) * chunks;

            ListTag auctionList = (ListTag) Auctioned.auctionedNBT.get("auction");
            assert auctionList != null;

            int listings = (int) auctionList.stream().filter(inbt -> ((CompoundTag) inbt).getString("owner").equals(player)).count();

            long dailyListingFee = ServerEvents.calculateDailyListCost(listings) * listings;

            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SUpdateBalance(Ledger.readBalance(Ledger.jsonFile, player), team.balance, dailyChunkFee, dailyListingFee));
        }
    }

    public enum Button {
        DEPOSIT((ctx, packet) -> {
            if (!(ctx.getSender().containerMenu instanceof AccountTransactionContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            long balance = Ledger.readBalance(Ledger.jsonFile, player);
            AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().containerMenu;
            if (packet.amount > Coin.MAX_SIZE || packet.amount <= 0) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invalidsize")));
            } else if (balance >= packet.amount) {
                Ledger.addBalance(Ledger.jsonFile, player, -packet.amount);
                ItemStack coins = Coin.createCoin(packet.amount, Outstanding.newCoin(Outstanding.jsonFile, packet.amount, player));
                if (container.handler.getStackInSlot(0).isEmpty()) {
                    container.handler.setStackInSlot(0, coins);
                } else {
                    ctx.getSender().getInventory().placeItemBackInInventory(coins);
                }
            } else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.nomoney")));
            }
        }),
        WITHDRAW((ctx, packet) -> {
            if (!(ctx.getSender().containerMenu instanceof AccountTransactionContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().containerMenu;
            if (container.handler.getStackInSlot(0).getItem() instanceof Coin) {
                ItemStack coins = container.handler.getStackInSlot(0);
                if (coins.getOrCreateTag().hasUUID("uuid") && Outstanding.redeemCoin(Outstanding.jsonFile, player, coins.getTag().getUUID("uuid"))) {
                    container.handler.setStackInSlot(0, ItemStack.EMPTY);
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invalidcoin")));
                }
            } else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invaliditem")));
            }
        }),
        BUY((ctx, packet) -> {
            if (!(ctx.getSender().containerMenu instanceof AbstractBuyContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            AbstractBuyContainer container = (AbstractBuyContainer) ctx.getSender().containerMenu;
            CompoundTag itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
            int price = itemNBT.getInt("price");
            boolean shopItem = price == 0;
            if (shopItem) {
                price = itemNBT.getInt("buyPrice");
                if (!Auctioned.buyFromShop(itemNBT)) {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.nostock")));
                    return;
                }
            }
            if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.listingerror")));
                return;
            }
            if (Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                Ledger.addBalance(Ledger.jsonFile, player, -price);

                CompoundTag removedTagNBT = container.handler.getStackInSlot(0).save(new CompoundTag());
                removedTagNBT.getCompound("tag").remove("uuid");
                if (container.getClass().equals(AuctionBuyContainer.class)) {
                    removedTagNBT.getCompound("tag").remove("owner");
                    removedTagNBT.getCompound("tag").remove("price");
                } else if (container.getClass().equals(ShopBuyContainer.class)) {
                    removedTagNBT.getCompound("tag").remove("buyPrice");
                    removedTagNBT.getCompound("tag").remove("sellPrice");
                }
                if (removedTagNBT.getCompound("tag").isEmpty()) {
                    removedTagNBT.remove("tag");
                }
                ctx.getSender().getInventory().placeItemBackInInventory(ItemStack.of(removedTagNBT));
                String owner = itemNBT.getString("owner");
                if (!owner.equals("shop listing") && !owner.isEmpty()) {
                    Ledger.addBalance(Ledger.jsonFile, owner, price);
                    Auctioned.deleteListing(Auctioned.auctionedNBT, itemNBT.getUUID("uuid"));
                    container.handler.setStackInSlot(0, ItemStack.EMPTY);
                }
            } else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.nomoney")));
            }
        }),
        REMOVE((ctx, packet) -> {
            if (!(ctx.getSender().containerMenu instanceof AuctionBuyContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().containerMenu;
            CompoundTag itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
            if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.listingerror")));
            } else if (!Auctioned.confirmOwner(Auctioned.auctionedNBT, itemNBT, player)) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.notowner")));
            } else {
                CompoundTag removedTagNBT = container.handler.getStackInSlot(0).save(new CompoundTag());
                removedTagNBT.getCompound("tag").remove("uuid");
                removedTagNBT.getCompound("tag").remove("owner");
                removedTagNBT.getCompound("tag").remove("price");
                if (removedTagNBT.getCompound("tag").isEmpty()) {
                    removedTagNBT.remove("tag");
                }
                ctx.getSender().getInventory().placeItemBackInInventory(ItemStack.of(removedTagNBT));
                Auctioned.deleteListing(Auctioned.auctionedNBT, itemNBT.getUUID("uuid"));
                container.handler.setStackInSlot(0, ItemStack.EMPTY);
            }
        }),
        SELL((ctx, packet) -> {
            if (!(ctx.getSender().containerMenu instanceof ShopBuyContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            ShopBuyContainer container = (ShopBuyContainer) ctx.getSender().containerMenu;
            ItemStack item = container.handler.getStackInSlot(0);
            CompoundTag itemNBT = item.getOrCreateTag();
            int price = itemNBT.getInt("sellPrice");
            if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.listingerror")));
                return;
            }
            int quantity = 0;
            List<ItemStack> slots = new ArrayList<>();
            Inventory inv = ctx.getSender().getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (!inv.getItem(i).hasTag()) {
                    if (inv.getItem(i).getItem().equals(item.getItem())) {
                        quantity += inv.getItem(i).getCount();
                        slots.add(inv.getItem(i));
                    }
                } else {
                    if (ItemStack.matches(inv.getItem(i), item)) {
                        quantity += inv.getItem(i).getCount();
                        slots.add(inv.getItem(i));
                    }
                }
            }
            if (quantity >= item.getCount()) {
                if (!Auctioned.sellToShop(itemNBT)) {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.error")));
                    return;
                }
                int itemsLeft = item.getCount();
                for (ItemStack stack : slots) {
                    int maxShrink = Math.min(stack.getCount(), itemsLeft);
                    stack.shrink(maxShrink);
                    itemsLeft -= maxShrink;
                    if (itemsLeft == 0) break;
                }
            } else {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.noitems")));
                return;
            }
            Ledger.addBalance(Ledger.jsonFile, player, price);
        }),
        LIST_ITEM((ctx, packet) -> {
            if (!(ctx.getSender().containerMenu instanceof AuctionListItemContainer)) return;
            String player = ctx.getSender().getGameProfile().getName();
            AuctionListItemContainer container = (AuctionListItemContainer) ctx.getSender().containerMenu;
            if (container.handler.getStackInSlot(0).isEmpty() || container.handler.getStackInSlot(0).getItem().getClass() == Coin.class) {
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invaliditem")));
            } else {
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

                if (Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                    Auctioned.addListing(Auctioned.auctionedNBT, container.handler.getStackInSlot(0), packet.amount, player);
                    container.handler.setStackInSlot(0, ItemStack.EMPTY);
                    Ledger.addBalance(Ledger.jsonFile, player, -price);
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncFeesPacket(ServerEvents.calculateListCost(items + 1), ServerEvents.calculateDailyListCost(items + 1) * (items + 1)));
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.nomoney")));
                }
            }
        });

        public final BiConsumer<NetworkEvent.Context, CTransactionButton> process;

        Button(BiConsumer<NetworkEvent.Context, CTransactionButton> consumer) {
            process = consumer;
        }
    }
}
