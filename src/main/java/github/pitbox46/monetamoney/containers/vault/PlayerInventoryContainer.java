package github.pitbox46.monetamoney.containers.vault;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class PlayerInventoryContainer extends AbstractContainerMenu {
    protected final Player playerEntity;
    protected final IItemHandler playerInventory;

    public PlayerInventoryContainer(MenuType<? extends PlayerInventoryContainer> containerType, int id, Inventory playerInventory, int x, int y) {
        super(containerType, id);
        this.playerEntity = playerInventory.player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.layoutPlayerInventorySlots(x, y);
    }

    @Override
    public abstract ItemStack quickMoveStack(Player playerIn, int index);

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    protected int addSlotRow(IItemHandler handler, int index, int x, int y, int columns, int dx) {
        for (int i = 0; i < columns; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    protected int addLockedSlotRow(IItemHandler handler, int index, int x, int y, int columns, int dx) {
        for (int i = 0; i < columns; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y) {
                @Override
                public boolean mayPickup(Player playerIn) {
                    return false;
                }
            });
            x += dx;
            index++;
        }
        return index;
    }

    protected int addSlots(IItemHandler handler, int index, int x, int y, int columns, int dx, int rows, int dy) {
        for (int j = 0; j < rows; j++) {
            index = addSlotRow(handler, index, x, y, columns, dx);
            y += dy;
        }
        return index;
    }

    protected int addLockedSlots(IItemHandler handler, int index, int x, int y, int columns, int dx, int rows, int dy) {
        for (int j = 0; j < rows; j++) {
            index = addLockedSlotRow(handler, index, x, y, columns, dx);
            y += dy;
        }
        return index;
    }

    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlots(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRow(playerInventory, 0, leftCol, topRow, 9, 18);
    }
}
