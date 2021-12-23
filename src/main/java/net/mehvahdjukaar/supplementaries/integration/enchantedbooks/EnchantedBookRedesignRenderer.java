package net.mehvahdjukaar.supplementaries.integration.enchantedbooks;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EnchantedBookRedesignRenderer extends RenderStateShard {

    //private static final RenderType ENTITY_GLINT_TINTED = RenderType.create("entity_glint_tinted", DefaultVertexFormat.POSITION_COLOR_TEX, 7, 256, CompositeState.builder().setTextureState(new TextureStateShard(Hooks.TINTED_GLINT_RL, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));

    //just to access its fields
    public EnchantedBookRedesignRenderer(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }


    @Nullable
    public static VertexConsumer getColoredFoil(ItemStack stack, MultiBufferSource buffer) {

        //if (EnchantedBookRedesign.cache.contains(stack.getItem())) {
        //    return TintedVertexConsumer.withTint(buffer.getBuffer(ModRenderType.TINTED_ENTITY_GLINT_DIRECT), Hooks.getColor(stack));
        //}
        return null;
    }
}
