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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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

public class Vault extends Block implements IOnBreak {
    public static final VoxelShape NORTH_SHAPE = createRotatedShape(Direction.NORTH);
    public static final VoxelShape EAST_SHAPE = createRotatedShape(Direction.EAST);
    public static final VoxelShape SOUTH_SHAPE = createRotatedShape(Direction.SOUTH);
    public static final VoxelShape WEST_SHAPE = createRotatedShape(Direction.WEST);

    public static BlockPos lastOpenedVault;

    public Vault() {
        super(BlockBehaviour.Properties
                .of(Material.METAL, MaterialColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
        );
    }

    private static VoxelShape createRotatedShape(Direction facing) {
        return Shapes.or(
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

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!worldIn.isClientSide()) {
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SOpenMainPage());
        } else {
            lastOpenedVault = pos;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onBlockBreak(Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            Team team = Teams.getTeam(Teams.jsonFile, world.dimension().location().toString() + pos.asLong());
            if (!team.isNull()) {
                while (team.balance > 0) {
                    int amount = team.balance <= Coin.MAX_SIZE ? (int) team.balance : Coin.MAX_SIZE;
                    ItemStack stack = Coin.createCoin(amount, Outstanding.newCoin(Outstanding.jsonFile, amount, "team_vault_broke"));
                    team.balance -= amount;
                    world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
                }
                ServerEvents.CHUNK_MAP.putIfAbsent("unlisted", new ArrayList<>());
                for (ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    ServerEvents.CHUNK_MAP.get("unlisted").add(chunkLoader);
                }
                ServerEvents.CHUNK_MAP.remove(team.toString());
                Teams.removeTeam(Teams.jsonFile, team.toString());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = super.getStateForPlacement(context);
        assert blockState != null;
        return blockState.setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
