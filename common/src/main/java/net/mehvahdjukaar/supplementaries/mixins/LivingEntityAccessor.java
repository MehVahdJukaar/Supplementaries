package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Invoker("shouldDropLoot")
    boolean invokeShouldDropLoot();

    @Accessor("useItem")
    void setUseItem(ItemStack useItem);

}
