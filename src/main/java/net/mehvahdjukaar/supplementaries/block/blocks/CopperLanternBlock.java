package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.OilLanternBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CopperLanternBlock extends EnhancedLanternBlock {
    public static final VoxelShape SHAPE_DOWN = Shapes.or(Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D), Block.box(6.0D, 8.0D, 6.0D, 10.0D, 9.0D, 10.0D));
    public static final VoxelShape SHAPE_UP = Shapes.or(Block.box(5.0D, 5.0D, 5.0D, 11.0D, 13.0D, 11.0D), Block.box(6.0D, 13.0D, 6.0D, 10.0D, 14.0D, 10.0D));

    public static final EnumProperty<AttachFace> FACE = FaceAttachedHorizontalDirectionalBlock.FACE;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CopperLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(LIT, true)
                .setValue(FACING, Direction.NORTH).setValue(EXTENSION, 0).setValue(FACE, AttachFace.FLOOR));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> Block.canSupportCenter(worldIn, pos.below(), Direction.UP);
            case CEILING -> RopeBlock.isSupportingCeiling(pos.above(), worldIn);
            case WALL -> super.canSurvive(state, worldIn, pos);
        };
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return switch (stateIn.getValue(FACE)) {
            case WALL -> facing == stateIn.getValue(FACING).getOpposite() ? !stateIn.canSurvive(worldIn, currentPos)
                    ? Blocks.AIR.defaultBlockState()
                    : getConnectedState(stateIn, facingState, worldIn, facingPos) : stateIn;
            case CEILING -> facing == Direction.UP && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
            case FLOOR -> facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;

        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, context.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            Level world = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            if (blockstate.canSurvive(world, blockpos)) {

                BlockPos relative = blockpos.relative(direction);
                BlockState facingState = world.getBlockState(relative);

                return getConnectedState(blockstate, facingState, world, relative).setValue(WATERLOGGED, water);
            }
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> SHAPE_DOWN;
            case CEILING -> SHAPE_UP;
            case WALL -> super.getShape(state, world, pos, context);
        };
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (state.getValue(FACE) == AttachFace.WALL)
            super.entityInside(state, world, pos, entity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (state.getValue(FACE) == AttachFace.CEILING) return RenderShape.ENTITYBLOCK_ANIMATED;
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (player.getAbilities().mayBuild) {
            ItemStack item = player.getItemInHand(handIn);
            if (!state.getValue(LIT)) {
                if (item.getItem() instanceof FlintAndSteelItem) {
                    if (!worldIn.isClientSide) {
                        worldIn.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, worldIn.getRandom().nextFloat() * 0.4F + 0.8F);
                        worldIn.setBlock(pos, state.setValue(LIT, true), 3);
                    }
                    item.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                } else if (item.getItem() instanceof FireChargeItem) {
                    if (!worldIn.isClientSide) {
                        worldIn.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (worldIn.getRandom().nextFloat() - worldIn.getRandom().nextFloat()) * 0.2F + 1.0F);
                        worldIn.setBlock(pos, state.setValue(LIT, true), 3);
                    }
                    if (!player.isCreative()) item.shrink(1);
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
            } else if (item.isEmpty()) {
                if (!worldIn.isClientSide) {
                    worldIn.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
                    worldIn.setBlock(pos, state.setValue(LIT, false), 3);
                }
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FACE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new OilLanternBlockTile();
    }
}
