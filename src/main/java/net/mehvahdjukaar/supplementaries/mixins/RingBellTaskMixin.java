package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChainBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.RingBellTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RingBellTask.class)
public abstract class RingBellTaskMixin extends Task<LivingEntity> {


    public RingBellTaskMixin(Map<MemoryModuleType<?>, MemoryModuleStatus> requiredMemoryStateIn) {
        super(requiredMemoryStateIn);
    }

    //might want to remove this since it barely works
    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    protected void startExecuting(ServerWorld worldIn, LivingEntity entityIn, long gameTimeIn, CallbackInfo info) {
        Brain<?> brain = entityIn.getBrain();
        BlockPos blockpos = brain.getMemory(MemoryModuleType.MEETING_POINT).get().pos();
        if (CommonUtil.withinDistanceDown(blockpos, entityIn.position(), 2.0D, 5)) {
            BlockState blockstate = worldIn.getBlockState(blockpos);
            if (blockstate.is(Blocks.BELL)) {
                BellBlock bellblock = (BellBlock)blockstate.getBlock();
                BlockPos.Mutable mut = blockpos.mutable();
                boolean flag = false;
                for (int i = 0; i<5; i++){
                    if(mut.closerThan(entityIn.blockPosition(), 2.0D)){
                        flag = true;
                        break;
                    }
                    mut.move(0,-1,0);
                    BlockState state = worldIn.getBlockState(mut);
                    if(!(state.getBlock() instanceof ChainBlock && state.getValue(ChainBlock.AXIS) == Direction.Axis.Y)){
                        break;
                    }
                }
                if(flag) {
                    bellblock.attemptToRing(worldIn, blockpos, blockstate.getValue(BellBlock.FACING).getClockWise());
                }
                return;
                //TODO: figure out if this is actually ending the function
            }
        }

    }



}