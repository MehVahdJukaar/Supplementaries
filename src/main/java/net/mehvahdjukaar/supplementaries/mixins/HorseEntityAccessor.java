package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractHorseEntity.class)
public interface HorseEntityAccessor {

    @Accessor("field_234235_bE_")
    static void setfield_234235_bE_(Ingredient ingredient){
        throw new AssertionError();
    }
    @Accessor("field_234235_bE_")
    static Ingredient getfield_234235_bE_(){
        throw new AssertionError();
    }
}
