package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.nbt.CompoundNBT;

public interface NBTTile {

    default CompoundNBT saveItemNBT(CompoundNBT compound) {return compound;}
    default void readItemNBT(CompoundNBT compoundNBT) {}
}
