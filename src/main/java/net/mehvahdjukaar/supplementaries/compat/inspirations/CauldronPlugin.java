package net.mehvahdjukaar.supplementaries.compat.inspirations;

import knightminer.inspirations.library.recipe.cauldron.CauldronContentTypes;
import knightminer.inspirations.library.recipe.cauldron.contents.CauldronContentType;
import knightminer.inspirations.library.recipe.cauldron.contents.ICauldronContents;
import knightminer.inspirations.recipes.tileentity.CauldronTileEntity;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;

import java.util.function.Supplier;

public class CauldronPlugin {

    public static boolean doStuff(TileEntity cauldronTile, SoftFluidHolder faucetFluidHolder, boolean doTransfer, Supplier<Boolean> transferBelow) {
        if (cauldronTile instanceof CauldronTileEntity) {
            CauldronTileEntity te = (CauldronTileEntity) cauldronTile;

            ICauldronContents contents = te.getContents();

            CauldronContentType<?> contentType = contents.getType();
            SoftFluid equivalent;
            CompoundNBT nbt = new CompoundNBT();
            int levelToBottleEquivalent;
            if (contentType == CauldronContentTypes.FLUID) {
                //ugly. might not work
                Fluid f = (Fluid) contents.get(contentType).get();
                equivalent = SoftFluidRegistry.fromForgeFluid(f);
                levelToBottleEquivalent = 3;
                //fix so won't create infinite soup
                //if(equivalent==SoftFluidRegistry.BEETROOT_SOUP||equivalent==SoftFluidRegistry.RABBIT_STEW||
                        //equivalent==SoftFluidRegistry.MUSHROOM_STEW||f.getRegistryName().getPath().equals("potato_soup")){}
            } else if (contentType == CauldronContentTypes.POTION) {
                Potion potion = (Potion) contents.get(contentType).get();
                equivalent = SoftFluidRegistry.POTION;
                nbt.putString("Potion", potion.getRegistryName().toString());
                levelToBottleEquivalent = 4;
            } else if (contentType == CauldronContentTypes.DYE) {
                DyeColor dye = (DyeColor) contents.get(contentType).get();
                equivalent = SoftFluidRegistry.get("inspirations:" + dye.getSerializedName() + "_dye");
                levelToBottleEquivalent = 4;
            } else return false;

            faucetFluidHolder.fill(equivalent, nbt);

            if (doTransfer) {
                int level = te.getLevel();
                if (level >= levelToBottleEquivalent && transferBelow.get()) {
                    te.updateStateAndBlock(contents, level - levelToBottleEquivalent);
                    te.setChanged();
                }
            }
            if (!doTransfer) return !faucetFluidHolder.isEmpty();
            return true;
        }
        return false;
    }

    public static boolean tryAddFluid(TileEntity cauldronTile, SoftFluidHolder faucetFluidHolder) {
        if (cauldronTile instanceof CauldronTileEntity) {
            CauldronTileEntity te = (CauldronTileEntity) cauldronTile;
            //TODO: finish
            /*
            int levelToBottleEquivalent = 3;
            SoftFluid s = faucetFluidHolder.getFluid();
            ResourceLocation name = s.getRegistryName();
            CauldronContentType<?> contentType = CauldronContentTypes.DYE;
            contentType.setValue();
            if(name.getNamespace().equals("inspirations") && name.getPath().contains("dyed")){
                levelToBottleEquivalent = 4;
            }
            int level = te.getLevel();
            if(level!=0) {
                ICauldronContents contents = te.getContents();
                CauldronContentType<?> contentType = contents.getType();

                SoftFluid equivalent;
                CompoundNBT nbt = new CompoundNBT();
                int levelToBottleEquivalent;
                if (contentType == CauldronContentTypes.FLUID) {
                    //ugly. might not work
                    equivalent = SoftFluidRegistry.fromForgeFluid((Fluid) contents.get(contentType).get());

                } else if (contentType == CauldronContentTypes.POTION) {
                    Potion potion = (Potion) contents.get(contentType).get();
                    equivalent = SoftFluidRegistry.POTION;
                    nbt.putString("Potion", potion.getRegistryName().toString());
                    levelToBottleEquivalent = 4;
                } else if (contentType == CauldronContentTypes.DYE) {
                    DyeColor dye = (DyeColor) contents.get(contentType).get();
                    equivalent = SoftFluidRegistry.get("inspirations:" + dye.getSerializedName() + "_dye");
                    levelToBottleEquivalent = 4;
                } else return false;

            }

            if (level + levelToBottleEquivalent <= 12 && (faucetFluidHolder.isSameFluidAs(equivalent, nbt)||level==0)) {
                te.updateStateAndBlock(contents, level + levelToBottleEquivalent);
                te.setChanged();
                return true;
            }

             */

        }
        return false;
    }
}