package github.pitbox46.monetamoney.containers.vault;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public abstract class AbstractBuyContainer extends PlayerInventoryContainer {
    public ItemStackHandler handler = new ItemStackHandler();

    public AbstractBuyContainer(MenuType<? extends AbstractBuyContainer> containerType, int id, Inventory playerInventory, CompoundTag itemNBT) {
        super(containerType, id, playerInventory, 31, 117);
        this.addSlot(new SlotItemHandler(handler, 0, 103, 21) {
            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }
        });
        handler.setStackInSlot(0, ItemStack.of(itemNBT));
    }

    public int getItemBuyPrice() {
        return handler.getStackInSlot(0).getTag().getInt("price");
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        if (ItemStack.matches(stack, handler.getStackInSlot(0))) return false;
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.getSlot(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 36) {
                if (!this.moveItemStackTo(stack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else if (!this.moveItemStackTo(stack, 36, 36, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return itemstack;
    }
}
