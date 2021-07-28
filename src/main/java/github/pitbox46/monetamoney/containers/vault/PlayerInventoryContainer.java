package github.pitbox46.monetamoney.containers.vault;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class PlayerInventoryContainer extends Container {
    protected final PlayerEntity playerEntity;
    protected final IItemHandler playerInventory;

    public PlayerInventoryContainer(ContainerType<? extends PlayerInventoryContainer> containerType, int id, PlayerInventory playerInventory, int x, int y) {
        super(containerType, id);
        this.playerEntity = playerInventory.player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.layoutPlayerInventorySlots(x, y);
    }

    @Override
    public abstract ItemStack transferStackInSlot(PlayerEntity playerIn, int index);

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    protected int addSlotRow(IItemHandler handler, int index, int x, int y, int columns, int dx) {
        for (int i = 0 ; i < columns ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    protected int addSlots(IItemHandler handler, int index, int x, int y, int columns, int dx, int rows, int dy) {
        for (int j = 0 ; j < rows ; j++) {
            index = addSlotRow(handler, index, x, y, columns, dx);
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
