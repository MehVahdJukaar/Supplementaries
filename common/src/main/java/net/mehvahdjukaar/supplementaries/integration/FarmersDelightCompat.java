package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.misc.ModSoundType;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.List;
import java.util.function.Supplier;

public class FarmersDelightCompat {

    public static final ModSoundType STICK_TOMATO_SOUND = new ModSoundType(1.0F, 1.0F,
            () -> SoundEvents.CROP_BREAK,
            () -> SoundEvents.GRASS_STEP,
            () -> SoundEvents.WOOD_PLACE,
            () -> SoundEvents.GRASS_HIT,
            () -> SoundEvents.GRASS_FALL);

    public static final Supplier<Block> ROPE_TOMATO = RegHelper.registerBlock(Supplementaries.res("rope_tomatoes"),
            () -> new TomatoRopeBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)
                    .forceSolidOff()));

    public static final Supplier<Block> STICK_TOMATOES = RegHelper.registerBlock(Supplementaries.res("stick_tomatoes"),
            () -> new TomatoStickBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)
                    .forceSolidOff()
                    .sound(STICK_TOMATO_SOUND)));

    public static void init() {
    }


    public static boolean tryTomatoLogging(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (isTomatoVineClimbingConfigOn()) {
            BlockState toPlace;
            if (state.is(ModRegistry.ROPE.get())) {
                toPlace = ROPE_TOMATO.get().defaultBlockState();
                toPlace = Block.updateFromNeighbourShapes(toPlace, level, pos);
                level.setBlock(pos, toPlace, 3);
                return true;
            } else if (state.is(ModRegistry.STICK_BLOCK.get())) {
                toPlace = STICK_TOMATOES.get().defaultBlockState();
                level.setBlock(pos, toPlace, 3);
                return true;
            }
        }
        return false;
    }

    @ExpectPlatform
    public static boolean isTomatoVineClimbingConfigOn() {
        throw new AssertionError();
    }

    public static Block getStickTomato() {
        return STICK_TOMATOES.get();
    }


    private abstract static class TomatoLoggedBlock extends TomatoVineBlock {

        public TomatoLoggedBlock(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        public void attemptRopeClimb(ServerLevel level, BlockPos pos, RandomSource random) {
            if (random.nextFloat() < 0.3F) {
                BlockPos posAbove = pos.above();
                BlockState stateAbove = level.getBlockState(posAbove);
                boolean canClimb = stateAbove.is(ModTags.ROPES);
                if (canClimb) {
                    int vineHeight;
                    for (vineHeight = 1; level.getBlockState(pos.below(vineHeight)).getBlock() instanceof TomatoVineBlock; ++vineHeight) {
                    }

                    if (vineHeight < 3) {
                        BlockState toPlace;
                        if (stateAbove.is(ModRegistry.ROPE.get())) {
                            toPlace = ROPE_TOMATO.get().withPropertiesOf(stateAbove);
                        } else if (stateAbove.is(ModRegistry.STICK_BLOCK.get())) {
                            toPlace = STICK_TOMATOES.get().withPropertiesOf(stateAbove);
                        } else {
                            toPlace = CompatObjects.TOMATO_CROP.get().defaultBlockState().setValue(TomatoVineBlock.ROPELOGGED, true);
                        }
                        level.setBlockAndUpdate(posAbove, toPlace);
                    }
                }
            }
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            return (belowState.getBlock() instanceof TomatoVineBlock || super.canSurvive(state.setValue(TomatoVineBlock.ROPELOGGED, false), level, pos)) && this.hasGoodCropConditions(level, pos);
        }

        @Override
        public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
            super.playerDestroy(level, player, pos, state.setValue(TomatoVineBlock.ROPELOGGED, false), blockEntity, stack);
        }

        @ForgeOverride
        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
            this.playerWillDestroy(level, pos, state, player);
            return level.setBlock(pos, getInnerBlock().withPropertiesOf(state), level.isClientSide ? 11 : 3);
        }

        @Override
        public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            if (!state.canSurvive(level, pos)) {
                //we can't just break block or other ropes will react when instead we want to replace with another rope
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                Block.dropResources(state, level, pos, null, null, ItemStack.EMPTY);

                level.setBlockAndUpdate(pos, getInnerBlock().withPropertiesOf(state));
            }
        }

        @Override
        public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos,
                                      BlockPos facingPos) {
            if (!state.canSurvive(level, currentPos)) {
                level.scheduleTick(currentPos, this, 1);
            }
            return state;
        }

        @Override
        public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
            state = CompatObjects.TOMATO_CROP.get().withPropertiesOf(state);
            return state.getDrops(builder);
        }

        public abstract Block getInnerBlock();
    }

    private static class TomatoRopeBlock extends TomatoLoggedBlock implements IRopeConnection {

        public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
        public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
        public static final BooleanProperty WEST = BlockStateProperties.WEST;
        public static final BooleanProperty EAST = BlockStateProperties.EAST;
        public static final BooleanProperty KNOT = ModBlockProperties.KNOT;

        public TomatoRopeBlock(BlockBehaviour.Properties properties) {
            super(properties);
            this.registerDefaultState(this.defaultBlockState().setValue(TomatoVineBlock.ROPELOGGED, true).setValue(KNOT, false)
                    .setValue(EAST, false).setValue(WEST, false).setValue(NORTH, false).setValue(SOUTH, false));
        }

        public Block getInnerBlock() {
            return ModRegistry.ROPE.get();
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(NORTH, SOUTH, EAST, WEST, KNOT);
            super.createBlockStateDefinition(builder);
        }

        @Override
        public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos,
                                      BlockPos facingPos) {
            super.updateShape(state, facing, facingState, level, currentPos, facingPos);

            if (facing.getAxis() == Direction.Axis.Y) {
                return state;
            }
            BlockState newState = state.setValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(facing), this.shouldConnectToFace(state, facingState, facingPos, facing, level));
            boolean hasKnot = newState.getValue(SOUTH) || newState.getValue(EAST) || newState.getValue(NORTH) || newState.getValue(WEST);
            newState = newState.setValue(KNOT, hasKnot);

            return newState;
        }

        @Override
        public boolean canSideAcceptConnection(BlockState state, Direction direction) {
            return true;
        }
    }

    private static class TomatoStickBlock extends TomatoLoggedBlock {

        public static final BooleanProperty AXIS_X = ModBlockProperties.AXIS_X;
        public static final BooleanProperty AXIS_Z = ModBlockProperties.AXIS_Z;

        public TomatoStickBlock(BlockBehaviour.Properties properties) {
            super(properties);
            this.registerDefaultState(this.defaultBlockState().setValue(TomatoVineBlock.ROPELOGGED, true)
                    .setValue(AXIS_X, false).setValue(AXIS_Z, false));
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return StickBlock.getStickShape(state.getValue(AXIS_X), true, state.getValue(AXIS_Z));
        }

        public Block getInnerBlock() {
            return ModRegistry.STICK_BLOCK.get();
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(AXIS_X, AXIS_Z);
            super.createBlockStateDefinition(builder);
        }

        @Override
        public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
            if (!context.isSecondaryUseActive() && context.getItemInHand().is(Items.STICK)) {
                return switch (context.getClickedFace().getAxis()) {
                    case Z -> !state.getValue(AXIS_Z);
                    case X -> !state.getValue(AXIS_X);
                    default -> false;
                };
            }
            return super.canBeReplaced(state, context);
        }
    }


    public static void setupClient() {
        ClientHelper.registerRenderType(ROPE_TOMATO.get(), RenderType.cutout());
        ClientHelper.registerRenderType(STICK_TOMATOES.get(), RenderType.cutout());
    }

    public static boolean canAddStickToTomato(BlockState blockstate, BooleanProperty axis) {
        if (blockstate.getBlock() == getStickTomato()) {
            return !blockstate.getValue(axis);
        }
        return false;
    }

    public static PlanterBlock makePlanterRich() {
        return new PlanterRichBlock(BlockBehaviour.Properties.copy(Blocks.RED_TERRACOTTA)
                .strength(2f, 6f)
                .requiresCorrectToolForDrops()
                .randomTicks(), CompatObjects.RICH_SOIL);
    }

    public static class PlanterRichBlock extends PlanterBlock {

        private final Supplier<BlockState> richSoilDelegate;

        public PlanterRichBlock(Properties properties, Supplier<Block> mimic) {
            super(properties);
            richSoilDelegate = Suppliers.memoize(() -> mimic.get().defaultBlockState());

            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                    .setValue(EXTENDED, false));
        }

        @Override
        public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
            return super.getDrops(blockState, builder);
        }

        @Override
        public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
            //hax
            if(CommonConfigs.Building.FD_PLANTER.get()) {
                richSoilDelegate.get().randomTick(worldIn, pos, rand);
            }
        }
    }

}
