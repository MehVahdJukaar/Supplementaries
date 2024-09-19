package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.block.IPistonMotionReact;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class SlidyBlock extends FallingBlock implements IPistonMotionReact {

    public static MapCodec<SlidyBlock> CODEC = simpleCodec(SlidyBlock::new);

    public static BooleanProperty ON_PRESSURE_PLATE = ModBlockProperties.ON_PRESSURE_PLATE;

    public SlidyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ON_PRESSURE_PLATE, false));
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public void onMagnetMoved(Level level, BlockPos blockPos, Direction direction, BlockState blockState, BlockEntity blockEntity) {
        this.onMoved(level, blockPos, blockState, direction, false);
    }

    @Override
    public void onMoved(Level level, BlockPos pos, BlockState movedState, Direction direction, boolean extending) {
        if (level.getBlockState(pos.below()).is(BlockTags.ICE)) {
            for (Direction dir : Direction.values()) {
                if (SuppPlatformStuff.canStickTo(movedState, level.getBlockState(pos.relative(dir)))) {
                    return;
                }
            }
            MovingSlidyBlock.maybeMove(movedState, level, pos, direction);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ON_PRESSURE_PLATE);
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0x888877;
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(1.0F, 10);
    }

    public static boolean canFall(BlockPos pos, LevelAccessor world) {
        return (world.isEmptyBlock(pos.below()) || isFree(world.getBlockState(pos.below()))) &&
                pos.getY() >= world.getMinBuildHeight() &&
                !IRopeConnection.isSupportingCeiling(pos.above(), world);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (canFall(pos, level)) {
            FallingBlockEntity entity = FallingBlockEntity.fall(level, pos, state);
            this.falling(entity);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState state1, FallingBlockEntity blockEntity) {
        super.onLand(level, pos, state, state1, blockEntity);
        //land sound
        if (!blockEntity.isSilent()) {
            level.playSound(null, pos, state.getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (MovingSlidyBlock.maybeMove(state, level, pos, hitResult.getDirection().getOpposite())) {
            level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }

}
