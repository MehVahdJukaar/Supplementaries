package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.decorativeblocks.DecoBlocksCompatRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BlockProperties {

    //BlockState properties
    public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty HAS_JAR = BooleanProperty.create("has_jar");
    public static final IntegerProperty LIGHT_LEVEL_0_15 = IntegerProperty.create("light_level", 0, 15);
    public static final IntegerProperty LIGHT_LEVEL_0_7 = IntegerProperty.create("light_level", 0, 7);
    public static final BooleanProperty HAS_ITEM = BooleanProperty.create("has_item");
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
    public static final IntegerProperty HONEY_LEVEL_POT = IntegerProperty.create("honey_level", 0, 4);
    public static final IntegerProperty BURNING = IntegerProperty.create("burning", 0, 8);
    public static final IntegerProperty BOOKS = IntegerProperty.create("books", 1, 4);
    public static final BooleanProperty ANTIQUE = BooleanProperty.create("ye_olde");
    public static final BooleanProperty TREASURE = BooleanProperty.create("treasure");
    public static final BooleanProperty PACKED = BooleanProperty.create("packed");
    public static final IntegerProperty WIND_STRENGTH = IntegerProperty.create("wind_strength", 0, 3);
    public static final IntegerProperty OPENING_PROGRESS = IntegerProperty.create("opening_progress", 0, 2);
    public static final EnumProperty<SignAttachment> SIGN_ATTACHMENT = EnumProperty.create("sign_attachment", SignAttachment.class);
    public static final EnumProperty<BlockAttachment> BLOCK_ATTACHMENT = EnumProperty.create("attachment", BlockAttachment.class);

    //model properties
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();
    public static final ModelProperty<Boolean> FANCY = new ModelProperty<>();
    public static final ModelProperty<Boolean> FRAMED = new ModelProperty<>();
    public static final ModelProperty<BlackboardTextureManager.BlackboardKey> BLACKBOARD = new ModelProperty<>();

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

        @Nullable
        public static PostType get(BlockState state) {
            return get(state, false);
        }

        @Nullable
        public static PostType get(BlockState state, boolean needsFullHeight) {

            PostType type = null;
            //if (state.getBlock().hasTileEntity(state)) return type;
            if (state.is(ModTags.POSTS)) {
                type = PostType.POST;
            } else if (state.is(ModTags.PALISADES) || (CompatHandler.deco_blocks && DecoBlocksCompatRegistry.isPalisade(state))) {
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

    //for wall lanterns
    public enum BlockAttachment implements StringRepresentable {
        BLOCK("block"),
        BEAM("beam"),
        WALL("wall"),
        PALISADE("palisade"),
        POST("post"),
        STICK("stick");

        private final String name;

        BlockAttachment(String name) {
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

        @Nullable
        public static BlockAttachment get(BlockState state, BlockPos pos, LevelReader level, Direction facing) {
            if (state.isFaceSturdy(level, pos, facing)) return BLOCK;
            PostType postType = PostType.get(state, true);
            if (postType == null) {
                //case for sticks
                if ((state.getBlock() instanceof StickBlock &&
                        (facing.getAxis() == Direction.Axis.X ?
                                !state.getValue(StickBlock.AXIS_X) :
                                !state.getValue(StickBlock.AXIS_Z))) ||
                        (state.getBlock() instanceof EndRodBlock &&
                                state.getValue(EndRodBlock.FACING).getAxis()== Direction.Axis.Y)) return STICK;
                return null;
            }
            return switch (postType) {
                case BEAM -> BEAM;
                case WALL -> WALL;
                case PALISADE -> PALISADE;
                case POST -> POST;
            };
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
            if (s == SoftFluidRegistry.HONEY.get()) return HONEY;
            String name = s.getRegistryName().getPath();
            if (name.equals("chocolate")) return CHOCOLATE;
            if (name.equals("syrup") || name.equals("maple_syrup") || name.equals("holy_syrup") || name.equals("unholy_syrup")) return SYRUP;
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


    public enum SignAttachment implements StringRepresentable {
        CEILING("ceiling"),
        BLOCK_BLOCK(BlockAttachment.BLOCK, BlockAttachment.BLOCK),
        BLOCK_BEAM(BlockAttachment.BLOCK, BlockAttachment.BEAM),
        BLOCK_WALL(BlockAttachment.BLOCK, BlockAttachment.WALL),
        BLOCK_PALISADE(BlockAttachment.BLOCK, BlockAttachment.PALISADE),
        BLOCK_POST(BlockAttachment.BLOCK, BlockAttachment.POST),


        BEAM_BLOCK(BlockAttachment.BEAM, BlockAttachment.BLOCK),
        BEAM_BEAM(BlockAttachment.BEAM, BlockAttachment.BEAM),
        BEAM_WALL(BlockAttachment.BEAM, BlockAttachment.WALL),
        BEAM_PALISADE(BlockAttachment.BEAM, BlockAttachment.PALISADE),
        BEAM_POST(BlockAttachment.BEAM, BlockAttachment.POST),


        WALL_BLOCK(BlockAttachment.WALL, BlockAttachment.BLOCK),
        WALL_BEAM(BlockAttachment.WALL, BlockAttachment.BEAM),
        WALL_WALL(BlockAttachment.WALL, BlockAttachment.WALL),
        WALL_PALISADE(BlockAttachment.WALL, BlockAttachment.PALISADE),
        WALL_POST(BlockAttachment.WALL, BlockAttachment.POST),


        PALISADE_BLOCK(BlockAttachment.PALISADE, BlockAttachment.BLOCK),
        PALISADE_BEAM(BlockAttachment.PALISADE, BlockAttachment.BEAM),
        PALISADE_WALL(BlockAttachment.PALISADE, BlockAttachment.WALL),
        PALISADE_PALISADE(BlockAttachment.PALISADE, BlockAttachment.PALISADE),
        PALISADE_POST(BlockAttachment.PALISADE, BlockAttachment.POST),


        POST_BLOCK(BlockAttachment.POST, BlockAttachment.BLOCK),
        POST_BEAM(BlockAttachment.POST, BlockAttachment.BEAM),
        POST_WALL(BlockAttachment.POST, BlockAttachment.WALL),
        POST_PALISADE(BlockAttachment.POST, BlockAttachment.PALISADE),
        POST_POST(BlockAttachment.POST, BlockAttachment.POST),

        STICK_BLOCK(BlockAttachment.STICK, BlockAttachment.BLOCK),
        STICK_BEAM(BlockAttachment.STICK, BlockAttachment.BEAM),
        STICK_WALL(BlockAttachment.STICK, BlockAttachment.WALL),
        STICK_PALISADE(BlockAttachment.STICK, BlockAttachment.PALISADE),
        STICK_POST(BlockAttachment.STICK, BlockAttachment.POST),
        STICK_STICK(BlockAttachment.STICK, BlockAttachment.STICK),

        BLOCK_STICK(BlockAttachment.BLOCK, BlockAttachment.STICK),
        BEAM_STICK(BlockAttachment.BEAM, BlockAttachment.STICK),
        WALL_STICK(BlockAttachment.WALL, BlockAttachment.STICK),
        PALISADE_STICK(BlockAttachment.PALISADE, BlockAttachment.STICK),
        POST_STICK(BlockAttachment.POST, BlockAttachment.STICK);

        public final BlockAttachment left;
        public final BlockAttachment right;
        private final String name;

        SignAttachment(BlockAttachment left, BlockAttachment right) {
            this.name = left.name + "_" + right.name;
            this.left = left;
            this.right = right;
        }

        SignAttachment(String name) {
            this.name = name;
            this.left = BlockAttachment.BLOCK;
            this.right = BlockAttachment.BLOCK;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public SignAttachment withAttachment(boolean left, @Nullable BlockAttachment attachment) {
            if (attachment == null) attachment = BlockAttachment.BLOCK;
            String s = left ? attachment.name + "_" + this.right : this.left + "_" + attachment.name;
            return SignAttachment.valueOf(s.toUpperCase(Locale.ROOT));
        }

    }


}
