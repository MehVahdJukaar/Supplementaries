package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DirectionalCakeBlock extends CakeBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape[] SHAPES_NORTH = new VoxelShape[]{
            Block.box(1, 0, 1, 15, 8, 15),
            Block.box(1, 0, 3, 15, 8, 15),
            Block.box(1, 0, 5, 15, 8, 15),
            Block.box(1, 0, 7, 15, 8, 15),
            Block.box(1, 0, 9, 15, 8, 15),
            Block.box(1, 0, 11, 15, 8, 15),
            Block.box(1, 0, 13, 15, 8, 15)};
    protected static final VoxelShape[] SHAPES_SOUTH = new VoxelShape[]{
            Block.box(1, 0, 1, 15, 8, 15),
            Block.box(1, 0, 1, 15, 8, 13),
            Block.box(1, 0, 1, 15, 8, 11),
            Block.box(1, 0, 1, 15, 8, 9),
            Block.box(1, 0, 1, 15, 8, 7),
            Block.box(1, 0, 1, 15, 8, 5),
            Block.box(1, 0, 1, 15, 8, 3)};
    protected static final VoxelShape[] SHAPES_EAST = new VoxelShape[]{
            Block.box(1, 0, 1, 15, 8, 15),
            Block.box(1, 0, 1, 13, 8, 15),
            Block.box(1, 0, 1, 11, 8, 15),
            Block.box(1, 0, 1, 9, 8, 15),
            Block.box(1, 0, 1, 7, 8, 15),
            Block.box(1, 0, 1, 5, 8, 15),
            Block.box(1, 0, 1, 3, 8, 15)};


    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public DirectionalCakeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0)
                .setValue(FACING, Direction.WEST).setValue(WATERLOGGED, false));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(handIn);
        Item item = itemstack.getItem();

        if (CompatHandler.FARMERS_DELIGHT) {
            InteractionResult res = FarmersDelightCompat.onCakeInteract(state, pos, level, itemstack);
            if (res.consumesAction()) return res;
        }

        if (itemstack.is(ItemTags.CANDLES) && state.getValue(BITES) == 0 && state.is(ModRegistry.DIRECTIONAL_CAKE.get())) {
            Block block = Block.byItem(item);
            if (block instanceof CandleBlock) {
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }

                level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(pos, CandleCakeBlock.byCandle(block));
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.awardStat(Stats.ITEM_USED.get(item));
                return InteractionResult.SUCCESS;
            }
        }
        return this.eatSliceD(level, pos, state, player,
                hit.getDirection().getAxis() != Direction.Axis.Y ? hit.getDirection() : player.getDirection().getOpposite());

    }

    public InteractionResult eatSliceD(LevelAccessor world, BlockPos pos, BlockState state, Player player, Direction dir) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        } else {
            player.awardStat(Stats.EAT_CAKE_SLICE);
            player.getFoodData().eat(2, 0.1F);
            if (!world.isClientSide()) {
                this.removeSlice(state, pos, world, dir);
            }
            return InteractionResult.sidedSuccess(world.isClientSide());
        }
    }

    public void removeSlice(BlockState state, BlockPos pos, LevelAccessor world, Direction dir) {
        int i = state.getValue(BITES);
        if (i < 6) {
            if (i == 0 && CommonConfigs.Tweaks.DIRECTIONAL_CAKE.get()) state = state.setValue(FACING, dir);
            world.setBlock(pos, state.setValue(BITES, i + 1), 3);
        } else {
            world.removeBlock(pos, false);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Items.CAKE);
    }

    @Override
    public MutableComponent getName() {
        return Blocks.CAKE.getName();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BITES, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> SHAPE_BY_BITE[state.getValue(BITES)];
            case EAST -> SHAPES_EAST[state.getValue(BITES)];
            case SOUTH -> SHAPES_SOUTH[state.getValue(BITES)];
            case NORTH -> SHAPES_NORTH[state.getValue(BITES)];
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (CommonUtil.FESTIVITY.isStValentine()) {
            if (rand.nextFloat() > 0.8) {
                double d0 = (pos.getX() + 0.5 + (rand.nextFloat() - 0.5));
                double d1 = (pos.getY() + 0.25 + (rand.nextFloat() - 0.25));
                double d2 = (pos.getZ() + 0.5 + (rand.nextFloat() - 0.5));
                worldIn.addParticle(ParticleTypes.HEART, d0, d1, d2, 0, 0, 0);
            }
        }
    }
}
