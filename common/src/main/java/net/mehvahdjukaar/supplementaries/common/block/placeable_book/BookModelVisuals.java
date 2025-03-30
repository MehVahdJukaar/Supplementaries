package net.mehvahdjukaar.supplementaries.common.block.placeable_book;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSVColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

import java.util.List;
import java.util.Objects;

//client
public record BookModelVisuals(ModelResourceLocation model, HSVColor color, float hueShift, boolean hasGlint) {

    public BookModelVisuals(ModelResourceLocation res, int color, float hueShift, boolean hasGlint) {
        this(res, new RGBColor(color).asHSV(), hueShift, hasGlint);
    }

    public BookModelVisuals(ModelResourceLocation res, DyeColor color, float hueShift, boolean hasGlint) {
        this(res, color.getFireworkColor(), hueShift, hasGlint);
    }

    public static final Codec<Either<Integer, DyeColor>> COLOR_CODEC = Codec.either(Codec.INT, DyeColor.CODEC);

    public static final Codec<BookModelVisuals> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.xmap(RenderUtil::getStandaloneModelLocation, ModelResourceLocation::id)
                    .fieldOf("model").forGetter(BookModelVisuals::model),
            COLOR_CODEC.optionalFieldOf("color", Either.left(-1)).forGetter(b -> Either.left(b.color.asRGB().toInt())),
            Codec.FLOAT.optionalFieldOf("hue_shift", 1f).forGetter(b -> b.hueShift),
            Codec.BOOL.optionalFieldOf("has_glint", false).forGetter(BookModelVisuals::hasGlint)
    ).apply(instance,
            (modelResourceLocation, color, aFloat, aBoolean) -> color.map(
                    integer -> new BookModelVisuals(modelResourceLocation, integer, aFloat, aBoolean),
                    dyeColor -> new BookModelVisuals(modelResourceLocation, dyeColor, aFloat, aBoolean)
            )));

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


    public boolean looksGoodNextTo(BookModelVisuals other) {
        float diff = Math.abs(Mth.degreesDifference(this.color.hue() * 360, other.color.hue() * 360) / 360);
        return diff < (other.hueShift + this.hueShift) / 2f;
    }


    public record VariantModelList(DataComponentMap itemComponents, List<BookModelVisuals> models) {
        private static final Codec<VariantModelList> CODEC = RecordCodecBuilder.<VariantModelList>create(instance -> instance.group(
                        DataComponentMap.CODEC.optionalFieldOf("item_components", DataComponentMap.EMPTY).forGetter(VariantModelList::itemComponents),
                        BookModelVisuals.CODEC.listOf().fieldOf("models").forGetter(VariantModelList::models)
                ).apply(instance, VariantModelList::new))
                .validate(v -> {
                    if (v.models.isEmpty()) {
                        return DataResult.error(() -> "VariantModelList must have at least one model");
                    }
                    return DataResult.success(v);
                });

        public static final Codec<List<VariantModelList>> LIST_CODEC = VariantModelList.CODEC.listOf().fieldOf("variants").codec();

        public boolean matchesComponents(DataComponentMap other) {
            //check if all the keys in our component maps are present and same in the other
            for (TypedDataComponent<?> entry : this.itemComponents) {
                var type = entry.type();
                var otherValue = other.get(type);
                var myValue = entry.value();
                if (!Objects.equals(myValue, otherValue)) {
                    return false;
                }
            }
            return true;
        }
    }
}
