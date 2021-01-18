package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.blocks.tiles.HangingSignBlockTile;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.gui.HangingSignGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
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


public class HangingSignBlock extends SwayingBlock {
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.create(0.4375D, 0D, 0D, 0.5625D, 1D, 1D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.create(0D, 0D, 0.5625D, 1D, 1D, 0.4375D);

    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty TILE = Resources.TILE; // is it renderer by tile entity? animated part

    public HangingSignBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, false)
                .with(EXTENSION, 0).with(FACING, Direction.NORTH).with(TILE, false).with(HANGING,false));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof HangingSignBlockTile) {
            HangingSignBlockTile te = (HangingSignBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            boolean server = !worldIn.isRemote();
            boolean emptyhand = itemstack.isEmpty();
            boolean flag = itemstack.getItem() instanceof DyeItem && player.abilities.allowEdit;
            boolean flag1 = te.isEmpty() && !emptyhand;
            boolean flag2 = !te.isEmpty() && emptyhand;
            //color
            if (flag){
                if(te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server)te.markDirty();
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }
            //not an else to allow to place dye items after coloring
            //place item
            if (flag1) {
                ItemStack it = itemstack.copy();
                it.setCount(1);
                NonNullList<ItemStack> stacks = NonNullList.withSize(1, it);
                te.setItems(stacks);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                if (!worldIn.isRemote()) {
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F,
                            worldIn.rand.nextFloat() * 0.10F + 0.95F);
                    te.markDirty();
                }
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
            //remove item
            else if (flag2) {
                ItemStack it = te.removeStackFromSlot(0);
                player.setHeldItem(handIn, it);
                if(server)te.markDirty();
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
            // open gui (edit sign with empty hand)
            else if (!server && emptyhand) {
                HangingSignGui.open(te);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        if(state.get(HANGING)){
            return worldIn.getBlockState(pos.up()).isSolidSide(worldIn, pos.up(), Direction.DOWN);
        }
        else {
            return worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite())).getMaterial().isSolid();
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        if(facing==Direction.UP){
            return !stateIn.isValidPosition(worldIn, currentPos)
                    ? Blocks.AIR.getDefaultState()
                    : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
        else {
            return facing == stateIn.get(FACING).getOpposite()? !stateIn.isValidPosition(worldIn, currentPos)
                    ? Blocks.AIR.getDefaultState()
                    : this.getConnectedState(stateIn,facingState, (World) worldIn,facingPos) : stateIn;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING).getAxis()) {
            default:
            case Z:
                return SHAPE_NORTH;
            case X :
                return SHAPE_WEST;
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return state.get(HANGING) ? BlockRenderType.ENTITYBLOCK_ANIMATED : BlockRenderType.MODEL;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(HANGING,TILE);
    }

    //TODO: merge with lantern
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean water = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        if (context.getFace() == Direction.DOWN||context.getFace() == Direction.UP) {
            return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().rotateYCCW())
                    .with(HANGING, context.getFace()==Direction.DOWN).with(WATERLOGGED, water);
        }
        BlockPos blockpos = context.getPos();
        World world = context.getWorld();
        BlockPos facingpos = blockpos.offset(context.getFace().getOpposite());
        BlockState facingState = world.getBlockState(facingpos);

        return this.getConnectedState(this.getDefaultState(),facingState, world,facingpos).with(FACING, context.getFace()).with(WATERLOGGED,water);
    }

    //for player bed spawn
    @Override
    public boolean canSpawnInBlock() {
        return true;
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.OPEN;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof HangingSignBlockTile) {
                //InventoryHelper.dropInventoryItems(world, pos, (HangingSignBlockTile) tileentity);

                ItemStack itemstack =  ((HangingSignBlockTile) tileentity).getStackInSlot(0);
                ItemEntity itementity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemstack);                itementity.setDefaultPickupDelay();
                world.addEntity(itementity);
                world.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new HangingSignBlockTile();
    }


}

