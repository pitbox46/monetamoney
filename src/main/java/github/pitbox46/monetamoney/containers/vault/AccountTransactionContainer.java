package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AccountTransactionContainer extends PlayerInventoryContainer {
    public ItemStackHandler handler = new ItemStackHandler();

    public AccountTransactionContainer(int id, PlayerInventory playerInventory) {
        super(Registration.ACC_TRANS.get(), id, playerInventory, 31, 117);
        this.addSlot(new SlotItemHandler(handler, 0, 103, 21));
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        if(!handler.getStackInSlot(0).isEmpty()) {
            playerIn.inventory.placeItemBackInInventory(playerIn.getEntityWorld(), handler.getStackInSlot(0));
        }
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
            }
            else if (this.mergeItemStack(stack, 36, 37, false)) {
                return ItemStack.EMPTY;
            }
            else if (index < 27) {
                if (!this.mergeItemStack(itemstack, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 36) {
                if (!this.mergeItemStack(itemstack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
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
