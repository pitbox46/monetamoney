package github.pitbox46.monetamoney.blocks;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.SOpenAnchorPage;
import github.pitbox46.monetamoney.utils.BlockUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class Anchor extends Block implements IOnBreak {
    public static final VoxelShape NORTH_SHAPE = createRotatedShape(Direction.NORTH);
    public static final VoxelShape EAST_SHAPE = createRotatedShape(Direction.EAST);
    public static final VoxelShape SOUTH_SHAPE = createRotatedShape(Direction.SOUTH);
    public static final VoxelShape WEST_SHAPE = createRotatedShape(Direction.WEST);

    private static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);

    public static BlockPos lastOpenedAnchor;

    public Anchor() {
        super(AbstractBlock.Properties
                .create(Material.IRON, MaterialColor.IRON)
                .setRequiresTool()
                .hardnessAndResistance(5.0F, 6.0F)
                .sound(SoundType.METAL)
                .notSolid()
        );
        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH)
                .with(STATUS, Status.OFF)
        );
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!worldIn.isRemote()) {
            Team team = Teams.getPlayersTeam(Teams.jsonFile, player.getGameProfile().getName());
            String teamKey = team.isNull() ? "unlisted" : team.toString();
            ServerEvents.CHUNK_MAP.putIfAbsent(teamKey, new ArrayList<>());

            boolean flag = false;
            for (ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(teamKey)) {
                if (worldIn.getDimensionKey().getLocation().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(pos)) {
                    flag = true;
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SOpenAnchorPage(chunkLoader.status == ChunkLoader.Status.ON));
                    break;
                }
            }
            if(!flag) {
                ServerEvents.CHUNK_MAP.get(teamKey).add(new ChunkLoader(worldIn.getDimensionKey().getLocation(), pos, player.getGameProfile().getName(), ChunkLoader.Status.OFF));
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SOpenAnchorPage(false));
            }
        } else {
            lastOpenedAnchor = pos;
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos) {
        if(!world.isRemote()) {
            ServerEvents.CHUNK_MAP.values().forEach(list ->
                    list.removeIf(chunk -> world.getDimensionKey().getLocation().equals(chunk.dimensionKey) && chunk.pos.equals(pos))
            );
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
        return blockState.with(FACING, context.getPlacementHorizontalFacing().rotateY());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(STATUS);
    }

    private static VoxelShape createRotatedShape(Direction direction) {
        return VoxelShapes.or(
                BlockUtils.cuboidWithRotation(direction, 5, 1, 6, 6, 13, 10),
                BlockUtils.cuboidWithRotation(direction, 4, 0, 4, 12, 1, 12),
                BlockUtils.cuboidWithRotation(direction, 4, 13, 4, 12, 14, 12),
                BlockUtils.cuboidWithRotation(direction, 4, 1, 6, 5, 2, 10),
                BlockUtils.cuboidWithRotation(direction, 11, 1, 6, 12, 2, 10),
                BlockUtils.cuboidWithRotation(direction, 11, 12, 6, 12, 13, 10),
                BlockUtils.cuboidWithRotation(direction, 4, 12, 6, 5, 13, 10),
                BlockUtils.cuboidWithRotation(direction, 6, 1, 11, 10, 2, 12),
                BlockUtils.cuboidWithRotation(direction, 6, 12, 11, 10, 13, 12),
                BlockUtils.cuboidWithRotation(direction, 6, 12, 4, 10, 13, 5),
                BlockUtils.cuboidWithRotation(direction, 6, 1, 4, 10, 2, 5),
                BlockUtils.cuboidWithRotation(direction, 10, 1, 5, 11, 2, 6),
                BlockUtils.cuboidWithRotation(direction, 10, 1, 10, 11, 2, 11),
                BlockUtils.cuboidWithRotation(direction, 10, 12, 10, 11, 13, 11),
                BlockUtils.cuboidWithRotation(direction, 10, 12, 5, 11, 13, 6),
                BlockUtils.cuboidWithRotation(direction, 5, 12, 5, 6, 13, 6),
                BlockUtils.cuboidWithRotation(direction, 5, 12, 10, 6, 13, 11),
                BlockUtils.cuboidWithRotation(direction, 5, 1, 10, 6, 2, 11),
                BlockUtils.cuboidWithRotation(direction, 5, 1, 5, 6, 2, 6),
                BlockUtils.cuboidWithRotation(direction, 6, 1, 5, 10, 13, 11),
                BlockUtils.cuboidWithRotation(direction, 10, 1, 6, 11, 13, 10),
                BlockUtils.cuboidWithRotation(direction, 6, 14, 0, 10, 32, 16)
        ).simplify();
    }

    public enum Status implements IStringSerializable {
        OFF("off"),
        ON("on"),
        OUT("out");

        public final String name;
        Status(String name) {
            this.name = name;
        }

        @Override
        public String getString() {
            return name;
        }
    }
}
