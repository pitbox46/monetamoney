package github.pitbox46.monetamoney.setup;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.*;
import github.pitbox46.monetamoney.items.Coin;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MonetaMoney.MODID);
    public static final RegistryObject<Coin> COIN = ITEMS.register("coin", () -> new Coin(new Item.Properties().tab(MonetaMoney.MOD_TAB)));
    static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MonetaMoney.MODID);
    public static final RegistryObject<Vault> VAULT_BLOCK = BLOCKS.register("vault", Vault::new);
    public static final RegistryObject<Item> VAULT_ITEM = ITEMS.register("vault", () -> new BlockItem(VAULT_BLOCK.get(), new Item.Properties().tab(MonetaMoney.MOD_TAB)));
    public static final RegistryObject<Anchor> ANCHOR_BLOCK = BLOCKS.register("anchor", Anchor::new);
    public static final RegistryObject<Item> ANCHOR_ITEM = ITEMS.register("anchor", () -> new BlockItem(ANCHOR_BLOCK.get(), new Item.Properties().tab(MonetaMoney.MOD_TAB)));
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MonetaMoney.MODID);
    static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MonetaMoney.MODID);
    public static final RegistryObject<MenuType<AccountTransactionContainer>> ACC_TRANS = CONTAINERS.register("acc_transaction", () -> IForgeContainerType.create((windowId, inv, data) -> new AccountTransactionContainer(windowId, inv)));
    public static final RegistryObject<MenuType<AuctionHomeContainer>> AUCTION_HOME = CONTAINERS.register("auction_home", () -> IForgeContainerType.create((windowId, inv, data) -> new AuctionHomeContainer(windowId, inv, false, data.readInt())));
    public static final RegistryObject<MenuType<AuctionBuyContainer>> AUCTION_BUY = CONTAINERS.register("auction_buy", () -> IForgeContainerType.create((windowId, inv, data) -> new AuctionBuyContainer(windowId, inv, data.readNbt())));
    public static final RegistryObject<MenuType<AuctionListItemContainer>> AUCTION_LIST = CONTAINERS.register("auction_list", () -> IForgeContainerType.create((windowId, inv, data) -> new AuctionListItemContainer(windowId, inv)));
    public static final RegistryObject<MenuType<ShopHomeContainer>> SHOP_HOME = CONTAINERS.register("shop_home", () -> IForgeContainerType.create((windowId, inv, data) -> new ShopHomeContainer(windowId, inv, data.readInt())));
    public static final RegistryObject<MenuType<ShopBuyContainer>> SHOP_BUY = CONTAINERS.register("shop_buy", () -> IForgeContainerType.create((windowId, inv, data) -> new ShopBuyContainer(windowId, inv, data.readNbt(), data.readInt())));

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CONTAINERS.register(modEventBus);
    }
}
