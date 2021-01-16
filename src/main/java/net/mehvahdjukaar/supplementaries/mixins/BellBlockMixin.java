package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.IBellConnection;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(BellBlock.class)
public class BellBlockMixin extends Block{

    public BellBlockMixin(Properties properties) {
        super(properties);
    }

    private void tryConnect(BlockPos pos, BlockState facingState, IWorld world){
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof IBellConnection){
            boolean canConnect = (facingState.getBlock() instanceof ChainBlock && facingState.get(ChainBlock.AXIS)== Direction.Axis.Y);
            ((IBellConnection) te).setConnected(canConnect);
            te.markDirty();
        }
    }

    @Inject(method = "updatePostPlacement", at = @At("HEAD"), cancellable = true)
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
                                          BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> info) {
        if(facing==Direction.DOWN){
            this.tryConnect(currentPos,facingState,worldIn);
        }
        return stateIn;
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public BlockState getStateForPlacement(BlockItemUseContext context, CallbackInfoReturnable<BlockState> info) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        this.tryConnect(pos,world.getBlockState(pos.down()),world);
        return this.getDefaultState();
    }


}