package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class MimicBlockColor implements BlockColor {

    private static final Method sodiumColors;

    static {
        //TODO: fix sodium thing
        Method m = null;
        try {
            m = ObfuscationReflectionHelper.findMethod(BlockColors.class, "getColorProvider", BlockState.class);
        } catch (Exception ignored) {}

        sodiumColors = m;
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
        if (world != null && pos != null) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof IBlockHolder) {
                BlockState mimic = ((IBlockHolder) te).getHeldBlock();
                if (mimic != null && !mimic.hasTileEntity()) {
                    if (sodiumColors != null){
                        try {
                            ((BlockColor)sodiumColors.invoke(Minecraft.getInstance().getBlockColors(), mimic))
                                    .getColor(mimic, world, pos, tint);
                        } catch (Exception ignored) {}
                    }
                    else{
                        return Minecraft.getInstance().getBlockColors().getColor(mimic, world, pos, tint);
                    }

                }
            }
        }
        return -1;
    }
}

