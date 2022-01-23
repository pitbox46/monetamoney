package github.pitbox46.monetamoney;

import github.pitbox46.monetamoney.commands.ModCommands;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.CommonProxy;
import github.pitbox46.monetamoney.setup.ClientSetup;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MonetaMoney.MODID)
public class MonetaMoney {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "monetamoney";
    public static CreativeModeTab MOD_TAB = new CreativeModeTab("monetamoney") {
        public ItemStack makeIcon() {
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
