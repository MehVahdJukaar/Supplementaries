package net.mehvahdjukaar.supplementaries.integration;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

public class EnchantRedesignCompat {

    @ExpectPlatform
    public static VertexConsumer getBookColoredFoil(ItemStack stack, MultiBufferSource buffer) {
        throw new AssertionError();
    }
}
