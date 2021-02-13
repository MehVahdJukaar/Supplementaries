package net.mehvahdjukaar.supplementaries.block;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.IStringSerializable;

public class BlockProperties {

    //TODO: I hope nobody is reading this

    //blockstate properties
    public static final BooleanProperty EXTENDING = BooleanProperty.create("extending");
    public static final IntegerProperty HOUR = IntegerProperty.create("hour", 0, 23);
    public static final BooleanProperty TILE = BooleanProperty.create("tile");
    public static final IntegerProperty TILE_3 = IntegerProperty.create("tile_3", 0, 2);
    public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
    public static final BooleanProperty HAS_JAR = BooleanProperty.create("has_jar");
    // it's detecting incoming laser and its distance
    public static final IntegerProperty RECEIVING = IntegerProperty.create("laser_receiving", 0, 15);
    public static final IntegerProperty LIGHT_LEVEL_0_15 = IntegerProperty.create("light_level", 0, 15);
    public static final BooleanProperty HAS_ITEM = BooleanProperty.create("has_item");
    public static final IntegerProperty EXTENSION = IntegerProperty.create("extension", 0, 2);
    public static final EnumProperty<Attachment> ATTACHMENT = EnumProperty.create("attachment",Attachment.class);
    public static final EnumProperty<Attachment> CONNECTION_NORTH = EnumProperty.create("north_connection",Attachment.class);
    public static final EnumProperty<Attachment> CONNECTION_SOUTH = EnumProperty.create("south_connection",Attachment.class);
    public static final EnumProperty<Attachment> CONNECTION_EAST = EnumProperty.create("east_connection",Attachment.class);
    public static final EnumProperty<Attachment> CONNECTION_WEST = EnumProperty.create("west_connection",Attachment.class);
    public static final IntegerProperty POISON = IntegerProperty.create("poison", 0, 15);
    public static final BooleanProperty TIPPED = BooleanProperty.create("tipped");

    public enum Attachment implements IStringSerializable{
        NONE("none"), //default /no attachment
        BLOCK("block"); //block attachment / pillar attachment
        //WALL("wall"), //wall attachment
        //POST("post"), //post attachment (druid craft ie)
        //FENCE("fence"); //fence attachment
        private final String name;
        Attachment(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String getString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        public boolean isNone(){
            return this==NONE;
        }
    }

}
