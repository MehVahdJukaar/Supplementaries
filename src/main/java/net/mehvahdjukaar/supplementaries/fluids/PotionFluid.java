package net.mehvahdjukaar.supplementaries.fluids;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

public class PotionFluid extends ForgeFlowingFluid {
    public PotionFluid(Properties properties) {
        super(properties);
    }
/*
    public static FluidStack withEffects(int amount, Potion potion, List<EffectInstance> customEffects) {
        FluidStack fluidStack = new FluidStack(((PotionFluid) AllFluids.POTION.get()).getStillFluid(), amount);
        addPotionToFluidStack(fluidStack, potion);
        appendEffects(fluidStack, customEffects);
        return fluidStack;
    }*/

    public static FluidStack addPotionToFluidStack(FluidStack fs, Potion potion) {
        ResourceLocation resourcelocation = ForgeRegistries.POTION_TYPES.getKey(potion);
        if (potion == Potions.EMPTY) {
            fs.removeChildTag("Potion");
        } else {
            fs.getOrCreateTag().putString("Potion", resourcelocation.toString());
        }
        return fs;
    }

    public static FluidStack appendEffects(FluidStack fs, Collection<EffectInstance> customEffects) {
        if (!customEffects.isEmpty()) {
            CompoundNBT compoundnbt = fs.getOrCreateTag();
            ListNBT listnbt = compoundnbt.getList("CustomPotionEffects", 9);

            for (EffectInstance effectinstance : customEffects) {
                listnbt.add(effectinstance.write(new CompoundNBT()));
            }

            compoundnbt.put("CustomPotionEffects", listnbt);
        }
        return fs;
    }

    @Override
    public boolean isSource(FluidState state) {
        return false;
    }

    @Override
    public int getLevel(FluidState state) {
        return 0;
    }

    public static class PotionFluidAttributes extends FluidAttributes {
        public PotionFluidAttributes(Builder builder, Fluid fluid) {
            super(builder, fluid);
        }

        public int getColor(FluidStack stack) {
            CompoundNBT tag = stack.getOrCreateTag();
            return PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(tag)) | -16777216;
        }
    }

    public static enum BottleType {
        REGULAR,
        SPLASH,
        LINGERING;

        private BottleType() {}
    }
}
