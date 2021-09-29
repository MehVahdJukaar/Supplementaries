package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.PedestalBlockTile;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class PedestalBlock extends WaterBlock {
    protected static final VoxelShape SHAPE = VoxelShapes.or(VoxelShapes.box(0.1875D, 0.125D, 0.1875D, 0.815D, 0.885D, 0.815D),
            VoxelShapes.box(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D),
            VoxelShapes.box(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D));
    protected static final VoxelShape SHAPE_UP = VoxelShapes.or(VoxelShapes.box(0.1875D, 0.125D, 0.1875D, 0.815D, 1, 0.815D),
            VoxelShapes.box(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D));
    protected static final VoxelShape SHAPE_DOWN = VoxelShapes.or(VoxelShapes.box(0.1875D, 0, 0.1875D, 0.815D, 0.885D, 0.815D),
            VoxelShapes.box(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D));
    protected static final VoxelShape SHAPE_UP_DOWN = VoxelShapes.box(0.1875D, 0, 0.1875D, 0.815D, 1, 0.815D);

    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty HAS_ITEM = BlockProperties.HAS_ITEM;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public PedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(UP, false).setValue(AXIS, Direction.Axis.X)
                .setValue(DOWN, false).setValue(WATERLOGGED, false).setValue(HAS_ITEM, false));
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof PedestalBlockTile) {
            if (((PedestalBlockTile) te).type == PedestalBlockTile.DisplayType.CRYSTAL) return 3;
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, WATERLOGGED, HAS_ITEM, AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean flag = world.getFluidState(pos).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(AXIS, context.getHorizontalDirection().getAxis())
                .setValue(UP, canConnect(world.getBlockState(pos.above()), pos, world, Direction.UP, false))
                .setValue(DOWN, canConnect(world.getBlockState(pos.below()), pos, world, Direction.DOWN, false));
    }

    public static boolean canConnect(BlockState state, BlockPos pos, IWorld world, Direction dir, boolean hasItem) {
        if (state.getBlock() instanceof PedestalBlock) {
            if (dir == Direction.DOWN) {
                return !state.getValue(HAS_ITEM);
            } else if (dir == Direction.UP) {
                return !hasItem;
            }
        }
        return false;
    }

    //called when a neighbor is placed
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        if (facing == Direction.UP) {
            return stateIn.setValue(UP, canConnect(facingState, currentPos, worldIn, facing, stateIn.getValue(HAS_ITEM)));
        } else if (facing == Direction.DOWN) {
            return stateIn.setValue(DOWN, canConnect(facingState, currentPos, worldIn, facing, stateIn.getValue(HAS_ITEM)));
        }
        return stateIn;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if (target.getLocation().y() > pos.getY() + 1 - 0.1875) {
            if (te instanceof ItemDisplayTile) {
                ItemStack i = ((ItemDisplayTile) te).getDisplayedItem();
                if (!i.isEmpty()) return i;
            }
        }
        return new ItemStack(this, 1);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        //create new tile
        if (!state.getValue(HAS_ITEM)) {
            worldIn.setBlock(pos, state.setValue(HAS_ITEM, true), Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.BLOCK_UPDATE);
        }
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        ActionResultType resultType = ActionResultType.PASS;
        if (tileentity instanceof PedestalBlockTile) {
            PedestalBlockTile te = (PedestalBlockTile) tileentity;

            ItemStack handItem = player.getItemInHand(handIn);

            //Indiana Jones swap
            if (handItem.getItem() instanceof SackItem) {

                ItemStack it = handItem.copy();
                it.setCount(1);
                ItemStack removed = te.removeItemNoUpdate(0);
                te.setDisplayedItem(it);

                if (!player.isCreative()) {
                    handItem.shrink(1);
                }
                if (!worldIn.isClientSide()) {
                    player.setItemInHand(handIn, removed);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, worldIn.random.nextFloat() * 0.10F + 0.95F);
                    te.setChanged();
                } else {
                    //also update visuals on client. will get overwritten by packet tho
                    te.updateClientVisualsOnLoad();
                }
                resultType = ActionResultType.sidedSuccess(worldIn.isClientSide);
            } else {
                resultType = te.interact(player, handIn);
            }
            if (resultType.consumesAction()) {
                Direction.Axis axis = player.getDirection().getAxis();
                boolean isEmpty = te.getDisplayedItem().isEmpty();
                if (axis != state.getValue(AXIS) || isEmpty) {
                    worldIn.setBlock(pos, state.setValue(AXIS, axis).setValue(HAS_ITEM, !isEmpty), 2);
                    if (isEmpty) worldIn.removeBlockEntity(pos);
                }
            }
        }
        return resultType;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {

        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        if (!up) {
            if (!down) {
                return SHAPE;
            } else {
                return SHAPE_DOWN;
            }
        } else {
            if (!down) {
                return SHAPE_UP;
            } else {
                return SHAPE_UP_DOWN;
            }
        }
    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        //some probably unnecessary optimization
        return state.getValue(HAS_ITEM);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PedestalBlockTile();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof ItemDisplayTile) {
                InventoryHelper.dropContents(world, pos, (IInventory) tileentity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof PedestalBlockTile)
            return ((IInventory) tileentity).isEmpty() ? 0 : 15;
        else
            return 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.getValue(AXIS)) {
                    case Z:
                        return state.setValue(AXIS, Direction.Axis.X);
                    case X:
                        return state.setValue(AXIS, Direction.Axis.Z);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }
}