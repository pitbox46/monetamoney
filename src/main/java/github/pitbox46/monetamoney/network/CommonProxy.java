package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.network.server.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonProxy {
    private static final Logger LOGGER = LogManager.getLogger();

    public CommonProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();
    }

    public void handleSDenyUseItemPacket(NetworkEvent.Context ctx, SDenyUseItem packet) {
    }

    public void handleSGuiStatusMessage(NetworkEvent.Context ctx, SGuiStatusMessage packet) {
    }

    public void handleSOpenAnchorPage(NetworkEvent.Context ctx, SOpenAnchorPage packet) {
    }

    public void handleSOpenBalancePage(NetworkEvent.Context ctx, SOpenBalancePage packet) {
    }

    public void handleSOpenChunksPage(NetworkEvent.Context ctx, SOpenChunksPage packet) {
    }

    public void handleSOpenMainPage(NetworkEvent.Context ctx, SOpenMainPage packet) {
    }

    public void handleSOpenTeamsPage(NetworkEvent.Context ctx, SOpenTeamsPage packet) {
    }

    public void handleSSyncAuctionNBT(NetworkEvent.Context ctx, SSyncAuctionNBT packet) {
    }

    public void handleSSyncFeesPacket(NetworkEvent.Context ctx, SSyncFeesPacket packet) {
    }

    public void handleSUpdateBalance(NetworkEvent.Context ctx, SUpdateBalance packet) {
    }
}
