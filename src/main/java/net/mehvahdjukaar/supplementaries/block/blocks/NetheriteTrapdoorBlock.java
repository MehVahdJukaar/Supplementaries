package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class NetheriteTrapdoorBlock extends TrapDoorBlock {

    public NetheriteTrapdoorBlock(Properties builder) {
        super(builder);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

        BlockPos p = this.hasTileEntity(state)? pos : pos.down();
        TileEntity te = worldIn.getTileEntity(p);
        if (te instanceof KeyLockableTile) {
            if (((KeyLockableTile) te).handleAction(player, handIn,"trapdoor")) {
                state = state.func_235896_a_(OPEN);
                worldIn.setBlockState(pos, state, 2);
                if (state.get(WATERLOGGED)) {
                    worldIn.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
                }

                //TODO: replace with proper sound event
                this.playSound(player, worldIn, pos, state.get(OPEN));
            }
        }

        return ActionResultType.func_233537_a_(worldIn.isRemote);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        if(state==null)return state;
        return state.with(OPEN,false).with(POWERED,false);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new KeyLockableTile();
    }

}
