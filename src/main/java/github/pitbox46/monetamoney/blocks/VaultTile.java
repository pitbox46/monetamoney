package github.pitbox46.monetamoney.blocks;

import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class VaultTile extends TileEntity {
    public VaultTile() {
        super(Registration.VAULT_TILE.get());
    }
}
