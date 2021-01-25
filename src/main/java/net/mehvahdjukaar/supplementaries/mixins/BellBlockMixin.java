package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.util.IBellConnection;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BellBlock.class)
public abstract class BellBlockMixin extends Block{

    public BellBlockMixin(Properties properties) {
        super(properties);
    }


    //for bells
    public boolean tryConnect(BlockPos pos, BlockState facingState, IWorld world){
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof IBellConnection){
            boolean canConnect = (facingState.getBlock() instanceof ChainBlock && facingState.get(ChainBlock.AXIS)== Direction.Axis.Y);
            ((IBellConnection) te).setConnected(canConnect);
            te.markDirty();
            return true;
       }
        return false;
    }

    @Inject(method = "updatePostPlacement", at = @At("HEAD"), cancellable = true)
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
                                          BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> info) {
        try{
            if (facing == Direction.DOWN) {
                if (this.tryConnect(currentPos, facingState, worldIn)) {
                    if (worldIn instanceof World)
                        ((World) worldIn).notifyBlockUpdate(currentPos, stateIn, stateIn, Constants.BlockFlags.BLOCK_UPDATE);
                }
            }
        }catch (Exception ignored){};
        return stateIn;
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        this.tryConnect(pos,worldIn.getBlockState(pos.down()),worldIn);
    }
}