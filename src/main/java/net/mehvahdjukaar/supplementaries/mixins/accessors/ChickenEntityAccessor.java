package net.mehvahdjukaar.supplementaries.mixins.accessors;


import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChickenEntity.class)
public interface ChickenEntityAccessor {


    @Accessor("FOOD_ITEMS")
    static void setFoodItems(Ingredient ingredient){
        throw new AssertionError();
    }
    @Accessor("FOOD_ITEMS")
    static Ingredient getFoodItems(){
        throw new AssertionError();
    }
}
