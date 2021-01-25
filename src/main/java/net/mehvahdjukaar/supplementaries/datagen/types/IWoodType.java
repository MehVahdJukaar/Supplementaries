package net.mehvahdjukaar.supplementaries.datagen.types;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public interface IWoodType {
    String toString();

    Material getMaterial();

    MaterialColor getColor();

    String getLocation();


}