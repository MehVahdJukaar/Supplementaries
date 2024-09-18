package net.mehvahdjukaar.supplementaries.integration.neoforge;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import tfar.enchantedbookredesign.EnchantedBookRedesign;
import tfar.enchantedbookredesign.Hooks;
import tfar.enchantedbookredesign.ModRenderType;
import tfar.enchantedbookredesign.TintedVertexConsumer;

public class EnchantRedesignCompatImpl {

    public static VertexConsumer getBookColoredFoil(ItemStack stack, MultiBufferSource buffer) {
        if (EnchantedBookRedesign.cache.contains(stack.getItem())) {
            return TintedVertexConsumer.withTint(buffer.getBuffer(ModRenderType.TINTED_GLINT_DIRECT), Hooks.getColor(stack));
        }
        return null;
    }
}
