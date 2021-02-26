package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class NetheriteDoorBlock extends DoorBlock {

    public NetheriteDoorBlock(Properties builder) {
        super(builder);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

        BlockPos p = this.hasTileEntity(state) ? pos : pos.down();
        TileEntity te = worldIn.getTileEntity(p);
        if (te instanceof KeyLockableTile) {
            if (((KeyLockableTile) te).handleAction(player, handIn, "door")) {
                state = state.func_235896_a_(OPEN);
                worldIn.setBlockState(pos, state, 10);

                //TODO: replace with proper sound event
                worldIn.playEvent(player, state.get(OPEN) ? this.getOpenSound() : this.getCloseSound(), pos, 0);
            }
        }

        return ActionResultType.func_233537_a_(worldIn.isRemote);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) { }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return state;
        return state.with(OPEN, false).with(POWERED, false);
    }

    private int getCloseSound() {
        return 1011;
    }

    private int getOpenSound() {
        return 1005;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new KeyLockableTile();
    }

    //TODO: maybe remove this since I'm not using it
    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}
