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
import java.util.Comparator;
import java.util.List;

public class AuctionHomeContainer extends PlayerInventoryContainer {
    public static final int SLOTS = 63;
    public static final int ROWS = 7;
    public static final List<ItemStackHandler> PAGES = new ArrayList<>();

    public final ItemStackHandler currentPage = new ItemStackHandler(SLOTS);

    public boolean editMode;
    public int pageNumber;

    public AuctionHomeContainer(int id, Inventory playerInventory, boolean editMode, int page) {
        super(Registration.AUCTION_HOME.get(), id, playerInventory, 31, 173);

        this.changePage(editMode, page);

        addLockedSlots(this.currentPage, 0, 31, 18, 9, 18, ROWS, 18);
    }

    public void changePage(boolean editMode, int pageNumber) {
        this.editMode = editMode;
        this.pageNumber = pageNumber;

        List<ItemStackHandler> pages = new ArrayList<>();
        if (Auctioned.auctionedNBT.get("shop") instanceof ListTag) {
            ListTag totalItems = new ListTag();
            ListTag auction = (ListTag) Auctioned.auctionedNBT.get("auction");

            auction.sort(Comparator.comparing(nbt -> ((CompoundTag) nbt).getString("id")));

            totalItems.addAll(auction);

            NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

            for (int i = 0, itemsAdded = 0; i < totalItems.size(); i++) {
                if (i % SLOTS == 0 && i != 0) {
                    pages.add(new ItemStackHandler(items));
                    items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
                }
                CompoundTag compoundNBT = (CompoundTag) totalItems.get(i);
                if (this.editMode) {
                    if (compoundNBT.getString("owner").equals(this.playerEntity.getGameProfile().getName())) {
                        ItemStack itemStack = ItemStack.of(compoundNBT);
                        itemStack.getOrCreateTag().putUUID("uuid", compoundNBT.getUUID("uuid"));
                        itemStack.getTag().putString("owner", this.playerEntity.getGameProfile().getName());
                        itemStack.getTag().putInt("price", compoundNBT.getInt("price"));
                        items.set(itemsAdded % SLOTS, itemStack);
                        itemsAdded++;
                    }
                } else {
                    ItemStack itemStack = ItemStack.of(compoundNBT);
                    itemStack.getOrCreateTag().putUUID("uuid", compoundNBT.getUUID("uuid"));
                    itemStack.getTag().putString("owner", compoundNBT.getString("owner"));
                    itemStack.getTag().putInt("price", compoundNBT.getInt("price"));
                    items.set(itemsAdded % SLOTS, itemStack);
                    itemsAdded++;
                }
            }
            if (!this.editMode) {
                PAGES.clear();
                PAGES.addAll(pages);
            }
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
