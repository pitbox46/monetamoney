package github.pitbox46.monetamoney;

import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.screen.vault.AccountTransactionPage;
import github.pitbox46.monetamoney.screen.vault.AuctionBuyPage;
import github.pitbox46.monetamoney.screen.vault.AuctionHomePage;
import github.pitbox46.monetamoney.screen.vault.AuctionListItemPage;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(final FMLClientSetupEvent event) {
        ScreenManager.<AccountTransactionContainer, AccountTransactionPage>registerFactory(Registration.ACC_TRANS.get(), (container, inv, name) -> new AccountTransactionPage(container, inv));
        ScreenManager.<AuctionHomeContainer, AuctionHomePage>registerFactory(Registration.AUCTION_HOME.get(), (container, inv, name) -> new AuctionHomePage(container, inv));
        ScreenManager.<AuctionBuyContainer, AuctionBuyPage>registerFactory(Registration.AUCTION_BUY.get(), (container, inv, name) -> new AuctionBuyPage(container, inv));
        ScreenManager.<AuctionListItemContainer, AuctionListItemPage>registerFactory(Registration.AUCTION_LIST.get(), (container, inv, name) -> new AuctionListItemPage(container, inv));
    }
}
