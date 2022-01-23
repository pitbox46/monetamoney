package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AuctionBuyContainer extends AbstractBuyContainer {
    public AuctionBuyContainer(int id, Inventory playerInventory, CompoundTag itemNBT) {
        super(Registration.AUCTION_BUY.get(), id, playerInventory, itemNBT);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        if (ItemStack.matches(stack, handler.getStackInSlot(0))) return false;
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }
}
