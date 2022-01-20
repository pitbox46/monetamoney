package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ShopBuyContainer extends AbstractBuyContainer {
    public int stock;

    public ShopBuyContainer(int id, PlayerInventory playerInventory, CompoundNBT itemNBT, int stock) {
        super(Registration.SHOP_BUY.get(), id, playerInventory, itemNBT);
        this.stock = stock;
    }
}
