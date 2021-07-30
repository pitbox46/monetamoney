package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.client.*;
import github.pitbox46.monetamoney.network.server.*;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "3.2.1";
    public static SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("monetamoney", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int ID = 0;

    public static void init() {
        //SERVER -> CLIENT
        CHANNEL.registerMessage(
                ID++,
                SOpenMainPage.class,
                (msg, pb) -> {
                },
                pb -> new SOpenMainPage(),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSOpenMainPage(ctx.get()));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SOpenBalancePage.class,
                (msg, pb) -> {
                    pb.writeLong(msg.personalBal);
                    pb.writeLong(msg.teamBal);
                },
                pb -> new SOpenBalancePage(pb.readLong(), pb.readLong()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSOpenBalancePage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SOpenTeamsPage.class,
                (msg, pb) -> {
                    msg.team.writeTeam(pb);
                    pb.writeEnumValue(msg.type);
                },
                pb -> new SOpenTeamsPage(Team.readTeam(pb), pb.readEnumValue(SOpenTeamsPage.Type.class)),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSOpenTeamsPage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SOpenChunksPage.class,
                (msg, pb) -> {
                    msg.team.writeTeam(pb);
                    pb.writeInt(msg.chunks.size());
                    for(ChunkLoader chunkLoader: msg.chunks) {
                        chunkLoader.writeChunk(pb);
                    }
                },
                pb -> {
                    Team team = Team.readTeam(pb);
                    List<ChunkLoader> chunks = new ArrayList<>();
                    int size = pb.readInt();
                    for(int i = 0; i < size; i++) {
                        chunks.add(ChunkLoader.readChunk(pb));
                    }
                    return new SOpenChunksPage(team, chunks);
                },
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSOpenChunksPage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SOpenAnchorPage.class,
                (msg, pb) -> {
                    pb.writeBoolean(msg.active);
                },
                pb -> new SOpenAnchorPage(pb.readBoolean()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSOpenAnchorPage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SGuiStatusMessage.class,
                (msg, pb) -> {
                    pb.writeTextComponent(msg.message);
                },
                pb -> new SGuiStatusMessage(pb.readTextComponent()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSGuiStatusMessage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SUpdateBalance.class,
                (msg, pb) -> {
                    pb.writeLong(msg.personalBal);
                    pb.writeLong(msg.teamBal);
                },
                pb -> new SUpdateBalance(pb.readLong(), pb.readLong()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSUpdateBalance(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SSyncAuctionNBT.class,
                (msg, pb) -> {
                    pb.writeCompoundTag(msg.nbt);
                },
                pb -> new SSyncAuctionNBT(pb.readCompoundTag()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSSyncAuctionNBT(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                SDenyUseItem.class,
                (msg, pb) -> {
                    pb.writeEnumValue(msg.hand);
                    pb.writeItemStack(msg.item, false);
                },
                pb -> new SDenyUseItem(pb.readEnumValue(Hand.class), pb.readItemStack()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleSDenyItemUse(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });

        //CLIENT -> SERVER
        CHANNEL.registerMessage(
                ID++,
                CPageChange.class,
                (msg, pb) -> {
                    pb.writeShort(msg.page);
                    pb.writeShort(msg.subpage);
                },
                pb -> new CPageChange(pb.readShort(), pb.readShort()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCPageChange(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                COpenBalancePage.class,
                (msg, pb) -> {
                    pb.writeBlockPos(msg.pos);
                },
                pb -> new COpenBalancePage(pb.readBlockPos()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCOpenBalancePage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                COpenBuyPage.class,
                (msg, pb) -> {
                    pb.writeCompoundTag(msg.nbt);
                },
                pb -> new COpenBuyPage(pb.readCompoundTag()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCOpenBuyPage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                COpenChunksPage.class,
                (msg, pb) -> {
                    pb.writeBlockPos(msg.pos);
                },
                pb -> new COpenChunksPage(pb.readBlockPos()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCOpenChunksPage(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                CTransactionButton.class,
                (msg, pb) -> {
                    pb.writeInt(msg.amount);
                    pb.writeEnumValue(msg.button);
                },
                pb -> new CTransactionButton(pb.readInt(), pb.readEnumValue(CTransactionButton.Button.class)),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCButtonPress(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                CTeamTransactionButton.class,
                (msg, pb) -> {
                    pb.writeBlockPos(msg.pos);
                    pb.writeInt(msg.amount);
                    pb.writeEnumValue(msg.button);
                },
                pb -> new CTeamTransactionButton(pb.readBlockPos(), pb.readInt(), pb.readEnumValue(CTeamTransactionButton.Button.class)),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCTeamTransactionButton(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                CTeamButton.class,
                (msg, pb) -> {
                    pb.writeBlockPos(msg.pos);
                    pb.writeEnumValue(msg.button);
                },
                pb -> new CTeamButton(pb.readBlockPos(), pb.readEnumValue(CTeamButton.Button.class)),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCTeamButtonPress(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                CAnchorButton.class,
                (msg, pb) -> {
                    pb.writeBlockPos(msg.pos);
                    pb.writeBoolean(msg.enable);
                },
                pb -> new CAnchorButton(pb.readBlockPos(), pb.readBoolean()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCAnchorButtonPress(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
        CHANNEL.registerMessage(
                ID++,
                CUpdateBalance.class,
                (msg, pb) -> {
                    pb.writeBlockPos(msg.pos);
                },
                pb -> new CUpdateBalance(pb.readBlockPos()),
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> MonetaMoney.PROXY.handleCUpdateBalance(ctx.get(), msg));
                    ctx.get().setPacketHandled(true);
                });
    }
}