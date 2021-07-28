package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.blocks.VaultTile;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.items.Coin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommonProxy {
    private static final Logger LOGGER = LogManager.getLogger();
    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    public void handleCPageChange(NetworkEvent.Context ctx, CPageChange packet) {
        if(ctx.getSender() == null) return;

        switch(packet.page) {
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
                    ((AuctionHomeContainer) ctx.getSender().openContainer).changePage(packet.page == 6, packet.subpage);
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
                    NetworkHooks.openGui(ctx.getSender(), new INamedContainerProvider() {
                        @Override
                        public ITextComponent getDisplayName() {
                            return new TranslationTextComponent("screen.monetamoney.auctionhome");
                        }

                        @Override
                        public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                            return new AuctionHomeContainer(id, inv, packet.page == 6, packet.subpage);
                        }
                    }, buf -> buf.writeInt(packet.subpage));
                }
            } break;
            //Auction listings
            case 7: {
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

    public void handleCOpenBuyPage(NetworkEvent.Context ctx, COpenBuyPage packet) {
        if(ctx.getSender() == null) return;
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
        NetworkHooks.openGui(ctx.getSender(), new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("screen.monetamoney.auctionbuy");
            }

            @Override
            public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
                return new AuctionBuyContainer(id, inv, packet.nbt);
            }
        }, buf -> buf.writeCompoundTag(packet.nbt));
    }

    public void handleCOpenChunksPage(NetworkEvent.Context ctx, COpenChunksPage packet) {
        if(ctx.getSender() != null && ctx.getSender().getEntityWorld().getTileEntity(packet.pos) instanceof VaultTile) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + packet.pos.toLong());
            List<ChunkLoader> chunks = ServerEvents.CHUNK_MAP.get(team.toString());
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenChunksPage(team, chunks == null ? new ArrayList<>(0) : chunks));
        }
    }

    public void handleCOpenBalancePage(NetworkEvent.Context ctx, COpenBalancePage packet) {
        if(ctx.getSender() != null && ctx.getSender().getEntityWorld().getTileEntity(packet.pos) instanceof VaultTile) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + packet.pos.toLong());
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenBalancePage(Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName()), team.balance));
        }
    }

    public void handleCTeamButtonPress(NetworkEvent.Context ctx, CTeamButton packet) {
        if(ctx.getSender() != null && ctx.getSender().getEntityWorld().getTileEntity(packet.pos) instanceof VaultTile) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + packet.pos.toLong());
            String player = ctx.getSender().getGameProfile().getName();
            switch (packet.button) {
                case OPENPAGE: {
                    SOpenTeamsPage.Type type;
                    if(Teams.getPlayersTeam(Teams.jsonFile, player).isNull()) {
                        type = SOpenTeamsPage.Type.INNONE;
                    } else if (Teams.getPlayersTeam(Teams.jsonFile, player).equals(team)) {
                        type = SOpenTeamsPage.Type.INSAME;
                    } else {
                        type = SOpenTeamsPage.Type.INDIFFERENT;
                    }
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, type));
                } break;
                case JOIN: {
                    if(!team.isNull() && Teams.getPlayersTeam(Teams.jsonFile, player).isNull() && !team.locked) {
                        team.members.add(player);
                        Teams.updateTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam("unlisted", team.toString(), player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case SWITCH: {
                    if(!team.isNull() && !team.locked) {
                        Team playersTeam = Teams.getPlayersTeam(Teams.jsonFile, player);
                        playersTeam.members.remove(player);
                        if(playersTeam.members.size() == 0) {
                            Teams.removeTeam(Teams.jsonFile, playersTeam.toString());
                        } else {
                            Teams.updateTeam(Teams.jsonFile, playersTeam);
                        }
                        team.members.add(player);
                        Teams.updateTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam(playersTeam.toString(), team.toString(), player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case LEAVE: {
                    if(!team.isNull() && team.equals(Teams.getPlayersTeam(Teams.jsonFile, player))) {
                        team.members.remove(player);
                        Teams.updateTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam(team.toString(), "unlisted", player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INNONE));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case CREATE: {
                    if(team.isNull() && Teams.getPlayersTeam(Teams.jsonFile, player).isNull()) {
                        team = new Team(packet.pos, ctx.getSender().getServerWorld().getDimensionKey().getLocation(), player, Collections.singletonList(player), 0, false );
                        Teams.newTeam(Teams.jsonFile, team);

                        ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                        ServerEvents.movePlayerChunkToTeam("unlisted", team.toString(), player);

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case REMOVE: {
                    if(!team.isNull() && !Teams.getPlayersTeam(Teams.jsonFile, player).isNull() && team.leader.equals(Teams.getPlayersTeam(Teams.jsonFile, player).leader)) {
                        Teams.removeTeam(Teams.jsonFile, team.toString());

                        ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
                        ServerEvents.moveAllChunksToTeam(team.toString(), "unlisted");

                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(Team.NULL_TEAM, SOpenTeamsPage.Type.INNONE));
                    } else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.error")));
                    }
                } break;
                case LOCK: {
                    if(!team.isNull() && team.leader.equals(player)) {
                        team.locked = true;
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    }
                } break;
                case UNLOCK: {
                    if(!team.isNull() && team.leader.equals(player)) {
                        team.locked = false;
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenTeamsPage(team, SOpenTeamsPage.Type.INSAME));
                    }
                } break;
            }
        }
    }

    public void handleCButtonPress(NetworkEvent.Context ctx, CTransactionButton packet) {
        if(ctx.getSender() != null) {
            if(ctx.getSender().openContainer instanceof AccountTransactionContainer) {
                AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().openContainer;
                long balance = Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName());
                switch(packet.button) {
                    case WITHDRAW: {
                        if (packet.amount > Coin.MAX_SIZE || packet.amount <= 0) {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidsize")));
                        } else if (balance >= packet.amount) {
                            Ledger.addBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName(), -packet.amount);
                            ItemStack coins = Coin.createCoin(packet.amount, Outstanding.newCoin(Outstanding.jsonFile, packet.amount));
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
                            if (Outstanding.redeemCoin(Outstanding.jsonFile, ctx.getSender().getGameProfile().getName(), coins.getOrCreateTag().getUniqueId("uuid"))) {
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
                if(packet.button == CTransactionButton.Button.PURCHASE) {
                    AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().openContainer;
                    CompoundNBT itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
                    int price = itemNBT.getInt("price");
                    if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.listingerror")));
                    }
                    else if (Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName()) >= price) {
                        Ledger.addBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName(), -price);

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
                else if(packet.button == CTransactionButton.Button.REMOVE) {
                    AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().openContainer;
                    CompoundNBT itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
                    int price = itemNBT.getInt("price");
                    if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.listingerror")));
                    }
                    else if(!Auctioned.confirmOwner(Auctioned.auctionedNBT, itemNBT, ctx.getSender().getGameProfile().getName())) {
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
                if(packet.button == CTransactionButton.Button.LIST_ITEM) {
                    AuctionListItemContainer container = (AuctionListItemContainer) ctx.getSender().openContainer;
                    if(container.handler.getStackInSlot(0).isEmpty()) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invaliditem")));
                    } else {
                        Auctioned.addListing(Auctioned.auctionedNBT, container.handler.getStackInSlot(0), packet.amount, ctx.getSender().getGameProfile().getName());
                        container.handler.setStackInSlot(0, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public void handleCTeamTransactionButton(NetworkEvent.Context ctx, CTeamTransactionButton packet) {
        if(ctx.getSender() != null) {
            if (ctx.getSender().openContainer instanceof AccountTransactionContainer) {
                AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().openContainer;
                Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + packet.pos.toLong());
                if(team.isNull()) {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.noteam")));
                    return;
                }
                if(team.members.contains(ctx.getSender().getGameProfile().getName())) {
                    if(packet.button == CTeamTransactionButton.Button.WITHDRAW) {
                        if (packet.amount > Coin.MAX_SIZE || packet.amount <= 0) {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidsize")));
                        } else if (team.balance >= packet.amount) {
                            team.balance -= packet.amount;
                            ItemStack coins = Coin.createCoin(packet.amount, Outstanding.newCoin(Outstanding.jsonFile, packet.amount));
                            if (container.handler.getStackInSlot(0).isEmpty()) {
                                container.handler.setStackInSlot(0, coins);
                            } else {
                                ctx.getSender().inventory.placeItemBackInInventory(ctx.getSender().getEntityWorld(), coins);
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
                        }
                    }
                    else if (packet.button == CTeamTransactionButton.Button.DEPOSIT) {
                        if (container.handler.getStackInSlot(0).getItem() instanceof Coin) {
                            ItemStack coins = container.handler.getStackInSlot(0);
                            if (Outstanding.redeemTeamCoin(Outstanding.jsonFile, team, coins.getOrCreateTag().getUniqueId("uuid"))) {
                                container.handler.setStackInSlot(0, ItemStack.EMPTY);
                            } else {
                                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidcoin")));
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invaliditem")));
                        }
                    }
                    Teams.updateTeam(Teams.jsonFile, team);
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.noaccess")));
                }
            }
        }
    }

    public void handleCAnchorButtonPress(NetworkEvent.Context ctx, CAnchorButton packet) {
        Team team = Teams.getPlayersTeam(Teams.jsonFile, ctx.getSender().getGameProfile().getName());
        if(ctx.getSender() != null && !team.isNull()) {
            if(packet.enable) {
                ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                boolean flag = false;
                for(ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    if(ctx.getSender().getEntityWorld().getDimensionKey().getLocation().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(packet.pos)) {
                        chunkLoader.status = ChunkLoader.Status.ON;
                        flag = true;
                        ServerEvents.loadNewChunk(ctx.getSender().getServerWorld(), team, chunkLoader);
                        break;
                    }
                }
                if(!flag) {
                    ChunkLoader chunkLoader = new ChunkLoader(ctx.getSender().getServerWorld().getDimensionKey().getLocation(), packet.pos, ctx.getSender().getGameProfile().getName(), ChunkLoader.Status.ON);
                    ServerEvents.loadNewChunk(ctx.getSender().getServerWorld(), team, chunkLoader);
                    ServerEvents.CHUNK_MAP.get(team.toString()).add(chunkLoader);
                }
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenAnchorPage(true));
            } else {
                ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                boolean flag = false;
                for(ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    if(ctx.getSender().getEntityWorld().getDimensionKey().getLocation().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(packet.pos)) {
                        chunkLoader.status = ChunkLoader.Status.OFF;
                        flag = true;
                        break;
                    }
                }
                if(!flag) {
                    ServerEvents.CHUNK_MAP.get(team.toString()).add(new ChunkLoader(ctx.getSender().getServerWorld().getDimensionKey().getLocation(), packet.pos, ctx.getSender().getGameProfile().getName(), ChunkLoader.Status.OFF));
                }
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenAnchorPage(false));
            }
        }
    }

    public void handleCUpdateBalance(NetworkEvent.Context ctx, CUpdateBalance packet) {
        if(ctx.getSender() != null && ctx.getSender().getEntityWorld().getTileEntity(packet.pos) instanceof VaultTile) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + packet.pos.toLong());
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SUpdateBalance(Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName()), team.balance));
        }
    }

    //Client
    public void handleSOpenMainPage(NetworkEvent.Context ctx) {
    }

    public void handleSOpenBalancePage(NetworkEvent.Context ctx, SOpenBalancePage packet) {
    }

    public void handleSUpdateBalance(NetworkEvent.Context ctx, SUpdateBalance packet) {
    }

    public void handleSSyncAuctionNBT(NetworkEvent.Context ctx, SSyncAuctionNBT packet) {
    }

    public void handleSDenyItemUse(NetworkEvent.Context ctx, SDenyUseItem packet) {
    }

    public void handleSOpenTeamsPage(NetworkEvent.Context ctx, SOpenTeamsPage packet) {
    }

    public void handleSOpenChunksPage(NetworkEvent.Context ctx, SOpenChunksPage packet) {
    }

    public void handleSOpenAnchorPage(NetworkEvent.Context ctx, SOpenAnchorPage packet) {
    }

    public void handleSGuiStatusMessage(NetworkEvent.Context ctx, SGuiStatusMessage packet) {
    }
}
