package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ShopBuyContainer extends PlayerInventoryContainer {
    public ItemStackHandler handler = new ItemStackHandler();
    public int stock;

    public ShopBuyContainer(int id, PlayerInventory playerInventory, CompoundNBT itemNBT, int stock) {
        super(Registration.SHOP_BUY.get(), id, playerInventory, 31, 117);
        this.addSlot(new SlotItemHandler(handler, 0, 103, 21) {
            @Override
            public boolean canTakeStack(PlayerEntity playerIn) {
                return false;
            }
        });
        handler.setStackInSlot(0, ItemStack.read(itemNBT));
        this.stock = stock;
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        if(ItemStack.areItemStacksEqual(stack, handler.getStackInSlot(0))) return false;
        return super.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.getSlot(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();
            if (index == 36) {
                if (!this.mergeItemStack(stack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, itemstack);
            } else if (!this.mergeItemStack(stack, 36, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return itemstack;
    }
}
