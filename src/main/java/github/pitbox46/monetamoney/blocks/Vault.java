package github.pitbox46.monetamoney.blocks;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Outstanding;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SOpenMainPage;
import github.pitbox46.monetamoney.utils.BlockUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

import java.util.ArrayList;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class Vault extends Block implements IOnBreak {
    public static final VoxelShape NORTH_SHAPE = createRotatedShape(Direction.NORTH);
    public static final VoxelShape EAST_SHAPE = createRotatedShape(Direction.EAST);
    public static final VoxelShape SOUTH_SHAPE = createRotatedShape(Direction.SOUTH);
    public static final VoxelShape WEST_SHAPE = createRotatedShape(Direction.WEST);

    public static BlockPos lastOpenedVault;

    public Vault() {
        super(AbstractBlock.Properties
                .create(Material.IRON, MaterialColor.IRON)
                .setRequiresTool()
                .hardnessAndResistance(5.0F, 6.0F)
                .sound(SoundType.METAL)
                .notSolid()
        );
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!worldIn.isRemote()) {
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SOpenMainPage());
        } else {
            lastOpenedVault = pos;
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos) {
        if(!world.isRemote()) {
            Team team = Teams.getTeam(Teams.jsonFile, world.getDimensionKey().getLocation().toString() + pos.toLong());
            if(!team.isNull()) {
                while (team.balance > 0) {
                    int amount = team.balance <= Coin.MAX_SIZE ? (int) team.balance : Coin.MAX_SIZE;
                    ItemStack stack = Coin.createCoin(amount, Outstanding.newCoin(Outstanding.jsonFile, amount, "team_vault_broke"));
                    team.balance -= amount;
                    world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
                }
                ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
                for(ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    ServerEvents.CHUNK_MAP.get("unlisted").add(chunkLoader);
                }
                ServerEvents.CHUNK_MAP.remove(team.toString());
                Teams.removeTeam(Teams.jsonFile, team.toString());
            }
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new VaultTile();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            case NORTH:
                return NORTH_SHAPE;
            case EAST:
                return EAST_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
        }
        return NORTH_SHAPE;
    }

    @Override
    public boolean isTransparent(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockState = super.getStateForPlacement(context);
        assert blockState != null;
        return blockState.with(FACING, context.getPlacementHorizontalFacing());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static VoxelShape createRotatedShape(Direction facing) {
        return VoxelShapes.or(
                BlockUtils.cuboidWithRotation(facing, 7, 8, 14, 10, 11, 15),
                BlockUtils.cuboidWithRotation(facing, 6, 9, 14, 7, 10, 15),
                BlockUtils.cuboidWithRotation(facing, 8, 7, 14, 9, 8, 15),
                BlockUtils.cuboidWithRotation(facing, 10, 9, 14, 11, 10, 15),
                BlockUtils.cuboidWithRotation(facing, 8, 11, 14, 9, 12, 15),
                BlockUtils.cuboidWithRotation(facing, 5, 6, 14, 6, 7, 15),
                BlockUtils.cuboidWithRotation(facing, 8, 9, 15, 9, 10, 16),
                BlockUtils.cuboidWithRotation(facing, 13, 4, 14, 14, 14, 15),
                BlockUtils.cuboidWithRotation(facing, 2, 4, 14, 3, 14, 15),
                BlockUtils.cuboidWithRotation(facing, 2, 3, 14, 14, 4, 15),
                BlockUtils.cuboidWithRotation(facing, 3, 14, 14, 13, 15, 15),
                BlockUtils.cuboidWithRotation(facing, 2, 15, 13, 14, 16, 14),
                BlockUtils.cuboidWithRotation(facing, 1, 2, 13, 15, 15, 14),
                BlockUtils.cuboidWithRotation(facing, 3, 2, 3, 13, 3, 13),
                BlockUtils.cuboidWithRotation(facing, 3, 14, 3, 13, 15, 13),
                BlockUtils.cuboidWithRotation(facing, 2, 15, 2, 14, 16, 3),
                BlockUtils.cuboidWithRotation(facing, 3, 0, 3, 5, 2, 5),
                BlockUtils.cuboidWithRotation(facing, 3, 0, 11, 5, 2, 13),
                BlockUtils.cuboidWithRotation(facing, 11, 0, 11, 13, 2, 13),
                BlockUtils.cuboidWithRotation(facing, 11, 0, 3, 13, 2, 5),
                BlockUtils.cuboidWithRotation(facing, 13, 2, 3, 14, 14, 13),
                BlockUtils.cuboidWithRotation(facing, 2, 2, 3, 3, 14, 13),
                BlockUtils.cuboidWithRotation(facing, 1, 2, 2, 15, 15, 3)
        );
    }
}
