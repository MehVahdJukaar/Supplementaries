package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public interface IWoodType {
    String toString();

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