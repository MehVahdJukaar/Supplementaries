package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LunchBoxBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.lang.ref.WeakReference;

public class SuppClientPlatformStuff {

    @ExpectPlatform
    public static RenderType staticNoise(ResourceLocation location) {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static ShaderInstance getNoiseShader() {
        throw new ArrayStoreException();
    }


    @ExpectPlatform
    public static ShaderInstance getEntityOffsetShader() {
        throw new ArrayStoreException();
    }

    @ExpectPlatform
    public static ISlider createSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue,
                                       double currentValue, double stepSize, int precision, boolean drawString) {
        throw new AssertionError();
    }

    //use c tag
    @Deprecated(forRemoval = true)
    @ExpectPlatform
    public static boolean isSlimeball(Item item) {
        throw new AssertionError();
    }
}
