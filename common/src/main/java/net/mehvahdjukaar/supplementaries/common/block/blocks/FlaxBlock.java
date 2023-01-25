package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IBeeGrowable;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

//btw this should never grow a upper stage with age <4. Such block has a funny model and it you see it its some mod doing some fuckery or unsafe assumptions
public class FlaxBlock extends CropBlock implements IBeeGrowable {
    public static final int DOUBLE_AGE = 4; //age at which it grows in block above
    private static final VoxelShape FULL_BOTTOM = Block.box(1, 0, 1, 15, 16, 15);
    private static final VoxelShape[] SHAPES_BOTTOM = new VoxelShape[]{
            Block.box(4, 0, 4, 12, 6, 12),
            Block.box(3, 0, 3, 13, 10, 13),
            Block.box(3, 0, 3, 13, 13, 13),
            Block.box(3, 0, 3, 13, 16, 13),
            Block.box(2, 0, 2, 14, 16, 14),
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM};
    private static final VoxelShape[] SHAPES_TOP = new VoxelShape[]{
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(1, 0, 1, 15, 7, 15),
            Block.box(1, 0, 1, 15, 14, 15),
            Block.box(1, 0, 1, 15, 16, 15),};

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public FlaxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return SHAPES_BOTTOM[state.getValue(AGE)];
        }
        return SHAPES_TOP[state.getValue(AGE)];
    }

    //double plant code
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = stateIn.getValue(HALF);

        if (facing.getAxis() != Direction.Axis.Y || (half == DoubleBlockHalf.LOWER != (facing == Direction.UP) || this.isSingle(stateIn)) || (facingState.is(this) && facingState.getValue(HALF) != half)) {
            return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos)
                    ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    public boolean isSingle(BlockState state) {
        return this.getAge(state) < DOUBLE_AGE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            //no more mods messing with my upper stage smh
            BlockState above = worldIn.getBlockState(pos.above());
            if (above.getBlock() == this && this.isSingle(above)) return false;
            return super.canSurvive(state, worldIn, pos);
        } else {
            if (this.isSingle(state)) return false;
            BlockState blockstate = worldIn.getBlockState(pos.below());
            if (state.getBlock() != this)
                return super.canSurvive(state, worldIn, pos); //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
            return blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER && this.getAge(state) == this.getAge(blockstate);
        }
    }

    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (!worldIn.isClientSide) {
            if (player.isCreative()) {
                removeBottomHalf(worldIn, pos, state, player);
            } else {
                dropResources(state, worldIn, pos, null, player, player.getMainHandItem());
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, Blocks.AIR.defaultBlockState(), te, stack);
    }

    protected static void removeBottomHalf(Level world, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf doubleblockhalf = state.getValue(HALF);
        if (doubleblockhalf == DoubleBlockHalf.UPPER) {
            BlockPos blockpos = pos.below();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.getBlock() == state.getBlock() && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
                world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                world.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public void placeAt(LevelAccessor worldIn, BlockPos pos, int flags) {
        worldIn.setBlock(pos, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER), flags);
        worldIn.setBlock(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), flags);
    }


    // Tick function
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!PlatformHelper.isAreaLoaded(level, pos, 1))
            return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return; //only bottom one handles ticking
        if (level.getRawBrightness(pos, 0) >= 9) {
            if (this.isValidBonemealTarget(level, pos, state, level.isClientSide)) {
                float f = getGrowthSpeed(this, level, pos);
                if (ForgeHelper.onCropsGrowPre(level, pos, state, random.nextInt((int) (25.0F / f) + 1) == 0)) {
                    this.growCropBy(level, pos, state, 1);
                    ForgeHelper.onCropsGrowPost(level, pos, state);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        InteractionResult old = super.use(state, world, pos, player, hand, rayTraceResult);
        if (!old.consumesAction() && !this.isSingle(state) && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            var ev = ForgeHelper.onRightClickBlock(player, hand, pos.below(), rayTraceResult);
            if (ev != null) return ev;
            else {
                BlockState below = world.getBlockState(pos.below());
                return this.use(below, world, pos.below(), player, hand, rayTraceResult);
            }
        }
        return old;
    }

    public boolean canGrowUp(BlockGetter worldIn, BlockPos downPos) {
        BlockState state = worldIn.getBlockState(downPos.above());
        return state.getBlock() instanceof FlaxBlock || state.getMaterial().isReplaceable();
    }

    //for bonemeal
    @Override
    public boolean isValidBonemealTarget(BlockGetter worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return (!this.isMaxAge(state) && (this.canGrowUp(worldIn, pos) || this.getAge(state) < DOUBLE_AGE - 1));
    }

    //here I'm assuming canGrow has already been called
    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        growCropBy(level, pos, state, this.getBonemealAgeIncrease(level));
    }

    public void growCropBy(Level level, BlockPos pos, BlockState state, int increment) {
        if (increment > 1 && this.isSingle(state) && !canGrowUp(level, pos)) increment = 1;
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            //as if it was called on lower
            pos = pos.below();
        }
        int newAge = this.getAge(state) + increment;
        newAge = Math.min(newAge, this.getMaxAge());

        if (newAge >= DOUBLE_AGE) {
            level.setBlock(pos.above(), getStateForAge(newAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
        }
        level.setBlock(pos, getStateForAge(newAge), 2);
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return false;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModRegistry.FLAX_SEEDS_ITEM.get();
    }

    @Override
    public boolean getPollinated(Level level, BlockPos pos, BlockState state) {
        growCropBy(level, pos, state, 1);
        return true;
    }
}