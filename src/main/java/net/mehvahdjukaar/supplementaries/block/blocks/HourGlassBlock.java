package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;


public class HourGlassBlock extends Block implements IWaterLoggable, IForgeBlock {
    protected static final VoxelShape SHAPE_Y = Block.makeCuboidShape(4D, 0D, 4.0D, 12.0D, 16D, 12.0D);
    protected static final VoxelShape SHAPE_Z = Block.makeCuboidShape(4D, 4D, 0.0D, 12.0D, 12D, 16.0D);
    protected static final VoxelShape SHAPE_X = Block.makeCuboidShape(0D, 4D, 4D, 16D, 12D, 12.0D);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public HourGlassBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP).with(LIGHT_LEVEL, 0)
                .with(WATERLOGGED, false));
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.get(LIGHT_LEVEL);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, LIGHT_LEVEL);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        boolean flag = world.getFluidState(pos).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(WATERLOGGED, flag).with(FACING, context.getFace());
    }

    //called when a neighbor is placed
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return stateIn;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        if(player.isSneaking()&&player.getHeldItem(handIn).isEmpty()&&state.get(FACING).getAxis() == Direction.Axis.Y){
            if(!worldIn.isRemote) {
                worldIn.setBlockState(pos, state.with(FACING, state.get(FACING).getOpposite()), 3);
                worldIn.playSound(null,pos, SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, SoundCategory.BLOCKS,1,1);
                return ActionResultType.CONSUME;
            }
            return ActionResultType.SUCCESS;
        }

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof HourGlassBlockTile) {
            HourGlassBlockTile te = (HourGlassBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            boolean flag1 = (te.isEmpty() && !itemstack.isEmpty() && (te.isItemValidForSlot(0, itemstack)));
            boolean flag2 = (itemstack.isEmpty() && !te.isEmpty());
            if (flag1) {
                ItemStack it = itemstack.copy();
                it.setCount(1);
                NonNullList<ItemStack> stacks = NonNullList.withSize(1, it);
                te.setItems(stacks);
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                if (!worldIn.isRemote()) {
                    worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.10F + 0.95F);
                    te.markDirty();
                }
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            } else if (flag2) {
                ItemStack it = te.removeStackFromSlot(0);
                player.setHeldItem(handIn, it);
                if (!worldIn.isRemote()) {
                    te.markDirty();
                }
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING).getAxis()) {
            case Z:
                return SHAPE_Z;
            default:
            case Y:
                return SHAPE_Y;
            case X:
                return SHAPE_X;
        }
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new HourGlassBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof HourGlassBlockTile) {
                InventoryHelper.dropInventoryItems(world, pos, (HourGlassBlockTile) tileentity);
                world.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof HourGlassBlockTile) {
            return ((HourGlassBlockTile) tileentity).power;
        } else
            return 0;
    }


    //TODO: camelcase all nbts 4 consistency

    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(!ClientConfigs.cached.TOOLTIP_HINTS)return;
        tooltip.add((new TranslationTextComponent(  "message.supplementaries.hourglass")).mergeStyle(TextFormatting.GRAY));
    }

}