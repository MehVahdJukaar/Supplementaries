package net.mehvahdjukaar.supplementaries.common.block;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.client.BlackboardManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.DecoBlocksCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class ModBlockProperties {

    //BlockState properties
    public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");
    public static final BooleanProperty KNOT = BooleanProperty.create("knot");
    public static final BooleanProperty TIPPED = BooleanProperty.create("tipped");
    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");
    public static final BooleanProperty AXIS_Y = BooleanProperty.create("axis_y");
    public static final BooleanProperty AXIS_X = BooleanProperty.create("axis_x");
    public static final BooleanProperty AXIS_Z = BooleanProperty.create("axis_z");
    public static final BooleanProperty FLOWER_BOX_ATTACHMENT = BooleanProperty.create("floor");
    public static final BooleanProperty LAVALOGGED = BooleanProperty.create("lavalogged");
    public static final BooleanProperty ANTIQUE = BooleanProperty.create("ye_olde");
    public static final BooleanProperty TREASURE = BooleanProperty.create("treasure");
    public static final BooleanProperty PACKED = BooleanProperty.create("packed");
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");
    public static final BooleanProperty WATCHED = BooleanProperty.create("watched");
    public static final BooleanProperty CULLED = BooleanProperty.create("culled");
    public static final BooleanProperty HAS_BLOCK = BooleanProperty.create("has_block");
    public static final BooleanProperty ROTATING = BooleanProperty.create("rotating");
    public static final BooleanProperty ON_PRESSURE_PLATE = BooleanProperty.create("on_pressure_plate");
    public static final BooleanProperty TWO_FACED = BooleanProperty.create("two_faced");
    public static final BooleanProperty SLANTED = BooleanProperty.create("slanted");

    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
    public static final IntegerProperty LIGHT_LEVEL_0_15 = IntegerProperty.create("light_level", 0, 15);
    public static final IntegerProperty LIGHT_LEVEL_5_15 = IntegerProperty.create("light_level", 5, 15);
    public static final IntegerProperty LIGHT_LEVEL_0_7 = IntegerProperty.create("light_level", 0, 7);
    public static final IntegerProperty WIND_STRENGTH = IntegerProperty.create("wind_strength", 0, 3);
    public static final IntegerProperty PANCAKES_1_8 = IntegerProperty.create("pancakes", 1, 8);
    public static final IntegerProperty ROTATION_4 = IntegerProperty.create("rotation", 0, 4);
    public static final IntegerProperty BURNING = IntegerProperty.create("burning", 0, 8);
    public static final IntegerProperty BOOKS = IntegerProperty.create("books", 1, 4);
    public static final IntegerProperty FINITE_FLUID_LEVEL = IntegerProperty.create("level", 1, 16);
    public static final IntegerProperty BALLS = IntegerProperty.create("balls", 1, 4);

    public static final EnumProperty<Topping> TOPPING = EnumProperty.create("topping", Topping.class);
    public static final EnumProperty<Winding> WINDING = EnumProperty.create("winding", Winding.class);
    public static final EnumProperty<PostType> POST_TYPE = EnumProperty.create("type", PostType.class);
    public static final EnumProperty<RakeDirection> RAKE_DIRECTION = EnumProperty.create("shape", RakeDirection.class);
    public static final EnumProperty<DisplayStatus> ITEM_STATUS = EnumProperty.create("item_status", DisplayStatus.class);
    public static final EnumProperty<Rune> RUNE = EnumProperty.create("rune", Rune.class);
    public static final EnumProperty<Bunting> NORTH_BUNTING = EnumProperty.create("north", Bunting.class);
    public static final EnumProperty<Bunting> SOUTH_BUNTING = EnumProperty.create("south", Bunting.class);
    public static final EnumProperty<Bunting> WEST_BUNTING = EnumProperty.create("west", Bunting.class);
    public static final EnumProperty<Bunting> EAST_BUNTING = EnumProperty.create("east", Bunting.class);

    //model properties
    public static final ModelDataKey<BlockState> MIMIC = MimicBlockTile.MIMIC_KEY;
    public static final ModelDataKey<Boolean> FANCY = new ModelDataKey<>(Boolean.class);
    public static final ModelDataKey<Boolean> FRAMED = new ModelDataKey<>(Boolean.class);
    public static final ModelDataKey<Float> RENDER_OFFSET = new ModelDataKey<>(Float.class);
    public static final ModelDataKey<SignPostBlockTile.Sign> SIGN_UP = new ModelDataKey<>(SignPostBlockTile.Sign.class);
    public static final ModelDataKey<SignPostBlockTile.Sign> SIGN_DOWN = new ModelDataKey<>(SignPostBlockTile.Sign.class);
    public static final ModelDataKey<BlockState> FLOWER_0 = new ModelDataKey<>(BlockState.class);
    public static final ModelDataKey<BlockState> FLOWER_1 = new ModelDataKey<>(BlockState.class);
    public static final ModelDataKey<BlockState> FLOWER_2 = new ModelDataKey<>(BlockState.class);
    public static final ModelDataKey<ResourceKey<SoftFluid>> FLUID = (ModelDataKey<ResourceKey<SoftFluid>>) new ModelDataKey(ResourceKey.class);

    public static final ModelDataKey<Integer> FLUID_COLOR = new ModelDataKey<>(Integer.class);
    public static final ModelDataKey<Float> FILL_LEVEL = new ModelDataKey<>(Float.class);
    public static final ModelDataKey<BlackboardManager.Key> BLACKBOARD = new ModelDataKey<>(BlackboardManager.Key.class);
    public static final ModelDataKey<BookPileBlockTile.BooksList> BOOKS_KEY = new ModelDataKey<>(BookPileBlockTile.BooksList.class);


    public enum PostType implements StringRepresentable {
        POST("post", 4),
        PALISADE("palisade", 6),
        WALL("wall", 8),
        BEAM("beam", 10);

        private final String name;
        private final int width;
        private final float offset;

        PostType(String name, int width) {
            this.name = name;
            this.width = width;
            this.offset = (8 - width / 2f) / 16f;
        }

        public int getWidth() {
            return width;
        }

        public float getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static PostType get(BlockState state) {
            return get(state, false);
        }

        @Nullable
        public static PostType get(BlockState state, boolean needsFullHeight) {

            PostType type = null;
            //if (state.getBlock().hasTileEntity(state)) return type;
            if (state.is(ModTags.POSTS)) {
                if (!state.hasProperty(BlockStateProperties.AXIS) || state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y) {
                    type = PostType.POST;
                }
            } else if (state.is(ModTags.PALISADES) || (CompatHandler.DECO_BLOCKS && DecoBlocksCompat.isPalisade(state))) {
                type = PostType.PALISADE;
            } else if (state.is(ModTags.WALLS)) {
                if ((state.getBlock() instanceof WallBlock) && !state.getValue(WallBlock.UP)) {
                    //ignoring not full height ones. might use hitbox here instead
                    if (needsFullHeight && (state.getValue(WallBlock.NORTH_WALL) == WallSide.LOW ||
                            state.getValue(WallBlock.WEST_WALL) == WallSide.LOW)) return null;
                    type = PostType.PALISADE;
                } else {
                    type = PostType.WALL;
                }
            } else if (state.is(ModTags.BEAMS)) {
                if (state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED)) {
                    //idk why this was here
                    type = null;
                } else {
                    type = PostType.BEAM;
                }
            }

            return type;
        }

    }

    public enum Topping implements StringRepresentable {
        NONE("none"),
        HONEY("honey"),
        SYRUP("syrup"),
        CHOCOLATE("chocolate"),
        JAM("jam");

        private final String name;

        Topping(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Pair<Topping, Item> fromFluidItem(Item item) {
            var holder = SoftFluidStack.fromItem(item.getDefaultInstance());
            if (holder == null) return null;
            SoftFluid s = holder.getFirst().fluid();
            var cat = holder.getSecond();
            if (cat.isEmpty() || cat.getAmount() != 1) return null;
            Topping t = fromFluid(s);
            if (t != NONE) {
                return Pair.of(t, cat.getEmptyContainer());
            }
            return Pair.of(NONE, null);
        }

        public static Topping fromFluid(SoftFluid s) {
            if (s.isEmptyFluid()) return NONE;
            if (s == BuiltInSoftFluids.HONEY.value()) {
                return HONEY;
            }
            String name = Utils.getID(s).getPath();
            if (name.contains("jam")) {
                return JAM;
            }
            if (name.equals("chocolate")) {
                return CHOCOLATE;
            }
            if (name.equals("syrup")) {
                return SYRUP;
            }
            return NONE;
        }

        //topping and empty item
        public static Pair<Topping, Item> fromItem(Item item) {
            var ff = fromFluidItem(item);
            if (ff.getFirst() != NONE) return ff;
            var holder = item.builtInRegistryHolder();
            Topping t;
            if (item == Items.SWEET_BERRIES) t = JAM;
            else if (holder.is(ModTags.SYRUP)) t = SYRUP;

            else if (item instanceof HoneyBottleItem) t = HONEY;
            else if (item == Items.COCOA_BEANS && (BuiltInRegistries.ITEM.getTag(ModTags.CHOCOLATE_BARS).isEmpty()) ||
                    BuiltInRegistries.ITEM.getTag(ModTags.CHOCOLATE_BARS).get().stream().findAny().isEmpty()) {
                t = CHOCOLATE;
            } else if (holder.is(ModTags.CHOCOLATE_BARS)) t = CHOCOLATE;
            else t = NONE;
            return Pair.of(t, ForgeHelper.getCraftingRemainingItem(item.getDefaultInstance()).map(ItemStack::getItem).orElse(null));
        }
    }

    public enum Winding implements StringRepresentable {
        NONE("none"),
        CHAIN("chain"),
        ROPE("rope");

        private final String name;

        Winding(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum RakeDirection implements StringRepresentable {
        NORTH_SOUTH("north_south", Direction.NORTH, Direction.SOUTH),
        EAST_WEST("east_west", Direction.EAST, Direction.WEST),
        SOUTH_EAST("south_east", Direction.SOUTH, Direction.EAST),
        SOUTH_WEST("south_west", Direction.SOUTH, Direction.WEST),
        NORTH_WEST("north_west", Direction.NORTH, Direction.WEST),
        NORTH_EAST("north_east", Direction.NORTH, Direction.EAST);

        private final List<Direction> directions;
        private final String name;

        RakeDirection(String name, Direction dir1, Direction dir2) {
            this.name = name;
            this.directions = Arrays.asList(dir1, dir2);
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public List<Direction> getDirections() {
            return directions;
        }

        public static RakeDirection fromDirections(List<Direction> directions) {
            for (RakeDirection shape : values()) {
                if (new HashSet<>(shape.getDirections()).containsAll(directions)) return shape;
            }
            return directions.get(0).getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
        }
    }

    public enum Rune implements StringRepresentable {
        A("a"),
        B("b"),
        C("c"),
        D("d"),
        E("e"),
        F("f"),
        G("g"),
        H("h"),
        I("i"),
        J("j"),
        K("k"),
        L("l"),
        M("m"),
        N("n"),
        O("o"),
        P("p"),
        Q("q"),
        R("r"),
        S("s"),
        T("t"),
        U("u"),
        V("v"),
        W("w"),
        X("x"),
        Y("y"),
        Z("z");

        private final String name;

        Rune(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }


    public enum DisplayStatus implements StringRepresentable {
        NONE, EMPTY, FULL;

        @Override
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return this.toString();
        }

        public boolean hasTile() {
            return this != NONE;
        }

        public boolean hasItem() {
            return this == FULL;
        }
    }

    public enum Bunting implements StringRepresentable {
        NONE, ROPE, BUNTING;

        @Override
        public String toString() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return this.toString();
        }

        public boolean isConnected() {
            return this != NONE;
        }

        public boolean hasBunting() {
            return this == BUNTING;
        }
    }

}
