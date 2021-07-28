package github.pitbox46.monetamoney.data;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fml.loading.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

public class Auctioned {
    public static File auctionedFile;
    public static CompoundNBT auctionedNBT;

    public static void init(Path modFolder) {
        auctionedFile = new File(FileUtils.getOrCreateDirectory(modFolder, "monetamoney").toFile(), "auctioned.nbt");
        try {
            if (auctionedFile.createNewFile()) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.put("shop", new ListNBT());
                nbt.put("auction", new ListNBT());
                write(auctionedFile, nbt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        auctionedNBT = load(auctionedFile);
    }

    public static CompoundNBT load(File file) {
        try {
            return CompressedStreamTools.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CompoundNBT();
    }

    public static void write(File file, CompoundNBT nbt) {
        try {
            CompressedStreamTools.write(nbt, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteListing(CompoundNBT nbt, UUID uuid) {
        Iterator<INBT> iterator = ((ListNBT) nbt.get("auction")).iterator();
        while(iterator.hasNext()) {
            if(((CompoundNBT) iterator.next()).getUniqueId("uuid").equals(uuid)) {
                iterator.remove();
                return;
            }
        }
    }

    public static void addListing(CompoundNBT nbt, ItemStack item, int amount, String owner) {
        CompoundNBT itemNBT = new CompoundNBT();
        itemNBT.putUniqueId("uuid", new UUID(System.nanoTime(), Double.doubleToLongBits(Math.random())));
        itemNBT.putString("owner", owner);
        itemNBT.putInt("price", amount);
        item.write(itemNBT);

        ((ListNBT) nbt.get("auction")).add(itemNBT);
    }

    public static void addShopListing(CompoundNBT nbt, ItemStack item, int amount) {
        CompoundNBT itemNBT = new CompoundNBT();
        itemNBT.putUniqueId("uuid", new UUID(System.nanoTime(), Double.doubleToLongBits(Math.random())));
        itemNBT.putString("owner", "shop listing");
        itemNBT.putInt("price", amount);
        item.write(itemNBT);

        ((ListNBT) nbt.get("shop")).add(itemNBT);
    }

    public static boolean confirmListing(CompoundNBT nbt, CompoundNBT itemNBT) {
        try {
            for (INBT element : (ListNBT) Auctioned.auctionedNBT.get("auction")) {
                if (((CompoundNBT) element).getUniqueId("uuid").equals(itemNBT.getUniqueId("uuid"))) {
                    return itemNBT.getInt("price") == ((CompoundNBT) element).getInt("price");
                }
            }
            for (INBT element : (ListNBT) Auctioned.auctionedNBT.get("shop")) {
                if (((CompoundNBT) element).getUniqueId("uuid").equals(itemNBT.getUniqueId("uuid"))) {
                    return itemNBT.getInt("price") == ((CompoundNBT) element).getInt("price");
                }
            }
        } catch (NullPointerException ignored) {}
        return false;
    }

    public static boolean confirmOwner(CompoundNBT nbt, CompoundNBT itemNBT, String owner) {
        try {
            for (INBT element : (ListNBT) Auctioned.auctionedNBT.get("auction")) {
                if (((CompoundNBT) element).getUniqueId("uuid").equals(itemNBT.getUniqueId("uuid"))) {
                    return owner.equals(((CompoundNBT) element).getString("owner"));
                }
            }
        } catch (NullPointerException ignored) {}
        return false;
    }
}
