package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.misc.ModSoundType;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.TomatoBlock;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FarmersDelightCompat {

    public static final ModSoundType STICK_TOMATO_SOUND = new ModSoundType(1.0F, 1.0F,
            () -> SoundEvents.CROP_BREAK,
            () -> SoundEvents.GRASS_STEP,
            () -> SoundEvents.WOOD_PLACE,
            () -> SoundEvents.GRASS_HIT,
            () -> SoundEvents.GRASS_FALL);

    public static final Supplier<Block> ROPE_TOMATO = RegHelper.registerBlock(Supplementaries.res("rope_tomatoes"),
            () -> new TomatoRopeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .forceSolidOff()));

    public static final Supplier<Block> STICK_TOMATOES = RegHelper.registerBlock(Supplementaries.res("stick_tomatoes"),
            () -> new TomatoStickBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .forceSolidOff()
                    .sound(STICK_TOMATO_SOUND)));

    public static void init() {
    }


    @Nullable
    public static BlockState getTomatoLoggedReplacement(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (isTomatoVineClimbingConfigOn()) {
            BlockState toPlace;
            if (state.is(ModRegistry.ROPE.get())) {
                toPlace = ROPE_TOMATO.get().defaultBlockState();
                toPlace = Block.updateFromNeighbourShapes(toPlace, level, pos);
                return toPlace;
            } else if (state.is(ModRegistry.STICK_BLOCK.get())) {
                toPlace = STICK_TOMATOES.get().defaultBlockState();
                return toPlace;
            }
        }
        return null;
    }

    public static boolean isTomatoVineClimbingConfigOn() {
        return Configuration.ENABLE_TOMATO_VINE_CLIMBING_TAGGED_ROPES.get();
    }

    public static Block getStickTomato() {
        return STICK_TOMATOES.get();
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
        return new PlanterRichBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_TERRACOTTA)
                .strength(2f, 6f)
                .requiresCorrectToolForDrops()
                .randomTicks(), CompatObjects.RICH_SOIL);
    }

    private static class TomatoStickBlock extends TomatoBlock {

        public static final BooleanProperty AXIS_X = ModBlockProperties.AXIS_X;
        public static final BooleanProperty AXIS_Z = ModBlockProperties.AXIS_Z;

        public TomatoStickBlock(BlockBehaviour.Properties properties) {
            super(properties);
            this.registerDefaultState(this.defaultBlockState().setValue(TomatoBlock.ROPELOGGED, true)
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


    private static class TomatoRopeBlock extends TomatoBlock implements IRopeConnection {

        public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
        public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
        public static final BooleanProperty WEST = BlockStateProperties.WEST;
        public static final BooleanProperty EAST = BlockStateProperties.EAST;
        public static final BooleanProperty KNOT = ModBlockProperties.KNOT;

        public TomatoRopeBlock(BlockBehaviour.Properties properties) {
            super(properties);
            this.registerDefaultState(this.defaultBlockState()
                    .setValue(VINE_AGE, 0).setValue(ROPELOGGED, false).setValue(KNOT, false)
                    .setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false));
        }

        // map horizontal directions to properties
        private static final Map<Direction, BooleanProperty> HMAP = Map.of(
                Direction.NORTH, NORTH,
                Direction.EAST, EAST,
                Direction.SOUTH, SOUTH,
                Direction.WEST, WEST
        );

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            // first add tomato properties
            super.createBlockStateDefinition(builder);
            // then rope connection properties
            builder.add(NORTH, SOUTH, EAST, WEST, KNOT);
        }

        @Override
        public boolean canSideAcceptConnection(BlockState state, Direction direction) {
            // allow connections from any side (IRopeConnection helpers will filter)
            return true;
        }

        public boolean hasConnection(Direction dir, BlockState state) {
            if (HMAP.containsKey(dir)) return state.getValue(HMAP.get(dir));
            // no vertical connection properties on this class
            return false;
        }

        public BlockState setConnection(Direction dir, BlockState state, boolean value) {
            if (HMAP.containsKey(dir)) return state.setValue(HMAP.get(dir), value);
            return state;
        }

        // determine whether this block should have a middle knot (copied logic adapted to horizontals)
        private boolean hasMiddleKnot(BlockState state) {
            boolean north = state.getValue(NORTH);
            boolean east = state.getValue(EAST);
            boolean south = state.getValue(SOUTH);
            boolean west = state.getValue(WEST);
            return !((north && south && !east && !west) || (!north && !south && east && west));
        }

        @Override
        public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                      LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
            // keep tomato behavior
            super.updateShape(state, facing, facingState, world, currentPos, facingPos);

            // update horizontal connections
            if (facing.getAxis().isHorizontal()) {
                boolean conn = this.shouldConnectToFace(state, facingState, facingPos, facing, world);
                state = setConnection(facing, state, conn);
            }

            // recompute knot
            state = state.setValue(KNOT, hasMiddleKnot(state));
            return state;
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = this.defaultBlockState();
            // set tomato defaults
            state = state.setValue(VINE_AGE, 0).setValue(ROPELOGGED, false).setValue(KNOT, false);

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos facingPos = pos.relative(dir);
                BlockState facingState = world.getBlockState(facingPos);
                state = setConnection(dir, state, this.shouldConnectToFace(state, facingState, facingPos, dir, world));
            }
            state = state.setValue(KNOT, hasMiddleKnot(state));
            return state;
        }
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
            if (CommonConfigs.Building.FD_PLANTER.get()) {
                richSoilDelegate.get().randomTick(worldIn, pos, rand);
            }
        }
    }

}
