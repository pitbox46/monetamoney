package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ShopBuyContainer extends AbstractBuyContainer {
    public int stock;

    public ShopBuyContainer(int id, Inventory playerInventory, CompoundTag itemNBT, int stock) {
        super(Registration.SHOP_BUY.get(), id, playerInventory, itemNBT);
        this.stock = stock;
    }

    public int getItemBuyPrice() {
        return handler.getStackInSlot(0).getTag().getInt("buyPrice");
    }

    public int getItemSellPrice() {
        return handler.getStackInSlot(0).getTag().getInt("sellPrice");
    }

    public void buyItem() {
        if (ClientProxy.personalBalance >= getItemBuyPrice() && stock > 0) {
            stock--;
        }
    }

    public void sellItem() {
        //Logic to make sure player has enough items. Only necessary to ensure that the displayed stock is somewhat accurate
        int quantity = 0;
        Inventory inv = this.playerEntity.getInventory();
        ItemStack item = handler.getStackInSlot(0);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).hasTag()) {
                if (inv.getItem(i).getItem().equals(item.getItem())) {
                    quantity += inv.getItem(i).getCount();
                }
            } else {
                if (ItemStack.matches(inv.getItem(i), item)) {
                    quantity += inv.getItem(i).getCount();
                }
            }
        }
        if (quantity >= item.getCount()) {
            stock++;
        }
    }
}
