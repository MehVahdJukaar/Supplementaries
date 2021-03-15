package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChickenEntity.class)
public interface ChickenEntityAccessor {

    @Accessor("TEMPTATION_ITEMS")
    static void setTemptationItems(Ingredient ingredient){
        throw new AssertionError();
    }
    @Accessor("TEMPTATION_ITEMS")
    static Ingredient getTemptationItems(){
        throw new AssertionError();
    }
}
