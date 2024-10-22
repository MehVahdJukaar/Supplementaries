package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingSlidyBlockEntity;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSetSlidingBlockEntityPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public class MovingSlidyBlock extends MovingPistonBlock {

    public MovingSlidyBlock(Properties properties) {
        super(properties);
    }

    public static MovingSlidyBlockEntity newMovingBlockEntity(BlockPos pos, BlockState blockState, BlockState movedState, Direction direction) {
        return new MovingSlidyBlockEntity(pos, blockState, movedState, direction, true, false);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModRegistry.MOVING_SLIDY_BLOCK_TILE.get(), MovingSlidyBlockEntity::tick);
    }

    public static boolean maybeMove(BlockState state, Level level, BlockPos pos, Direction direction) {
        // can run on both sides so it updates faster but really we could make this server only
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighbor = level.getBlockState(neighborPos);
        if (!neighbor.isAir() && neighbor.getPistonPushReaction() != PushReaction.DESTROY) {
            return false;
        }

        level.destroyBlock(neighborPos, true);

        // called on both sides because it becomes smoother
        MovingSlidyBlock.move(state, level, pos, direction, neighborPos);
        if (!level.isClientSide) {
            level.playSound(null, pos, ModSounds.SLIDY_BLOCK_SLIDE.get(), SoundSource.BLOCKS,
                    1.0F, 1.1F + level.random.nextFloat() * 0.15F);
        }
        return true;
    }

    private static void move(BlockState state, Level level, BlockPos pos, Direction direction, BlockPos neighborPos) {
        BlockState newState = ModRegistry.MOVING_SLIDY_BLOCK.get().defaultBlockState()
                .setValue(MovingSlidyBlock.FACING, direction);

        level.setBlock(neighborPos, newState, UPDATE_ALL);
        var be = MovingSlidyBlock.newMovingBlockEntity(neighborPos, newState, state, direction);
        level.setBlockEntity(be);

        if (!level.isClientSide) {
            ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(level, neighborPos,
                    new ClientBoundSetSlidingBlockEntityPacket(be));
        }

        //pistons usually call this from both sides. here sometimes we dont... we must use a custom packet since tile is set manually
        //calling remove on same pst to hopefully fixe some tile entity issues
        level.removeBlock(pos, true);
        level.setBlock(pos, ModRegistry.MOVING_SLIDY_BLOCK_SOURCE.get()
                .defaultBlockState().setValue(BlockStateProperties.FACING, direction), 3);

    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
