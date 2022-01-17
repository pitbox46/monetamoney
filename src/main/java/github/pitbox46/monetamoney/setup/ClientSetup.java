package github.pitbox46.monetamoney.setup;

import github.pitbox46.monetamoney.containers.vault.*;
import github.pitbox46.monetamoney.screen.vault.*;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(final FMLClientSetupEvent event) {
        ScreenManager.<AccountTransactionContainer, AccountTransactionPage>registerFactory(Registration.ACC_TRANS.get(), (container, inv, name) -> new AccountTransactionPage(container, inv));
        ScreenManager.<AuctionHomeContainer, AuctionHomePage>registerFactory(Registration.AUCTION_HOME.get(), (container, inv, name) -> new AuctionHomePage(container, inv));
        ScreenManager.<AuctionBuyContainer, AuctionBuyPage>registerFactory(Registration.AUCTION_BUY.get(), (container, inv, name) -> new AuctionBuyPage(container, inv));
        ScreenManager.<AuctionListItemContainer, AuctionListItemPage>registerFactory(Registration.AUCTION_LIST.get(), (container, inv, name) -> new AuctionListItemPage(container, inv));
        ScreenManager.<ShopHomeContainer, ShopHomePage>registerFactory(Registration.SHOP_HOME.get(), (container, inv, name) -> new ShopHomePage(container, inv));
        ScreenManager.<ShopBuyContainer, ShopBuyPage>registerFactory(Registration.SHOP_BUY.get(), (container, inv, name) -> new ShopBuyPage(container, inv));
    }
}
