package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.items.RopeArrowItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;


@Mixin(BowItem.class)
public abstract class BowMixin {

    //TODO: use forge event, test
    @ModifyReturnValue(method = "getAllSupportedProjectiles", at = @At(
            value = "RETURN"))
    public Predicate<ItemStack> getAllSupportedProjectiles(Predicate<ItemStack> originalPred) {
        if(CommonConfigs.Tools.ROPE_ARROW_CROSSBOW.get()){
            return s->{
                if(s.getItem() instanceof RopeArrowItem) return false;
                else return originalPred.test(s);
            };
        }
        return originalPred;
    }
}
