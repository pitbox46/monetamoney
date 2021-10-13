package github.pitbox46.monetamoney;

import github.pitbox46.monetamoney.commands.ModCommands;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.CommonProxy;
import github.pitbox46.monetamoney.screen.vault.*;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MonetaMoney.MODID)
public class MonetaMoney {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "monetamoney";
    public static ItemGroup MOD_TAB = new ItemGroup("monetamoney") {
        public ItemStack createIcon() {
            return new ItemStack(Registration.COIN.get());
        }
    };
    public static CommonProxy PROXY;

    public MonetaMoney() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        Registration.init();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
