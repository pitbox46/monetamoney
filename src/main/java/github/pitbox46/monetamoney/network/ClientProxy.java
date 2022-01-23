package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.server.*;
import github.pitbox46.monetamoney.screen.AnchorScreen;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.vault.BalancePage;
import github.pitbox46.monetamoney.screen.vault.ChunksPage;
import github.pitbox46.monetamoney.screen.vault.MainPage;
import github.pitbox46.monetamoney.screen.vault.TeamsPage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

public class ClientProxy extends CommonProxy {
    public static long personalBalance = 0;
    public static long teamBalance = 0;
    public static long dailyChunkFee = 0;
    public static long dailyListFee = 0;
    public static long listFee = 0;

    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void handleSDenyUseItemPacket(NetworkEvent.Context ctx, SDenyUseItem packet) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setItemInHand(packet.hand, packet.item);
        }
    }

    @Override
    public void handleSGuiStatusMessage(NetworkEvent.Context ctx, SGuiStatusMessage packet) {
        if (Minecraft.getInstance().screen instanceof IStatusable) {
            ((IStatusable) Minecraft.getInstance().screen).setStatus(packet.message);
        }
    }

    @Override
    public void handleSOpenAnchorPage(NetworkEvent.Context ctx, SOpenAnchorPage packet) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().setScreen(new AnchorScreen(packet.active));
        }
    }

    @Override
    public void handleSOpenBalancePage(NetworkEvent.Context ctx, SOpenBalancePage packet) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().setScreen(new BalancePage());
            ClientProxy.personalBalance = packet.personalBal;
            ClientProxy.teamBalance = packet.teamBal;
        }
    }

    @Override
    public void handleSOpenChunksPage(NetworkEvent.Context ctx, SOpenChunksPage packet) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().setScreen(new ChunksPage(packet.team, packet.chunks));
        }
    }

    @Override
    public void handleSOpenMainPage(NetworkEvent.Context ctx, SOpenMainPage packet) {
        Minecraft.getInstance().setScreen(new MainPage());
    }

    @Override
    public void handleSOpenTeamsPage(NetworkEvent.Context ctx, SOpenTeamsPage packet) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().setScreen(new TeamsPage(packet.team, packet.type));
        }
    }

    @Override
    public void handleSSyncAuctionNBT(NetworkEvent.Context ctx, SSyncAuctionNBT packet) {
        Auctioned.auctionedNBT = packet.nbt;
    }

    @Override
    public void handleSSyncFeesPacket(NetworkEvent.Context ctx, SSyncFeesPacket packet) {
        listFee = packet.fee;
        dailyListFee = packet.dailyFee;
    }

    @Override
    public void handleSUpdateBalance(NetworkEvent.Context ctx, SUpdateBalance packet) {
        personalBalance = packet.personalBal;
        teamBalance = packet.teamBal;
        dailyListFee = packet.dailyListings;
        dailyChunkFee = packet.dailyChunks;
    }
}
