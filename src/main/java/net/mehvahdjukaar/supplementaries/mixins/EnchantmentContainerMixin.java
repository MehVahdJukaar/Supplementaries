package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(EnchantmentContainer.class)
public abstract class EnchantmentContainerMixin {

    @Redirect(method ="slotsChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;isEmptyBlock(Lnet/minecraft/util/math/BlockPos;)Z",
                    ordinal = 1))
    public boolean isEmptyBlock(World world, BlockPos pos) {
        return world.isEmptyBlock(pos) || (ServerConfigs.cached.ENCHANTMENT_BYPASS && world.getBlockState(pos).is(ModTags.ENCHANTMENT_BYPASS));
    }

}
