package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.OilLanternBlockTile;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class OilLanternBlock extends EnhancedLanternBlock {
    public static final VoxelShape SHAPE_DOWN = VoxelShapes.or(Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D), Block.box(6.0D, 8.0D, 6.0D, 10.0D, 9.0D, 10.0D));
    public static final VoxelShape SHAPE_UP = VoxelShapes.or(Block.box(5.0D, 5.0D, 5.0D, 11.0D, 13.0D, 11.0D), Block.box(6.0D, 13.0D, 6.0D, 10.0D, 14.0D, 10.0D));

    public static final EnumProperty<AttachFace> FACE = HorizontalFaceBlock.FACE;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public OilLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(LIT,true)
                .setValue(FACING,Direction.NORTH).setValue(EXTENSION,0).setValue(FACE,AttachFace.FLOOR));
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        switch (state.getValue(FACE)) {
            default:
            case FLOOR:
                return Block.canSupportCenter(worldIn, pos.below(), Direction.UP);
            case CEILING:
                return RopeBlock.isSupportingCeiling(pos.above(),worldIn);
            case WALL:
                return super.canSurvive(state,worldIn,pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        switch(stateIn.getValue(FACE)) {
            default:
            case WALL:
                return facing == stateIn.getValue(FACING).getOpposite() ? !stateIn.canSurvive(worldIn, currentPos)
                        ? Blocks.AIR.defaultBlockState()
                        : this.getConnectedState(stateIn, facingState, worldIn, facingPos) : stateIn;
            case CEILING:
                return facing == Direction.UP && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
            case FLOOR:
                return facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;

        for(Direction direction : context.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING, context.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            World world = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            if (blockstate.canSurvive(world, blockpos)) {

                BlockPos facingpos = blockpos.relative(direction);
                BlockState facingState = world.getBlockState(facingpos);

                return this.getConnectedState(blockstate,facingState, world, facingpos).setValue(WATERLOGGED,water);
            }
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch(state.getValue(FACE)) {
            default:
            case FLOOR:
                return SHAPE_DOWN;
            case CEILING:
                return SHAPE_UP;
            case WALL:
                return super.getShape(state,world,pos,context);
        }
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if(state.getValue(FACE)==AttachFace.WALL)
            super.entityInside(state, world, pos, entity);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        if(state.getValue(FACE)==AttachFace.CEILING)return BlockRenderType.ENTITYBLOCK_ANIMATED;
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(player.abilities.mayBuild) {
            ItemStack item = player.getItemInHand(handIn);
            if(!state.getValue(LIT)) {
                if (item.getItem() instanceof FlintAndSteelItem) {
                    if (!worldIn.isClientSide) {
                        worldIn.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, worldIn.getRandom().nextFloat() * 0.4F + 0.8F);
                        worldIn.setBlock(pos, state.setValue(LIT, true), 3);
                    }
                    item.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                } else if (item.getItem() instanceof FireChargeItem) {
                    if (!worldIn.isClientSide) {
                        worldIn.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (worldIn.getRandom().nextFloat() - worldIn.getRandom().nextFloat()) * 0.2F + 1.0F);
                        worldIn.setBlock(pos, state.setValue(LIT, true), 3);
                    }
                    if (!player.isCreative()) item.shrink(1);
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
            }
            else if(item.isEmpty()){
                if (!worldIn.isClientSide) {
                    worldIn.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
                    worldIn.setBlock(pos, state.setValue(LIT, false), 3);
                }
                return ActionResultType.sidedSuccess(worldIn.isClientSide);
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FACE);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new OilLanternBlockTile();
    }
}
