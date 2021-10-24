package net.mehvahdjukaar.supplementaries.mixins.accessors;


import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//TODO: maybe replace with access modifier
@Mixin(AbstractHorse.class)
public interface HorseAccessor {

    @Accessor("FOOD_ITEMS")
    static void setFoodItems(Ingredient ingredient){
        throw new AssertionError();
    }
    @Accessor("FOOD_ITEMS")
    static Ingredient getFoodItems(){
        throw new AssertionError();
    }

}


