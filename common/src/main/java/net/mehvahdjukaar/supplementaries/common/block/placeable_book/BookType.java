package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public record BookType(String texture, float hue, float hueShift, boolean hasGlint,
                       float enchantPower, boolean isVertical,
                       ItemPredicate predicate) {

    public static final Codec<BookType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("texture").forGetter(BookType::texture),
            Codec.FLOAT.fieldOf("color").forGetter(BookType::hue),
            Codec.FLOAT.fieldOf("angle").forGetter(BookType::hueShift),
            Codec.BOOL.optionalFieldOf("hasGlint", false).forGetter(BookType::hasGlint),
            Codec.FLOAT.optionalFieldOf("enchantPower", 0f).forGetter(BookType::enchantPower),
            Codec.BOOL.optionalFieldOf("isVertical", false).forGetter(BookType::isVertical),
            ItemPredicate.CODEC.fieldOf("predicate").forGetter(BookType::predicate)
    ).apply(instance, BookType::new));

    /*
    public BookType create(String texture, int rgb, float angle, boolean hasGlint, ItemPredicate predicate) {
        var col = new RGBColor(rgb).asHSV();
        float hueShift;
        if (angle < 0) hueShift = getLegacyAllowedHueShift(col.asHSL());
        else hueShift = Math.max(1, angle);

        return new BookType(texture, col.hue(), hueShift, hasGlint, predicate);
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
    //I think it allows darker non-saturated colors to have higher hue shift
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
        float diff = Math.abs(Mth.degreesDifference(this.hue * 360, other.hue * 360) / 360);
        return diff < (other.hueShift + this.hueShift) / 2f;
    }

}
