package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.common.effects.StasisEnchantment;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.EnchantRedesignCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BookPileBlockTile extends ItemDisplayTile {

    public final boolean horizontal;
    private float enchantPower = 0;

    //client only
    public final List<VisualBook> books = new ArrayList<>();

    public BookPileBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, false);
    }

    public BookPileBlockTile(BlockPos pos, BlockState state, boolean horizontal) {
        super(ModRegistry.BOOK_PILE_TILE.get(), pos, state, 4);
        this.horizontal = horizontal;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putFloat("EnchantPower", this.enchantPower);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.enchantPower = compound.getFloat("EnchantPower");
    }

    @Override
    public void updateTileOnInventoryChanged() {
        int b = (int) this.getItems().stream().filter(i -> !i.isEmpty()).count();
        if (b != this.getBlockState().getValue(BookPileBlock.BOOKS)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BookPileBlock.BOOKS, b), 2);
        }
        this.enchantPower = 0;
        for (int i = 0; i < 4; i++) {
            Item item = this.getItem(i).getItem();
            if (BookPileBlock.isNormalBook(item)) this.enchantPower += CommonConfigs.Tweaks.BOOK_POWER.get() / 4f;
            else if (BookPileBlock.isQuarkTome(item))
                this.enchantPower += (CommonConfigs.Tweaks.BOOK_POWER.get() / 4f) * 2;
            else if (BookPileBlock.isEnchantedBook(item))
                this.enchantPower += CommonConfigs.Tweaks.ENCHANTED_BOOK_POWER.get() / 4f;
        }
    }

    @Override
    public void updateClientVisualsOnLoad() {
        this.books.clear();
        List<BookColor> colors = new ArrayList<>(Arrays.asList(VALID_RANDOM_COLORS));
        for (int i = 0; i < 4; i++) {
            ItemStack stack = this.getItem(i);
            if (stack.isEmpty()) break;
            BookColor last = i == 0 ? null : this.books.get(i - 1).color;
            this.books.add(i, new VisualBook(stack, this.worldPosition, i, colors, last));
        }
    }

    public float getEnchantPower() {
        return enchantPower;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.supplementaries.book_pile");
    }

    //only client

    public static class VisualBook {
        private final float angle;
        private final @Nullable
        BookColor color;
        private final Material material;
        private final ItemStack stack;
        private final boolean isEnchanted;

        public VisualBook(ItemStack stack, BlockPos pos, int index, List<BookColor> colors, @Nullable BookColor lastColor) {
            this.stack = stack;
            Random rand = new Random(pos.asLong());
            for (int j = 0; j < index; j++) rand.nextInt();
            Item item = stack.getItem();
            this.angle = (float) (rand.nextInt(32) * Math.PI / 16);


            if (item instanceof BookItem) {
                if (lastColor == null) {
                    this.color = colors.get(rand.nextInt(colors.size()));
                } else {
                    List<BookColor> c = colors.stream().filter(b -> b.looksGoodNextTo(lastColor)).collect(Collectors.toList());
                    this.color = c.get(rand.nextInt(c.size()));
                }
                colors.remove(this.color);

                this.material = ModMaterials.BOOK_MATERIALS.get(this.color);
                this.isEnchanted = false;
            } else if (Utils.getID(item).getNamespace().equals("inspirations")) {
                String colName = Utils.getID(item).getPath().replace("_book", "");
                this.color = BookColor.byName(colName);

                this.material = ModMaterials.BOOK_MATERIALS.get(this.color);
                this.isEnchanted = false;
            } else if (BookPileBlock.isWrittenBook(item)) {
                this.color = null;
                this.material = item instanceof WritableBookItem ? ModMaterials.BOOK_AND_QUILL_MATERIAL : ModMaterials.BOOK_WRITTEN_MATERIAL;

                this.isEnchanted = false;
            } else {
                this.color = null;

                this.material = BookPileBlock.isQuarkTome(item) ? ModMaterials.BOOK_TOME_MATERIAL : ModMaterials.BOOK_ENCHANTED_MATERIAL;
                this.isEnchanted = true;
            }
        }

        @SuppressWarnings("ConstantConditions")
        public VertexConsumer getBuilder(MultiBufferSource buffer) {
            if (this.isEnchanted && ClientConfigs.Tweaks.BOOK_GLINT.get()) {
                VertexConsumer foilBuilder = null;
                if (CompatHandler.enchantedbookredesign) {
                    foilBuilder = EnchantRedesignCompat.getBookColoredFoil(this.stack, buffer);
                }
                if (foilBuilder == null) {
                    foilBuilder = buffer.getBuffer(RenderType.entityGlint());
                }
                return VertexMultiConsumer.create(foilBuilder, this.material.buffer(buffer, RenderType::entitySolid));
            }
            return material.buffer(buffer, RenderType::entitySolid);
        }

        public float getAngle() {
            return angle;
        }

        public boolean isEnchanted() {
            return isEnchanted;
        }
    }

    private static final BookColor[] VALID_RANDOM_COLORS = {BookColor.BROWN, BookColor.ORANGE, BookColor.YELLOW, BookColor.RED,
            BookColor.DARK_GREEN, BookColor.LIME, BookColor.TEAL, BookColor.BLUE, BookColor.PURPLE};

    public enum BookColor {
        BROWN(DyeColor.BROWN, 1),
        WHITE(DyeColor.WHITE, 1),
        BLACK(DyeColor.BLACK, 1),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        GRAY(DyeColor.GRAY),
        ORANGE(DyeColor.ORANGE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        DARK_GREEN("green", 0x2fc137),
        TEAL("cyan", 0x16ecbf),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        BLUE(DyeColor.BLUE),
        PURPLE(DyeColor.PURPLE),
        MAGENTA(DyeColor.MAGENTA),
        PINK(DyeColor.PINK),
        RED(DyeColor.RED);

        private final String name;
        private final float hue;
        private final float angle;

        BookColor(String s, int rgb, int angle) {
            this.name = s;
            var col = new RGBColor(rgb).asHSL();
            this.hue = col.hue();
            if (angle < 0) this.angle = getAllowedHueShift(col);
            else this.angle = Math.max(1, angle);
        }

        BookColor(DyeColor color, int angle) {
            this(color.getName(), ColorHelper.pack(color.getTextureDiffuseColors()), angle);
        }

        BookColor(String name, int color) {
            this(name, color, -1);
        }

        BookColor(DyeColor color) {
            this(color.getName(), ColorHelper.pack(color.getTextureDiffuseColors()), -1);
        }

        public static BookColor byName(String name) {
            for (BookColor c : values()) {
                if (c.name.equals(name)) {
                    return c;
                }
            }
            return BROWN;
        }

        public boolean looksGoodNextTo(BookColor other) {
            float diff = Math.abs(Mth.degreesDifference(this.hue * 360, other.hue * 360) / 360);
            return diff < (other.angle + this.angle) / 2f;
        }

        //could even just use distance
        private float getAllowedHueShift(HSLColor color) {
            float l = color.lightness();
            float s = ColorHelper.oneToOneSaturation(color.saturation(), l);
            float minAngle = 90 / 360f;
            float addAngle = 65 / 360f;
            float distLightSq = 2;//(s * s) + (1 - l) * (1 - l);
            float distDarkSq = ((s * s) + (l * l));
            float distSq = Math.min(1, Math.min(distDarkSq, distLightSq));
            return minAngle + (1 - distSq) * addAngle;
        }

        public static BookColor rand(Random r) {
            return values()[r.nextInt(values().length)];
        }

        public String getName() {
            return name;
        }
    }
}
