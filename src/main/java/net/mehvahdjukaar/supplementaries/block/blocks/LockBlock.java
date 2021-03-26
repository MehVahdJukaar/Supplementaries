package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public class LockBlock extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(state.getValue(POWERED))return ActionResultType.PASS;
        BlockPos p = this.hasTileEntity(state)? pos : pos.below();
        TileEntity te = worldIn.getBlockEntity(p);
        if (te instanceof KeyLockableTile) {
            if (((KeyLockableTile) te).handleAction(player, handIn,"lock_block")) {
                if(!worldIn.isClientSide) {
                    worldIn.setBlock(pos, state.setValue(POWERED, true), 2|4|3);
                    worldIn.getBlockTicks().scheduleTick(pos, this, 20);
                }

                worldIn.levelEvent(player, 1037, pos, 0);
                //this.playSound(player, worldIn, pos, true);
            }
        }

        return ActionResultType.sidedSuccess(worldIn.isClientSide);
    }

    protected void playSound(@Nullable PlayerEntity player, World worldIn, BlockPos pos, boolean isOpened) {
        if (isOpened) {
            int i = this.material == Material.METAL ? 1037 : 1007;
            worldIn.levelEvent(player, i, pos, 0);
        } else {
            int j = this.material == Material.METAL ? 1036 : 1013;
            worldIn.levelEvent(player, j, pos, 0);
        }

    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
        if(!worldIn.isClientSide && state.getValue(POWERED)){
            worldIn.setBlock(pos, state.setValue(POWERED,false),2|4|3);
        }

    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED)?15:0;
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
