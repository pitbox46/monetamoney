package github.pitbox46.monetamoney.blocks;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class AnchorTile extends TileEntity {
    public AnchorTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }
}
