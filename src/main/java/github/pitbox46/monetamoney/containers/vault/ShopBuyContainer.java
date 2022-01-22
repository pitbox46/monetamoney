package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.List;

public class ShopBuyContainer extends AbstractBuyContainer {
    public int stock;

    public ShopBuyContainer(int id, PlayerInventory playerInventory, CompoundNBT itemNBT, int stock) {
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
        if(ClientProxy.personalBalance >= getItemBuyPrice() && stock > 0) {
            stock--;
        }
    }

    public void sellItem() {
        //Logic to make sure player has enough items. Only necessary to ensure that the displayed stock is somewhat accurate
        int quantity = 0;
        PlayerInventory inv = this.playerEntity.inventory;
        ItemStack item = handler.getStackInSlot(0);
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            if(!inv.getStackInSlot(i).hasTag()) {
                if (inv.getStackInSlot(i).getItem().equals(item.getItem())) {
                    quantity += inv.getStackInSlot(i).getCount();
                }
            }
            else {
                if (ItemStack.areItemStacksEqual(inv.getStackInSlot(i), item)) {
                    quantity += inv.getStackInSlot(i).getCount();
                }
            }
        }
        if(quantity >= item.getCount()) {
            stock++;
        }
    }
}
