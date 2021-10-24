package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.block.util.IBellConnections;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

@Mixin(BellBlock.class)
public abstract class BellMixin extends Block{

    public BellMixin(Properties properties) {
        super(properties);
    }


    //for bells
    public boolean tryConnect(BlockPos pos, BlockState facingState, LevelAccessor world){
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof IBellConnections){
            IBellConnections.BellConnection connection = IBellConnections.BellConnection.NONE;
            if(facingState.getBlock() instanceof ChainBlock && facingState.getValue(ChainBlock.AXIS)== Direction.Axis.Y)
                connection = IBellConnections.BellConnection.CHAIN;
            else if(facingState.getBlock() instanceof RopeBlock)
                connection = IBellConnections.BellConnection.ROPE;
            ((IBellConnections) te).setConnected(connection);
            te.setChanged();
            return true;
       }
        return false;
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    public void updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
                                    BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> info) {
        try{
            if (facing == Direction.DOWN) {
                if (this.tryConnect(currentPos, facingState, worldIn)) {
                    if (worldIn instanceof Level)
                        ((Level) worldIn).sendBlockUpdated(currentPos, stateIn, stateIn, Constants.BlockFlags.BLOCK_UPDATE);
                }
            }
        }catch (Exception ignored){}
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        this.tryConnect(pos,worldIn.getBlockState(pos.below()),worldIn);
    }
}