package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class MimicBlockColor implements IBlockColor {

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
    public int getColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tint) {
        if (world != null && pos != null) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof IBlockHolder) {
                BlockState mimic = ((IBlockHolder) te).getHeldBlock();
                if (mimic != null && !mimic.hasTileEntity()) {
                    if (sodiumColors != null){
                        try {
                            ((IBlockColor)sodiumColors.invoke(Minecraft.getInstance().getBlockColors(), mimic))
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

