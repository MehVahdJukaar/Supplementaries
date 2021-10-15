package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.ModList;

public interface IWoodType {

    String toString();

    default String getRegName(){return this.toString();}

    default String toNBT(){return this.getNamespace()+":"+this.toString();}

    /*
    default ResourceLocation toResourceLocation(){
        return new ResourceLocation(this.getNamespace(),this.toString());
    }*/

    default Material getMaterial(){
        return Blocks.OAK_PLANKS.defaultBlockState().getMaterial();
    }

    default MaterialColor getColor(){
        return Blocks.OAK_PLANKS.defaultMaterialColor();
    }

    String getNamespace();

    default String getLocation(){
        return this.getNamespace()+"/";
    }

    default String getPlankRegName() {
        return this.getNamespace()+":"+ this +"_planks";
    }

    default String getSignRegName(){
        return this.getNamespace()+":"+ this +"_sign";
    }

    default String getTranslationName(){
        return this.toString();

    }

    default boolean isModActive(){
        return ModList.get().isLoaded(this.getNamespace());
    }

}