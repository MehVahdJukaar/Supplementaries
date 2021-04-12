package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
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
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof IBellConnection){
            IBellConnection.BellConnection connection = IBellConnection.BellConnection.NONE;
            if(facingState.getBlock() instanceof ChainBlock && facingState.getValue(ChainBlock.AXIS)== Direction.Axis.Y)
                connection = IBellConnection.BellConnection.CHAIN;
            else if(facingState.getBlock() instanceof RopeBlock)
                connection = IBellConnection.BellConnection.ROPE;
            ((IBellConnection) te).setConnected(connection);
            te.setChanged();
            return true;
       }
        return false;
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    public void updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
                                    BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> info) {
        try{
            if (facing == Direction.DOWN) {
                if (this.tryConnect(currentPos, facingState, worldIn)) {
                    if (worldIn instanceof World)
                        ((World) worldIn).sendBlockUpdated(currentPos, stateIn, stateIn, Constants.BlockFlags.BLOCK_UPDATE);
                }
            }
        }catch (Exception ignored){};
    }


    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        this.tryConnect(pos,worldIn.getBlockState(pos.below()),worldIn);
    }
}