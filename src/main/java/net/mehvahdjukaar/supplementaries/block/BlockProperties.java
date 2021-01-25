package net.mehvahdjukaar.supplementaries.block;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;

public class BlockProperties {

    //TODO: I hope nobody is reading this

    //blockstate properties
    public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
    public static final IntegerProperty EXTENSION = IntegerProperty.create("extension", 0, 2);
    public static final BooleanProperty TILE = BooleanProperty.create("tile");
    public static final IntegerProperty TILE_3 = IntegerProperty.create("tile_3", 0, 2);
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty HAS_JAR = BooleanProperty.create("has_jar");
    public static final EnumProperty<CommonUtil.WoodType> WOOD_TYPE = EnumProperty.create("wood_type", CommonUtil.WoodType.class);
    // it's detecting incoming laser and its distance
    public static final IntegerProperty RECEIVING = IntegerProperty.create("laser_receiving", 0, 15);
    public static final IntegerProperty LIGHT_LEVEL_0_15 = IntegerProperty.create("light_level", 0, 15);
    public static final BooleanProperty HAS_ITEM = BooleanProperty.create("has_item");
    public static final IntegerProperty BITES_0_4 = IntegerProperty.create("bites", 0, 6);


}
