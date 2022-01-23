package github.pitbox46.monetamoney.blocks;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SOpenAnchorPage;
import github.pitbox46.monetamoney.utils.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class Anchor extends Block implements IOnBreak {
    public static final VoxelShape NORTH_SHAPE = createRotatedShape(Direction.NORTH);
    public static final VoxelShape EAST_SHAPE = createRotatedShape(Direction.EAST);
    public static final VoxelShape SOUTH_SHAPE = createRotatedShape(Direction.SOUTH);
    public static final VoxelShape WEST_SHAPE = createRotatedShape(Direction.WEST);

    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);

    public static BlockPos lastOpenedAnchor;

    public Anchor() {
        super(BlockBehaviour.Properties
                .of(Material.METAL, MaterialColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
        );
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATUS, Status.OFF)
        );
    }

    private static VoxelShape createRotatedShape(Direction direction) {
        return Shapes.or(
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
        ).optimize();
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!worldIn.isClientSide()) {
            Team team = Teams.getPlayersTeam(Teams.jsonFile, player.getGameProfile().getName());
            String teamKey = team.isNull() ? "unlisted" : team.toString();
            ServerEvents.CHUNK_MAP.putIfAbsent(teamKey, new ArrayList<>());

            boolean flag = false;
            for (ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(teamKey)) {
                if (worldIn.dimension().location().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(pos)) {
                    flag = true;
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SOpenAnchorPage(chunkLoader.status == ChunkLoader.Status.ON));
                    break;
                }
            }
            if (!flag) {
                ServerEvents.CHUNK_MAP.get(teamKey).add(new ChunkLoader(worldIn.dimension().location(), pos, player.getGameProfile().getName(), ChunkLoader.Status.OFF));
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SOpenAnchorPage(false));
            }
        } else {
            lastOpenedAnchor = pos;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onBlockBreak(Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            ServerEvents.CHUNK_MAP.values().forEach(list ->
                    list.removeIf(chunk -> world.dimension().location().equals(chunk.dimensionKey) && chunk.pos.equals(pos))
            );
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
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
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = super.getStateForPlacement(context);
        assert blockState != null;
        return blockState.setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(STATUS);
    }

    public enum Status implements StringRepresentable {
        OFF("off"),
        ON("on"),
        STUCK("stuck");

        public final String name;

        Status(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
