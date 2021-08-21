package github.pitbox46.monetamoney.setup;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.blocks.Vault;

import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MonetaMoney.MODID);
    static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MonetaMoney.MODID);
    static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MonetaMoney.MODID);
    static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MonetaMoney.MODID);

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
        CONTAINERS.register(modEventBus);
    }

    public static final RegistryObject<Coin> COIN = ITEMS.register("coin", () -> new Coin(new Item.Properties().group(MonetaMoney.MOD_TAB)));

    public static final RegistryObject<Vault> VAULT_BLOCK = BLOCKS.register("vault", Vault::new);
    public static final RegistryObject<Item> VAULT_ITEM = ITEMS.register("vault", () -> new BlockItem(VAULT_BLOCK.get(), new Item.Properties().group(MonetaMoney.MOD_TAB)));

    public static final RegistryObject<Anchor> ANCHOR_BLOCK = BLOCKS.register("anchor", Anchor::new);
    public static final RegistryObject<Item> ANCHOR_ITEM = ITEMS.register("anchor", () -> new BlockItem(ANCHOR_BLOCK.get(), new Item.Properties().group(MonetaMoney.MOD_TAB)));

    public static final RegistryObject<ContainerType<AccountTransactionContainer>> ACC_TRANS = CONTAINERS.register("acc_transaction", () -> IForgeContainerType.create((windowId, inv, data) -> new AccountTransactionContainer(windowId, inv)));
    public static final RegistryObject<ContainerType<AuctionHomeContainer>> AUCTION_HOME = CONTAINERS.register("auction_home", () -> IForgeContainerType.create((windowId, inv, data) -> new AuctionHomeContainer(windowId, inv, false, data.readInt())));
    public static final RegistryObject<ContainerType<AuctionBuyContainer>> AUCTION_BUY = CONTAINERS.register("auction_buy", () -> IForgeContainerType.create((windowId, inv, data) -> new AuctionBuyContainer(windowId, inv, data.readCompoundTag())));
    public static final RegistryObject<ContainerType<AuctionListItemContainer>> AUCTION_LIST = CONTAINERS.register("auction_list", () -> IForgeContainerType.create((windowId, inv, data) -> new AuctionListItemContainer(windowId, inv)));
}
