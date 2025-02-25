package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Optional;

public record BookType(ResourceLocation textureId, HSVColor color, float hueShift, boolean hasGlint,
                       float enchantPower, boolean isHorizontal, float chance,
                       ItemPredicate predicate) {

    public static final Codec<BookType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(BookType::textureId),
                    ColorUtils.CODEC.xmap(c -> new RGBColor(c).asHSV(), h -> h.asRGB().toInt())
                            .fieldOf("color").forGetter(BookType::color),
                    Codec.FLOAT.optionalFieldOf("hue_angle").forGetter(b -> Optional.of(b.hueShift)),
                    Codec.BOOL.optionalFieldOf("has_glint", false).forGetter(BookType::hasGlint),
                    Codec.FLOAT.optionalFieldOf("enchant_power", 0f).forGetter(BookType::enchantPower),
                    Codec.BOOL.optionalFieldOf("is_horizontal", false).forGetter(BookType::isHorizontal),
                    Codec.FLOAT.optionalFieldOf("chance", 1f).forGetter(BookType::chance),
                    ItemPredicate.CODEC.fieldOf("predicate").forGetter(BookType::predicate)
            ).apply(instance, (text, color, hueAngle, hasGlint, enchPower, isVertical, chance, itemPredicate) -> {
                float hueShift = hueAngle.orElseGet(() -> getAllowedHueShift(color));
                return new BookType(text, color, hueShift, hasGlint, enchPower, isVertical, chance, itemPredicate);

            })
    );


    /*
    public BookType create(String texture, int rgb, float angle, boolean hasGlint, ItemPredicate predicate) {
        var col = new RGBColor(rgb).asHSV();
        float hueShift;
        if (angle < 0) hueShift = getLegacyAllowedHueShift(col.asHSL());
        else hueShift = Math.max(1, angle);

        return new BookType(texture, col.color(), hueShift, hasGlint, predicate);
    }

    public BookType(DyeColor color, float angle, boolean enchanted) {
        this(color.getName(), color.getTextureDiffuseColor(), angle, enchanted, null);
    }

    public BookType(DyeColor color) {
        this(color, -1, false);
    }

    public BookType(String name, int rgb, boolean enchanted) {
        this(name, rgb, -1, enchanted, null);
    }
*/

    //this could be redone
//I think it allows darker non-saturated colors to have higher color shift
    private static float getAllowedHueShift(HSVColor color) {
        float v = color.value();
        float minAngle = 70 / 360f;
        float addAngle = 65 / 360f;
        return minAngle + addAngle * (1 - v);
    }

    // if it ain't broke, don't fix it?
    private static float getLegacyAllowedHueShift(HSLColor color) {
        float l = color.lightness();
        float s = ColorHelper.normalizeHSLSaturation(color.saturation(), l);
        float minAngle = 90 / 360f;
        float addAngle = 65 / 360f;
        float distLightSq = 2;//(s * s) + (1 - l) * (1 - l);
        float distDarkSq = ((s * s) + (l * l));
        float distSq = Math.min(1, Math.min(distDarkSq, distLightSq));
        return minAngle + (1 - distSq) * addAngle;
    }

    public boolean looksGoodNextTo(BookType other) {
        float diff = Math.abs(Mth.degreesDifference(this.color.hue() * 360, other.color.hue() * 360) / 360);
        return diff < (other.hueShift + this.hueShift) / 2f;
    }

}
