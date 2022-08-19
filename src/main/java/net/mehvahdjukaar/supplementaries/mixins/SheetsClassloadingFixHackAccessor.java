package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Sheets.class)
public interface SheetsClassloadingFixHackAccessor {

    @Mutable
    @Accessor("BANNER_MATERIALS")
    public static void setBannerMaterials(Map<BannerPattern, Material> map) {
    }

    @Mutable
    @Accessor("SHIELD_MATERIALS")
    public static void setShieldMaterials(Map<BannerPattern, Material> map) {
    }

}
