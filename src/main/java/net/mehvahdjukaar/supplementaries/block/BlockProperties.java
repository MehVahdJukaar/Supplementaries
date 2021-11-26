package net.mehvahdjukaar.supplementaries.block;

import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.block.util.IBellConnections;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.SpecialPlayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Arrays;
import java.util.List;

public class BlockProperties {

    //BlockState properties
    public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
    public static final BooleanProperty TILE = BooleanProperty.create("tile");
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty HAS_JAR = BooleanProperty.create("has_jar");
    // it's detecting incoming laser and its distance
    public static final IntegerProperty RECEIVING = IntegerProperty.create("laser_receiving", 0, 15);
    public static final IntegerProperty LIGHT_LEVEL_0_15 = IntegerProperty.create("light_level", 0, 15);
    public static final BooleanProperty HAS_ITEM = BooleanProperty.create("has_item");
    public static final IntegerProperty EXTENSION = IntegerProperty.create("extension", 0, 2);
    public static final BooleanProperty KNOT = BooleanProperty.create("knot");
    public static final BooleanProperty TIPPED = BooleanProperty.create("tipped");
    public static final IntegerProperty PANCAKES_1_8 = IntegerProperty.create("pancakes", 1, 8);
    public static final EnumProperty<Topping> TOPPING = EnumProperty.create("topping", Topping.class);
    public static final EnumProperty<Winding> WINDING = EnumProperty.create("winding", Winding.class);
    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");
    public static final BooleanProperty AXIS_Y = BooleanProperty.create("axis_y");
    public static final BooleanProperty AXIS_X = BooleanProperty.create("axis_x");
    public static final BooleanProperty AXIS_Z = BooleanProperty.create("axis_z");
    public static final BooleanProperty FLOOR = BooleanProperty.create("floor");
    public static final BooleanProperty LAVALOGGED = BooleanProperty.create("lavalogged");
    public static final EnumProperty<RakeDirection> RAKE_DIRECTION = EnumProperty.create("shape", RakeDirection.class);
    public static final BooleanProperty HAS_BLOCK = BooleanProperty.create("has_block");
    public static final BooleanProperty ROTATING = BooleanProperty.create("rotating");
    public static final EnumProperty<PostType> POST_TYPE = EnumProperty.create("type", PostType.class);
    public static final EnumProperty<BellAttachment> BELL_ATTACHMENT = EnumProperty.create("attachment", BellAttachment.class);
    public static final EnumProperty<IBellConnections.BellConnection> BELL_CONNECTION = EnumProperty.create("connection", IBellConnections.BellConnection.class);
    public static final IntegerProperty HONEY_LEVEL_POT = IntegerProperty.create("honey_level", 0, 4);
    public static final IntegerProperty BURNING = IntegerProperty.create("burning", 0, 8);
    public static final IntegerProperty BOOKS = IntegerProperty.create("books", 1, 4);
    public static final BooleanProperty WRITTEN = BooleanProperty.create("written");
    public static final BooleanProperty ANTIQUE = BooleanProperty.create("ye_olde");
    public static final BooleanProperty TREASURE = BooleanProperty.create("treasure");
    public static final IntegerProperty ROTATION_16_UP = IntegerProperty.create("rotation_up", 0, 15);
    public static final IntegerProperty WIND_STRENGTH = IntegerProperty.create("wind_strength", 0, 3);

    //model properties
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();
    public static final ModelProperty<Boolean> FANCY = new ModelProperty<>();
    public static final ModelProperty<Boolean> FRAMED = new ModelProperty<>();

    public static final EnumProperty<Rune> RUNE = EnumProperty.create("rune", Rune.class);

    public enum PostType implements StringRepresentable {
        POST("post", 4),
        PALISADE("palisade", 6),
        WALL("wall", 8),
        BEAM("beam", 10);

        private final String name;
        private final int width;

        PostType(String name, int width) {
            this.name = name;
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

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
    }

    public enum Attachment implements StringRepresentable {
        BLOCK("block"),
        BEAM("beam"),
        WALL("wall"),
        PALISADE("palisade"),
        POST("post");

        private final String name;

        Attachment(String name) {
            this.name = name;
        }

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
    }


    public enum Topping implements StringRepresentable {
        NONE("none"),
        HONEY("honey"),
        SYRUP("syrup"),
        CHOCOLATE("chocolate");

        private final String name;

        Topping(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Topping fromFluid(SoftFluid s) {
            if (s == SoftFluidRegistry.HONEY) return HONEY;
            String name = s.getRegistryName().getPath();
            if (name.equals("chocolate")) return CHOCOLATE;
            if (name.equals("syrup") || name.equals("maple_syrup")) return SYRUP;
            return NONE;
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
                if (shape.getDirections().containsAll(directions)) return shape;
            }
            return directions.get(0).getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
        }
    }


    public enum BellAttachment implements StringRepresentable {
        CEILING("ceiling"),
        SINGLE_WALL("single_block"),
        DOUBLE_WALL("double_block");

        private final String name;

        BellAttachment(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
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


}
