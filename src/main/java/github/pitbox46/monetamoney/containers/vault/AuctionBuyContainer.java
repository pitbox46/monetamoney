package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AuctionBuyContainer extends AbstractBuyContainer {
    public AuctionBuyContainer(int id, PlayerInventory playerInventory, CompoundNBT itemNBT) {
        super(Registration.AUCTION_BUY.get(), id, playerInventory, itemNBT);
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        if(ItemStack.areItemStacksEqual(stack, handler.getStackInSlot(0))) return false;
        return super.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
    }
}
