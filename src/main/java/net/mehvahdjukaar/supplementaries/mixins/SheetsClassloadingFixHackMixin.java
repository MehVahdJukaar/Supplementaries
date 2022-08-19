package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(Sheets.class)
public abstract class SheetsClassloadingFixHackMixin {

    @Inject(method = "getAllMaterials", at = @At("HEAD"))
    private static void whyDoIHaveToDoThis(Consumer<Material> materialConsumer, CallbackInfo ci) {
        //TODO: remove when forge fixes

        boolean applied = false;
        if (!Sheets.BANNER_MATERIALS.keySet().equals(Arrays.stream(BannerPattern.values()).collect(Collectors.toSet()))) {
            var map = new HashMap<>(Sheets.BANNER_MATERIALS);
            for (var v : BannerPattern.values())
                map.put(v,
                        new Material(Sheets.BANNER_SHEET, v.location(true)));
            SheetsClassloadingFixHackAccessor.setBannerMaterials(map);
            applied = true;
        }

        if (!Sheets.SHIELD_MATERIALS.keySet().equals(Arrays.stream(BannerPattern.values()).collect(Collectors.toSet()))) {
            var map = new HashMap<>(Sheets.SHIELD_MATERIALS);
            for (var v : BannerPattern.values())
                map.put(v,
                        new Material(Sheets.SHIELD_SHEET, v.location(false)));
            SheetsClassloadingFixHackAccessor.setShieldMaterials(map);

            applied = true;
        }
        if (applied) {
            Supplementaries.LOGGER.error("Some mod loaded the Sheets class to early! This causes the banner texture maps to not contain modded patterns. Supplementaries will not attempt to fix...");
        }

    }
}
