package net.mehvahdjukaar.supplementaries.common.items.components;

import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfettiColors implements TooltipProvider {
    protected static final HashBiMap<DyeColor, Integer> COLOR_TO_DIFFUSE = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(Function.identity(), ConfettiColors::dyeToRGB,
                    (color, color2) -> color2, HashBiMap::create));

    private static int dyeToRGB(DyeColor dyeColor) {
        return ColorHelper.prettyfyColor(new RGBColor(dyeColor.getTextColor()).asHSL())
                .asRGB().toInt();
    }

    public static final Codec<ConfettiColors> CODEC = Codec.INT.listOf().xmap(
            list -> new ConfettiColors(list.stream().mapToInt(i -> i).toArray()),
            colors -> colors.colors
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfettiColors> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
                    v -> v.colors,
                    ConfettiColors::new
            );

    public static final ConfettiColors EMPTY = new ConfettiColors();

    public static ConfettiColors of(int... colors) {
        return new ConfettiColors(colors);
    }

    public static ConfettiColors of(DyeColor... colors) {
        IntList list = IntList.of();
        for (DyeColor c : colors) {
            list.add(dyeToRGB(c));
        }
        return new ConfettiColors(list.toIntArray());
    }

    @Nullable
    public static Integer getRgbColor(ItemStack stack) {
        if (CompatObjects.DYE_BOTTLE.is(stack.getItem())) {
            DyedItemColor colorComp = stack.get(DataComponents.DYED_COLOR);
            if (colorComp != null) {
                return colorComp.rgb();
            }
        }
        var c = ForgeHelper.getColor(stack);
        if (c != null) {
            return dyeToRGB(c);
        }
        return null;
    }

    private final IntList colors;

    ConfettiColors(List<Integer> colors) {
        this(colors.stream().mapToInt(i -> i).toArray());
    }

    ConfettiColors(int... colors) {
        this.colors = IntList.of(colors);
    }

    public Collection<Integer> getColors() {
        return colors;
    }

    public ConfettiColors withAddedColor(int color) {
        IntList newColors = new IntArrayList();
        newColors.addAll(colors);
        newColors.add(color);
        return new ConfettiColors(newColors.toIntArray());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ConfettiColors that)) return false;
        return Objects.equals(colors, that.colors);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(colors);
    }

    public int size() {
        return colors.size();
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        for (int colorInt : colors) {
            DyeColor dye = COLOR_TO_DIFFUSE.inverse().get(colorInt);
            if (dye != null) {
                tooltipAdder.accept(Component.translatable("color.minecraft." + dye.getName()).withStyle(ChatFormatting.GRAY));
            } else {
                if (tooltipFlag.isAdvanced()) {
                    tooltipAdder.accept(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", colorInt)).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltipAdder.accept(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
            }
        }
    }
}
