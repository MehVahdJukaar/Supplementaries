package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StonecutterBlock.class)
public abstract class StonecutterBlockMixin {


    protected void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn, CallbackInfo info) {

    }


}
