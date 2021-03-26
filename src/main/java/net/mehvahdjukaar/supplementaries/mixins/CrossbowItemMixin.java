package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.RopeArrowItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vazkii.quark.content.building.module.VariantChestsModule;

@Mixin({CrossbowItem.class})
public abstract class CrossbowItemMixin {


    @ModifyVariable(method = "tryLoadProjectiles", at = @At("STORE"), ordinal = 1)
    private static int tryLoadProjectiles(int original, LivingEntity entity, ItemStack stack) {
        if(original>1 && entity.getProjectile(stack).getItem() instanceof RopeArrowItem) return 1;
        return original;
    }
}
