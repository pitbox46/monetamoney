package github.pitbox46.monetamoney.network;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class SDenyUseItem {
    public final Hand hand;
    public final ItemStack item;

    public SDenyUseItem(Hand hand, ItemStack item) {
        this.hand = hand;
        this.item = item;
    }
}
