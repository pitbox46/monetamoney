package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.Config;
import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.client.*;
import github.pitbox46.monetamoney.network.server.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
