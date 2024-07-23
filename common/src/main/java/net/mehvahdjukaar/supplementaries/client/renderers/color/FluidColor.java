package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record FluidColor(boolean flowing) implements BlockColor, ItemColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tint) {
        if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof ISoftFluidTankProvider bh) {
                if (tint == 1) {
                    var tank = bh.getSoftFluidTank();
                    return flowing ? tank.getCachedFlowingColor(level, pos) : tank.getCachedStillColor(level, pos);
                }
            }
        }
        return -1;
    }

    @Override
    public int getColor(ItemStack itemStack, int i) {
        CompoundTag fluidHolder = itemStack.getTagElement("FluidHolder");
        Level level = Minecraft.getInstance().level;
        if (fluidHolder != null && level != null) {
            SoftFluidStack stack = SoftFluidStack.load(fluidHolder);
            return flowing ? stack.getFlowingColor(level, null) : stack.getStillColor(level, null);
        }
        return 0;
    }
}

