package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.blocks.tiles.OilLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.List;

public class OilLanternBlock extends SwayingBlock {
    protected static final VoxelShape SHAPE_DOWN = VoxelShapes.or(Block.makeCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D), Block.makeCuboidShape(6.0D, 8.0D, 6.0D, 10.0D, 9.0D, 10.0D));
    protected static final VoxelShape SHAPE_UP = VoxelShapes.or(Block.makeCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 13.0D, 11.0D), Block.makeCuboidShape(6.0D, 13.0D, 6.0D, 10.0D, 14.0D, 10.0D));
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.create(0.6875D, 0.125D, 0.625D, 0.3125D, 1D, 0D);
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.create(0.3125D, 0.125D, 0.375D, 0.6875D, 1D, 1D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.create(0.375D, 0.125D, 0.6875D, 1D, 1D, 0.3125D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.create(0.625D, 0.125D, 0.3125D, 0D, 1D, 0.6875D);

    public static final EnumProperty<AttachFace> FACE = HorizontalFaceBlock.FACE;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public OilLanternBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED,false).with(LIT,true)
                .with(FACING,Direction.NORTH).with(EXTENSION,0).with(FACE,AttachFace.FLOOR));
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(!ClientConfigs.cached.TOOLTIP_HINTS)return;
        tooltip.add(new TranslationTextComponent("message.supplementaries.wall_lantern").mergeStyle(TextFormatting.GRAY));

    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(player.abilities.allowEdit) {
            ItemStack item = player.getHeldItem(handIn);
            if(!state.get(LIT)) {
                if (item.getItem() instanceof FlintAndSteelItem) {
                    if (!worldIn.isRemote) {
                        worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, worldIn.getRandom().nextFloat() * 0.4F + 0.8F);
                        worldIn.setBlockState(pos, state.with(LIT, true), 3);
                    }
                    item.damageItem(1, player, (playerIn) -> playerIn.sendBreakAnimation(handIn));
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                } else if (item.getItem() instanceof FireChargeItem) {
                    if (!worldIn.isRemote) {
                        worldIn.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (worldIn.getRandom().nextFloat() - worldIn.getRandom().nextFloat()) * 0.2F + 1.0F);
                        worldIn.setBlockState(pos, state.with(LIT, true), 3);
                    }
                    if (!player.isCreative()) item.shrink(1);
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }
            else if(item.isEmpty()){
                if (!worldIn.isRemote) {
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
                    worldIn.setBlockState(pos, state.with(LIT, false), 3);
                }
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
        }
        return ActionResultType.PASS;
    }

    //TODO: merge with lantern
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean water = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;

        for(Direction direction : context.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.getDefaultState().with(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).with(FACING, context.getPlacementHorizontalFacing());
            } else {
                blockstate = this.getDefaultState().with(FACE, AttachFace.WALL).with(FACING, direction.getOpposite());
            }

            World world = context.getWorld();
            BlockPos blockpos = context.getPos();
            if (blockstate.isValidPosition(world, blockpos)) {

                BlockPos facingpos = blockpos.offset(direction);
                BlockState facingState = world.getBlockState(facingpos);

                return this.getConnectedState(blockstate,facingState, world, facingpos).with(WATERLOGGED,water);
            }
        }
        return null;
    }


    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        switch(stateIn.get(FACE)) {
            default:
            case WALL:
            return facing == stateIn.get(FACING).getOpposite() ? !stateIn.isValidPosition(worldIn, currentPos)
                    ? Blocks.AIR.getDefaultState()
                    : this.getConnectedState(stateIn, facingState, worldIn, facingPos) : stateIn;
            case CEILING:
                return facing == Direction.UP && !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : stateIn;
            case FLOOR:
                return facing == Direction.DOWN && !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : stateIn;
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        switch (state.get(FACE)) {
            default:
            case FLOOR:
                return Block.hasEnoughSolidSide(worldIn, pos.offset(Direction.DOWN), Direction.UP);
            case CEILING:
                return Block.hasEnoughSolidSide(worldIn, pos.offset(Direction.UP), Direction.DOWN);
            case WALL:
                Direction direction = state.get(FACING);
                BlockPos blockpos = pos.offset(direction.getOpposite());
                BlockState blockstate = worldIn.getBlockState(blockpos);
                return (blockstate.isSolidSide(worldIn, blockpos, direction) || CommonUtil.getPostSize(blockstate, blockpos, worldIn) > 0);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if(state.get(FACE)==AttachFace.WALL)
            super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(LIT,FACE);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new OilLanternBlockTile();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        if(state.get(FACE)==AttachFace.CEILING)return BlockRenderType.ENTITYBLOCK_ANIMATED;
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch(state.get(FACE)) {
            default:
            case FLOOR:
                return SHAPE_DOWN;
            case CEILING:
                return SHAPE_UP;
            case WALL:
            switch (state.get(FACING)) {
                case UP:
                case DOWN:
                case SOUTH:
                default:
                    return SHAPE_SOUTH;
                case NORTH:
                    return SHAPE_NORTH;
                case WEST:
                    return SHAPE_WEST;
                case EAST:
                    return SHAPE_EAST;
            }
        }
    }
}
