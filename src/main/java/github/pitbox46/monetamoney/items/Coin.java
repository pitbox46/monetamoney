package github.pitbox46.monetamoney.items;

import github.pitbox46.monetamoney.data.Outstanding;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;
import java.util.UUID;

public class Coin extends Item {
    public static final int MAX_SIZE = 10000;

    public Coin(Properties properties) {
        super(properties.maxDamage(MAX_SIZE).setNoRepair());
    }

    public static ItemStack createCoin(int amount, UUID uuid) {
        ItemStack coins = new ItemStack(Registration.COIN.get());
        coins.setDamage(coins.getMaxDamage() - amount);
        coins.getOrCreateTag().putUniqueId("uuid", Objects.requireNonNull(uuid));
        coins.setDisplayName(new TranslationTextComponent("item.monetamoney.coin", coins.getMaxDamage() - coins.getDamage() + " "));
        return coins;
    }
}
