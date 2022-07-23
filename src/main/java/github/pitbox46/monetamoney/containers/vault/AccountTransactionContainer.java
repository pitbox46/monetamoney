package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AccountTransactionContainer extends PlayerInventoryContainer {
    public ItemStackHandler handler = new ItemStackHandler();

    public AccountTransactionContainer(int id, Inventory playerInventory) {
        super(Registration.ACC_TRANS.get(), id, playerInventory, 31, 117);
        this.addSlot(new SlotItemHandler(handler, 0, 103, 21));
    }

    @Override
    public void removed(Player playerIn) {
        if (!handler.getStackInSlot(0).isEmpty()) {
            playerIn.getInventory().placeItemBackInInventory(handler.getStackInSlot(0));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.getSlot(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            if(stack.getItem() == Registration.COIN.get()) {
                if(index == 36) {
                    if(!moveItemStackTo(stack, 0, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else {
                    if(!moveItemStackTo(stack, 36, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            slot.onTake(playerIn, stack);
        }

        return itemStack;
    }
}
