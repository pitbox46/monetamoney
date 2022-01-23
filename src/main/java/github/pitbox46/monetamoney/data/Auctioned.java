package github.pitbox46.monetamoney.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

public class Auctioned {
    public static File auctionedFile;
    public static CompoundTag auctionedNBT;

    public static void init(Path modFolder) {
        auctionedFile = new File(FileUtils.getOrCreateDirectory(modFolder, "monetamoney").toFile(), "auctioned.nbt");
        try {
            if (auctionedFile.createNewFile()) {
                CompoundTag nbt = new CompoundTag();
                nbt.put("shop", new ListTag());
                nbt.put("auction", new ListTag());
                write(auctionedFile, nbt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        auctionedNBT = load(auctionedFile);
    }

    public static CompoundTag load(File file) {
        try {
            return NbtIo.read(file);
        } catch (IOException e) {
            throw new RuntimeException("Auction file failed to load", e);
        }
    }

    public static void write(File file, CompoundTag nbt) {
        try {
            NbtIo.write(nbt, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteListing(CompoundTag nbt, UUID uuid) {
        Iterator<Tag> iterator = ((ListTag) nbt.get("auction")).iterator();
        while (iterator.hasNext()) {
            if (((CompoundTag) iterator.next()).getUUID("uuid").equals(uuid)) {
                iterator.remove();
                return;
            }
        }
    }

    public static void addListing(CompoundTag nbt, ItemStack item, int amount, String owner) {
        CompoundTag itemNBT = new CompoundTag();
        itemNBT.putUUID("uuid", new UUID(System.nanoTime(), Double.doubleToLongBits(Math.random())));
        itemNBT.putString("owner", owner);
        itemNBT.putInt("price", amount);
        item.save(itemNBT);

        ((ListTag) nbt.get("auction")).add(itemNBT);
    }

    public static void addShopListing(CompoundTag nbt, ItemStack item, int buy, int sell, int dailyStock) {
        CompoundTag itemNBT = new CompoundTag();
        itemNBT.putUUID("uuid", new UUID(System.nanoTime(), Double.doubleToLongBits(Math.random())));
        itemNBT.putInt("buyPrice", buy);
        itemNBT.putInt("sellPrice", sell);
        item.save(itemNBT);
        itemNBT.putInt("dailyStock", dailyStock);
        itemNBT.putInt("stock", dailyStock);

        ((ListTag) nbt.get("shop")).add(itemNBT);
    }

    public static boolean confirmListing(CompoundTag nbt, CompoundTag itemNBT) {
        try {
            for (Tag element : (ListTag) Auctioned.auctionedNBT.get("auction")) {
                if (((CompoundTag) element).getUUID("uuid").equals(itemNBT.getUUID("uuid"))) {
                    return itemNBT.getInt("price") == ((CompoundTag) element).getInt("price");
                }
            }
            for (Tag element : (ListTag) Auctioned.auctionedNBT.get("shop")) {
                if (((CompoundTag) element).getUUID("uuid").equals(itemNBT.getUUID("uuid"))) {
                    return itemNBT.getInt("price") == ((CompoundTag) element).getInt("price");
                }
            }
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    public static boolean confirmOwner(CompoundTag nbt, CompoundTag itemNBT, String owner) {
        try {
            for (Tag element : (ListTag) Auctioned.auctionedNBT.get("auction")) {
                if (((CompoundTag) element).getUUID("uuid").equals(itemNBT.getUUID("uuid"))) {
                    return owner.equals(((CompoundTag) element).getString("owner"));
                }
            }
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    public static boolean buyFromShop(CompoundTag itemNBT) {
        try {
            for (Tag element : (ListTag) Auctioned.auctionedNBT.get("shop")) {
                CompoundTag item = (CompoundTag) element;
                if (item.getUUID("uuid").equals(itemNBT.getUUID("uuid")) && item.getInt("stock") > 0) {
                    item.putInt("stock", item.getInt("stock") - 1);
                    return true;
                }
            }
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    public static boolean sellToShop(CompoundTag itemNBT) {
        try {
            for (Tag element : (ListTag) Auctioned.auctionedNBT.get("shop")) {
                CompoundTag item = (CompoundTag) element;
                if (item.getUUID("uuid").equals(itemNBT.getUUID("uuid"))) {
                    item.putInt("stock", item.getInt("stock") + 1);
                    return true;
                }
            }
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    public static int getStock(CompoundTag itemNBT) {
        try {
            for (Tag element : (ListTag) Auctioned.auctionedNBT.get("shop")) {
                CompoundTag item = (CompoundTag) element;
                if (item.getUUID("uuid").equals(itemNBT.getUUID("uuid"))) {
                    return item.getInt("stock");
                }
            }
        } catch (NullPointerException ignored) {
        }
        return 0;
    }

    public static boolean restockShop() {
        try {
            for (Tag element : (ListTag) Auctioned.auctionedNBT.get("shop")) {
                CompoundTag item = (CompoundTag) element;
                item.putInt("stock", item.getInt("dailyStock"));
            }
        } catch (NullPointerException ignored) {
        }
        return false;
    }
}
