package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.HangingSignBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.HangingSignGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.tileentity.LockableTileEntity;
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
import org.jetbrains.annotations.Nullable;

public class HangingSignBlock extends SwayingBlock {
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.box(0.4375D, 0D, 0D, 0.5625D, 1D, 1D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.box(0D, 0D, 0.5625D, 1D, 1D, 0.4375D);

    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty TILE = BlockProperties.TILE; // is it renderer by tile entity? animated part

    public HangingSignBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(EXTENSION, 0).setValue(FACING, Direction.NORTH).setValue(TILE, false).setValue(HANGING,false));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof HangingSignBlockTile && ((IOwnerProtected) tileentity).isAccessibleBy(player)) {
            HangingSignBlockTile te = (HangingSignBlockTile) tileentity;
            ItemStack handItem = player.getItemInHand(handIn);
            boolean server = !worldIn.isClientSide();
            boolean isDye = handItem.getItem() instanceof DyeItem && player.abilities.mayBuild;
            //color
            if (isDye){
                if(te.textHolder.setTextColor(((DyeItem) handItem.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        handItem.shrink(1);
                    }
                    if(server)te.setChanged();
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
            }
            //not an else to allow to place dye items after coloring
            //place item
            //TODO: return early for client. fix left hand(shield)
            if(handIn==Hand.MAIN_HAND) {
                //remove
                if (!te.isEmpty() && handItem.isEmpty()) {
                    ItemStack it = te.removeStackFromSlot(0);
                    if (!worldIn.isClientSide()) {
                        player.setItemInHand(handIn, it);
                        te.setChanged();
                    }
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
                //place
                else if (!handItem.isEmpty() && te.isEmpty()) {
                    ItemStack it = handItem.copy();
                    it.setCount(1);
                    te.setItems(NonNullList.withSize(1, it));

                    if (!player.isCreative()) {
                        handItem.shrink(1);
                    }
                    if (!worldIn.isClientSide()) {
                        worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, worldIn.random.nextFloat() * 0.10F + 0.95F);
                        te.setChanged();
                    }
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }

                // open gui (edit sign with empty hand)
                else if (handItem.isEmpty()) {
                    if (!server) HangingSignGui.open(te);
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        if(state.getValue(HANGING)){
            return worldIn.getBlockState(pos.above()).isFaceSturdy(worldIn, pos.above(), Direction.DOWN);
        }
        else {
            return worldIn.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).getMaterial().isSolid();
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        if(facing==Direction.UP){
            return !stateIn.canSurvive(worldIn, currentPos)
                    ? Blocks.AIR.defaultBlockState()
                    : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
        else {
            return facing == stateIn.getValue(FACING).getOpposite()? !stateIn.canSurvive(worldIn, currentPos)
                    ? Blocks.AIR.defaultBlockState()
                    : getConnectedState(stateIn,facingState, worldIn,facingPos) : stateIn;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING).getAxis()) {
            default:
            case Z:
                return SHAPE_NORTH;
            case X :
                return SHAPE_WEST;
        }
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return state.getValue(HANGING) ? BlockRenderType.ENTITYBLOCK_ANIMATED : BlockRenderType.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING,TILE);
    }

    //TODO: merge with lantern
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        if (context.getClickedFace() == Direction.DOWN||context.getClickedFace() == Direction.UP) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getCounterClockWise())
                    .setValue(HANGING, context.getClickedFace()==Direction.DOWN).setValue(WATERLOGGED, water);
        }
        BlockPos blockpos = context.getClickedPos();
        World world = context.getLevel();
        BlockPos facingpos = blockpos.relative(context.getClickedFace().getOpposite());
        BlockState facingState = world.getBlockState(facingpos);

        return getConnectedState(this.defaultBlockState(),facingState, world,facingpos).setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED,water);
    }

    //for player bed spawn
    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.OPEN;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof HangingSignBlockTile) {
                //InventoryHelper.dropInventoryItems(world, pos, (HangingSignBlockTile) tileentity);

                ItemStack itemstack =  ((HangingSignBlockTile) tileentity).getStackInSlot(0);
                ItemEntity itementity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemstack);                itementity.setDefaultPickUpDelay();
                world.addFreshEntity(itementity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new HangingSignBlockTile();
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }

}

