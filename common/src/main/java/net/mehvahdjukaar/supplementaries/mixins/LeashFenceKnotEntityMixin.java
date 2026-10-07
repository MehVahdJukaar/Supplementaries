package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LeashFenceKnotEntity.class)
public abstract class LeashFenceKnotEntityMixin {

    @WrapOperation(method = "survives", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean supp$surviveOnRopes(BlockState state, TagKey<Block> tag, Operation<Boolean> original) {
        return original.call(state, tag) || RopeBlock.canHoldLeashKnot(state);
    }
}
