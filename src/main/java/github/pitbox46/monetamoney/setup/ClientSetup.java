package github.pitbox46.monetamoney.setup;

import github.pitbox46.monetamoney.containers.vault.*;
import github.pitbox46.monetamoney.screen.vault.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(final FMLClientSetupEvent event) {
        MenuScreens.<AccountTransactionContainer, AccountTransactionPage>register(Registration.ACC_TRANS.get(), (container, inv, name) -> new AccountTransactionPage(container, inv));
        MenuScreens.<AuctionHomeContainer, AuctionHomePage>register(Registration.AUCTION_HOME.get(), (container, inv, name) -> new AuctionHomePage(container, inv));
        MenuScreens.<AuctionBuyContainer, AuctionBuyPage>register(Registration.AUCTION_BUY.get(), (container, inv, name) -> new AuctionBuyPage(container, inv));
        MenuScreens.<AuctionListItemContainer, AuctionListItemPage>register(Registration.AUCTION_LIST.get(), (container, inv, name) -> new AuctionListItemPage(container, inv));
        MenuScreens.<ShopHomeContainer, ShopHomePage>register(Registration.SHOP_HOME.get(), (container, inv, name) -> new ShopHomePage(container, inv));
        MenuScreens.<ShopBuyContainer, ShopBuyPage>register(Registration.SHOP_BUY.get(), (container, inv, name) -> new ShopBuyPage(container, inv));
    }
}
