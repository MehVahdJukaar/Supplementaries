package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidColor implements BlockColor, ItemColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tint) {
        if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof ISoftFluidTankProvider bh) {
                if (tint == 1) {
                    var tank = bh.getSoftFluidTank();
                    return tank.getTintColor(level, pos);
                }
            }
        }
        return -1;
    }

    @Override
    public int getColor(ItemStack itemStack, int i) {
        CompoundTag fluidHolder = itemStack.getTagElement("FluidHolder");
        if (fluidHolder != null) {
            return fluidHolder.getInt("CachedColor");
        }
        return 0;
    }
}

