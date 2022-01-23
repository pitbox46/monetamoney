package github.pitbox46.monetamoney.items;

import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.UUID;

public class Coin extends Item {
    public static final int MAX_SIZE = 10000;

    public Coin(Properties properties) {
        super(properties.durability(MAX_SIZE).setNoRepair());
    }

    public static ItemStack createCoin(int amount, UUID uuid) {
        ItemStack coins = new ItemStack(Registration.COIN.get());
        coins.setDamageValue(coins.getMaxDamage() - amount);
        coins.getOrCreateTag().putUUID("uuid", Objects.requireNonNull(uuid));
        coins.setHoverName(new TranslatableComponent("item.monetamoney.coin", coins.getMaxDamage() - coins.getDamageValue() + " "));
        return coins;
    }
}
