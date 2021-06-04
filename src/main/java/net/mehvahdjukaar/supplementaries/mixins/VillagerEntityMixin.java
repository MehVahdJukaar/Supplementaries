package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.entity.merchant.villager.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {
    /*
    @Inject(method = "wantsToPickUp", at = @At("HEAD"), cancellable = true)
    private void wantsToPickUp(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(this.getVillagerData().getProfession()== VillagerProfession.FARMER && Tags.Items.SEEDS.contains(stack.getItem())){
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Shadow
    public abstract VillagerData getVillagerData();
    */

}
