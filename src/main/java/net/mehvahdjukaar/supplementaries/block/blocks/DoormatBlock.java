package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.DoormatBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.DoormatGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DoormatBlock extends WaterBlock {
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 2.0D, 16.0D, 1.0D, 14.0D);

    protected static final VoxelShape SHAPE_WEST = Block.box(2.0D, 0.0D, 0.0D, 14.0D, 1.0D, 16.0D);
    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    public DoormatBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof DoormatBlockTile && ((IOwnerProtected) tileentity).isAccessibleBy(player)) {
            DoormatBlockTile te = (DoormatBlockTile) tileentity;
            ItemStack itemstack = player.getItemInHand(handIn);
            boolean server = !worldIn.isClientSide();
            boolean flag = itemstack.getItem() instanceof DyeItem && player.abilities.mayBuild;
            boolean sideHit = hit.getDirection() != Direction.UP;
            boolean canExtract = itemstack.isEmpty() && (player.isShiftKeyDown() || sideHit);
            boolean canInsert = te.isEmpty() && sideHit;
            if (canExtract ^ canInsert) {
                if (!server) return ActionResultType.SUCCESS;
                if (canExtract) {
                    ItemStack dropStack = te.removeItemNoUpdate(0);
                    ItemEntity drop = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY() + 0.125, pos.getZ() + 0.5, dropStack);
                    drop.setDefaultPickUpDelay();
                    worldIn.addFreshEntity(drop);
                } else {
                    ItemStack newStack = itemstack.copy();
                    newStack.setCount(1);
                    te.setItems(NonNullList.withSize(1, newStack));
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                }
                te.setChanged();
                worldIn.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundCategory.BLOCKS, 1.0F,
                        1.2f);
                return ActionResultType.CONSUME;

            }
            //color
            if (flag) {
                if (te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())) {
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if (server) te.setChanged();
                }
            }
            // open gui (edit sign with empty hand)
            else if (!server) {
                DoormatGui.open(te);
            }
            return ActionResultType.sidedSuccess(worldIn.isClientSide);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return !worldIn.isEmptyBlock(pos.below());
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING).getAxis()) {
            default:
            case Z:
                return SHAPE_NORTH;
            case X:
                return SHAPE_WEST;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
    }

    //for player bed spawn
    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    /*
    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.OPEN;
    }*/

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DoormatBlockTile();
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
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof IInventory) {
                InventoryHelper.dropContents(world, pos, (IInventory) tileentity);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }

}