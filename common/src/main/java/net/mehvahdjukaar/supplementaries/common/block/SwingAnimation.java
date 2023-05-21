package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public abstract class SwingAnimation {

   private final Function<BlockState, Vec3i> axisGetter;
   //all client stuff
   protected float angle = 0;
   protected float prevAngle = 0;

   protected SwingAnimation(Function<BlockState, Vec3i> axisGetter){
      this.axisGetter = axisGetter;
   }

   protected Vec3i getRotationAxis(BlockState state){
      return axisGetter.apply(state);
   }

   public abstract void tick(Level pLevel, BlockPos pPos, BlockState pState);
   public abstract boolean hitByEntity(Entity entity, BlockState state, BlockPos pos);
   public abstract float getAngle(float partialTicks);

   public abstract void reset();
}
