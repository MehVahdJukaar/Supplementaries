package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public final class BookType {

    public static final Codec<BookType> CODEC = Codec.stringResolver(BookType::name, PlaceableBookManager::getByName);

    private final String name;
    private final float hue;
    private final float hueShift;
    private final boolean hasGlint;
    private final ResourceLocation modelPath;

    public BookType(String name, int rgb, float angle, boolean hasGlint) {
        var col = new RGBColor(rgb).asHSV();
        float hueShift;
        if (angle < 0) hueShift = getLegacyAllowedHueShift(col.asHSL());
        else hueShift = Math.max(1, angle);
        this.name = name;
        this.hue = col.hue();
        this.hueShift = hueShift;
        this.hasGlint = hasGlint;
        this.modelPath = Supplementaries.res("block/books/book_" + name);
    }

    //this could be redone
    //I think it allows darker non-saturated colors to have higher hue shift
    private static float getAllowedHueShift(HSVColor color) {
        float v = color.value();
        float minAngle = 70 / 360f;
        float addAngle = 65 / 360f;
        return minAngle + addAngle * (1 - v);
    }

    // if it ain't broke, don't fix it?
    private  static float getLegacyAllowedHueShift(HSLColor color) {
        float l = color.lightness();
        float s = ColorHelper.normalizeHSLSaturation(color.saturation(), l);
        float minAngle = 90 / 360f;
        float addAngle = 65 / 360f;
        float distLightSq = 2;//(s * s) + (1 - l) * (1 - l);
        float distDarkSq = ((s * s) + (l * l));
        float distSq = Math.min(1, Math.min(distDarkSq, distLightSq));
        return minAngle + (1 - distSq) * addAngle;
    }

    public BookType(DyeColor color, float angle, boolean enchanted) {
        this(color.getName(), color.getTextureDiffuseColor(), angle, enchanted);
    }

    public BookType(DyeColor color) {
        this(color, -1, false);
    }

    public BookType(String name, int rgb, boolean enchanted) {
        this(name, rgb, -1, enchanted);
    }

    public boolean looksGoodNextTo(BookType other) {
        float diff = Math.abs(Mth.degreesDifference(this.hue * 360, other.hue * 360) / 360);
        return diff < (other.hueShift + this.hueShift) / 2f;
    }

    public String name() {
        return name;
    }

    public float hue() {
        return hue;
    }

    public float hueShift() {
        return hueShift;
    }

    public boolean hasGlint() {
        return hasGlint;
    }

    public ResourceLocation modelPath() {
        return modelPath;
    }
}
