package net.mehvahdjukaar.supplementaries.client;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LeatherPatternTexturesRegistry {

    /*
    private static final Map<String, ResourceLocation> MAP = new HashMap<>() {{
        Arrays.stream(BannerPattern.values()).filter(b -> b.hasPatternItem).forEach(p -> {
            String name = p.getFilename();
            put(name, Supplementaries.res("textures/entity/leather_patterns/" + name.replace(":", "/") + ".png"));
        });
    }};



    public static ResourceLocation getTexture(String patternName) {
        return MAP.get(patternName);
    }

    public static ResourceLocation getTexture(BannerPattern pattern) {
        return getTexture(pattern.getFilename());
    }

    @Nullable
    public static ResourceLocation getTexture(ItemStack stack) {
        CompoundTag compoundtag = stack.getTagElement("display");
        if (compoundtag != null) {
            return getTexture(compoundtag.getString("pattern"));
        }
        return null;//getTexture(BannerPattern.SKULL);
    }
    */


}
