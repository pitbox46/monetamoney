package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.client.*;
import github.pitbox46.monetamoney.network.server.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "3.2.1";
    public static SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("monetamoney", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int ID = 0;

    public static void init() {
        registerPacket(SDenyUseItem.class, SDenyUseItem.decoder());
        registerPacket(SGuiStatusMessage.class, SGuiStatusMessage.decoder());
        registerPacket(SOpenAnchorPage.class, SOpenAnchorPage.decoder());
        registerPacket(SOpenBalancePage.class, SOpenBalancePage.decoder());
        registerPacket(SOpenChunksPage.class, SOpenChunksPage.decoder());
        registerPacket(SOpenMainPage.class, SOpenMainPage.decoder());
        registerPacket(SOpenTeamsPage.class, SOpenTeamsPage.decoder());
        registerPacket(SSyncAuctionNBT.class, SSyncAuctionNBT.decoder());
        registerPacket(SSyncFeesPacket.class, SSyncFeesPacket.decoder());
        registerPacket(SUpdateBalance.class, SUpdateBalance.decoder());

        registerPacket(CAnchorButton.class, CAnchorButton.decoder());
        registerPacket(COpenBalancePage.class, COpenBalancePage.decoder());
        registerPacket(COpenBuyPage.class, COpenBuyPage.decoder());
        registerPacket(COpenChunksPage.class, COpenChunksPage.decoder());
        registerPacket(CPageChange.class, CPageChange.decoder());
        registerPacket(CTeamButton.class, CTeamButton.decoder());
        registerPacket(CTeamTransactionButton.class, CTeamTransactionButton.decoder());
        registerPacket(CTransactionButton.class, CTransactionButton.decoder());
        registerPacket(CUpdateBalance.class, CUpdateBalance.decoder());
    }

    public static <T extends IPacket> void registerPacket(Class<T> packetClass, Function<PacketBuffer,T> decoder) {
        CHANNEL.registerMessage(
                ID++,
                packetClass,
                IPacket::writePacketData,
                decoder,
                (msg, ctx) -> {
                    ctx.get().enqueueWork(() -> msg.processPacket(ctx.get()));
                    ctx.get().setPacketHandled(true);
                }
        );
    }
}