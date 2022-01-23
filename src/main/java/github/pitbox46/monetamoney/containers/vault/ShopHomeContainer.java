package github.pitbox46.monetamoney.containers.vault;

import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class ShopHomeContainer extends PlayerInventoryContainer {
    public static final int SLOTS = 63;
    public static final int ROWS = 7;
    public static final List<ItemStackHandler> PAGES = new ArrayList<>();

    public final ItemStackHandler currentPage = new ItemStackHandler(SLOTS);

    public int pageNumber;

    public ShopHomeContainer(int id, Inventory playerInventory, int page) {
        super(Registration.SHOP_HOME.get(), id, playerInventory, 31, 173);

        this.changePage(page);

        addLockedSlots(this.currentPage, 0, 31, 18, 9, 18, ROWS, 18);
    }

    public void changePage(int pageNumber) {
        this.pageNumber = pageNumber;

        List<ItemStackHandler> pages = new ArrayList<>();
        if (Auctioned.auctionedNBT.get("shop") instanceof ListTag) {
            ListTag totalItems = new ListTag();
            ListTag shop = (ListTag) Auctioned.auctionedNBT.get("shop");

            totalItems.addAll(shop);

            NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

            for (int i = 0, itemsAdded = 0; i < totalItems.size(); i++) {
                if (i % SLOTS == 0 && i != 0) {
                    pages.add(new ItemStackHandler(items));
                    items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
                }
                CompoundTag compoundNBT = (CompoundTag) totalItems.get(i);
                ItemStack itemStack = ItemStack.of(compoundNBT);
                itemStack.getOrCreateTag().putUUID("uuid", compoundNBT.getUUID("uuid"));
                itemStack.getTag().putInt("buyPrice", compoundNBT.getInt("buyPrice"));
                itemStack.getTag().putInt("sellPrice", compoundNBT.getInt("sellPrice"));
                items.set(itemsAdded % SLOTS, itemStack);
                itemsAdded++;
            }
            PAGES.clear();
            PAGES.addAll(pages);
            pages.add(new ItemStackHandler(items));
        }
        if (pages.size() <= this.pageNumber) {
            this.pageNumber = pages.size() - 1;
        }
        for (int i = 0; i < SLOTS; i++) {
            this.currentPage.setStackInSlot(i, pages.get(this.pageNumber).getStackInSlot(i));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.getSlot(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index > 35) {
                return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 35, 0, true)) {
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
