package net.mehvahdjukaar.supplementaries.compat.enchantedbooks;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.State;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tfar.enchantedbookredesign.EnchantedBookRedesign;
import tfar.enchantedbookredesign.Hooks;
import tfar.enchantedbookredesign.ModRenderType;
import tfar.enchantedbookredesign.TintedVertexConsumer;


public class EnchantedBookRedesignRenderer extends RenderState{

    private static final RenderType ENTITY_GLINT_TINTED = RenderType.create("entity_glint_tinted", DefaultVertexFormats.POSITION_COLOR_TEX, 7, 256, State.builder().setTextureState(new TextureState(Hooks.TINTED_GLINT_RL, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));

    //just to access its fields
    public EnchantedBookRedesignRenderer(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }


    @Nullable
    public static IVertexBuilder getColoredFoil(ItemStack stack, IRenderTypeBuffer buffer){
        if (EnchantedBookRedesign.cache.contains(stack.getItem())) {
            return TintedVertexConsumer.withTint(buffer.getBuffer(ModRenderType.TINTED_ENTITY_GLINT_DIRECT), Hooks.getColor(stack));
        }
        return null;
    }
}
