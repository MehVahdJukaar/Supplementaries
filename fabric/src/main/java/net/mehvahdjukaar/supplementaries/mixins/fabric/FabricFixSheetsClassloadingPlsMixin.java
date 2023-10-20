package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.function.Consumer;

@Deprecated(forRemoval = true)
@Mixin(Sheets.class)
public class FabricFixSheetsClassloadingPlsMixin {

    @Inject(method = "getAllMaterials", at = @At("RETURN"))
    private static void whyDoIHaveToDoThis(Consumer<Material> materialConsumer, CallbackInfo ci) {

        if (!Sheets.BANNER_MATERIALS.keySet().equals(BuiltInRegistries.BANNER_PATTERN.registryKeySet()) ||
        !Sheets.DECORATED_POT_MATERIALS.keySet().equals(BuiltInRegistries.DECORATED_POT_PATTERNS.registryKeySet())) {
            throw  new IllegalStateException("Some mod loaded the Sheets class to early! This causes the banner and sherds texture maps to not contain modded patterns. " +
                    "Supplementaries wont allow the game to load further to prevent further issues.");

        }

    }
}
