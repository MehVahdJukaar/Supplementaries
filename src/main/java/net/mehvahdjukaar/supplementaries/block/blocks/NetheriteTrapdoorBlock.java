package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.block.util.ILavaAndWaterLoggable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.Half;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NetheriteTrapdoorBlock extends TrapDoorBlock implements ILavaAndWaterLoggable {
    public static final BooleanProperty LAVALOGGED = BlockProperties.LAVALOGGED;
    public NetheriteTrapdoorBlock(Properties builder) {
        super(builder);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false).setValue(HALF, Half.BOTTOM).setValue(POWERED, false)
                .setValue(WATERLOGGED, false).setValue(LAVALOGGED,false));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

        BlockPos p = this.hasTileEntity(state)? pos : pos.below();
        TileEntity te = worldIn.getBlockEntity(p);
        if (te instanceof KeyLockableTile) {
            if (((KeyLockableTile) te).handleAction(player, handIn,"trapdoor")) {
                state = state.cycle(OPEN);
                worldIn.setBlock(pos, state, 2);
                if (state.getValue(WATERLOGGED)) {
                    worldIn.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
                }

                //TODO: replace with proper sound event
                this.playSound(player, worldIn, pos, state.getValue(OPEN));
            }
        }

        return ActionResultType.sidedSuccess(worldIn.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        else if (state.getValue(LAVALOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(worldIn));
        }
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        state = state.setValue(LAVALOGGED,fluidstate.getType() == Fluids.LAVA);
        if(state==null)return state;
        return state.setValue(OPEN,false).setValue(POWERED,false);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new KeyLockableTile();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState p_196271_3_, IWorld p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
        if (state.getValue(LAVALOGGED)) {
            p_196271_4_.getLiquidTicks().scheduleTick(p_196271_5_, Fluids.LAVA, Fluids.LAVA.getTickDelay(p_196271_4_));
        }
        return super.updateShape(state, direction, p_196271_3_, p_196271_4_, p_196271_5_, p_196271_6_);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAVALOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(LAVALOGGED) ? Fluids.LAVA.getSource(false) : super.getFluidState(state);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LAVALOGGED) ? 15 : 0;
    }

    @Override
    public boolean canPlaceLiquid(IBlockReader p_204510_1_, BlockPos p_204510_2_, BlockState p_204510_3_, Fluid p_204510_4_) {
        return ILavaAndWaterLoggable.super.canPlaceLiquid(p_204510_1_, p_204510_2_, p_204510_3_,  p_204510_4_);
    }

    @Override
    public boolean placeLiquid(IWorld p_204509_1_, BlockPos p_204509_2_, BlockState p_204509_3_, FluidState p_204509_4_) {
        return ILavaAndWaterLoggable.super.placeLiquid(p_204509_1_, p_204509_2_, p_204509_3_, p_204509_4_);
    }

    @Override
    public Fluid takeLiquid(IWorld p_204508_1_, BlockPos p_204508_2_, BlockState p_204508_3_) {
        return ILavaAndWaterLoggable.super.takeLiquid(p_204508_1_, p_204508_2_, p_204508_3_);
    }
}
