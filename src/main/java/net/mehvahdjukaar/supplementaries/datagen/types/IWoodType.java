package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;

public interface IWoodType {
    String toString();

    default String getRegName(){return this.toString();}

    default String toNBT(){return this.getNamespace()+":"+this.toString();}

    default ResourceLocation toResourceLocation(){
        return new ResourceLocation(this.getNamespace(),this.toString());
    }

    Material getMaterial();

    MaterialColor getColor();

    String getNamespace();

    default String getLocation(){
        return this.getNamespace()+"/";
    }

    default String getPlankRegName() {
        return this.getNamespace()+":"+this.toString()+"_planks";
    }

    default String getSignRegName(){
        return this.getNamespace()+":"+this.toString()+"_sign";
    }

    default String getTranslationName(){
        return this.toString();

    }


}