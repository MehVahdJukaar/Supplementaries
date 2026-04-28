package net.mehvahdjukaar.supplementaries.integration;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

public class EnchantRedesignCompat {

    @PlatformImpl
    public static VertexConsumer getBookColoredFoil(ItemStack stack, MultiBufferSource buffer) {
        throw new AssertionError();
    }
}
