package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpiderSkullBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {

    @Inject(method = "createSkullRenderers",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/model/SkullModel;<init>(Lnet/minecraft/client/model/geom/ModelPart;)V",
                    ordinal = 4))
    private static void supp$addExtraSkulls(EntityModelSet entityModelSet, CallbackInfoReturnable<Map<SkullBlock.Type, SkullModelBase>> cir,
                                            @Local ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> builder){
        builder.put(EndermanSkullBlock.TYPE, new SkullModel(entityModelSet.bakeLayer(ModelLayers.SKELETON_SKULL)));
        builder.put(SpiderSkullBlock.TYPE, new SkullModel(entityModelSet.bakeLayer(ModelLayers.SKELETON_SKULL)));
    }

    @Inject(method = "method_3580(Ljava/util/HashMap;)V", at = @At("TAIL"))
    private static void supp$addExtraTextures(HashMap<SkullBlock.Type, ResourceLocation> hashMap, CallbackInfo ci){
        hashMap.put(EndermanSkullBlock.TYPE, Supplementaries.res("textures/entity/enderman_head.png"));
        hashMap.put(SpiderSkullBlock.TYPE, Supplementaries.res("textures/entity/spider_head.png"));
    }
}
