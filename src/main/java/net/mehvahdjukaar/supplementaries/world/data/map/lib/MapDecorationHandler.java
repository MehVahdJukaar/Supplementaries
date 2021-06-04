package net.mehvahdjukaar.supplementaries.world.data.map.lib;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDecorationHandler {
    private static final Map<String, CustomDecorationType<?,?>> DECORATION_TYPES = new HashMap<>();

    public static final CustomDecorationType<?,?> GENERIC_STRUCTURE_TYPE = makeSimpleType(Supplementaries.MOD_ID, "generic_structure");


    public static void register(CustomDecorationType<?,?> newType){
        String id = newType.getSerializeId();
        if(DECORATION_TYPES.containsKey(id)){
            throw new IllegalArgumentException("Duplicate map decoration registration " + id);
        }
        else {
            DECORATION_TYPES.put(id, newType);
        }
    }

    /**
     * registers a simple decoration with no associated world marker.<br>
     * useful for exploration maps
     * @param modId mod id
     * @param name decoration name
     */
    public static void registerSimple(String modId, String name){
        register(makeSimpleType(modId,name));
    }

    /**
     * creates a simple decoration type with no associated marker
     * @param modId mod id
     * @param name decoration name
     * @return newly created decoration type
     */
    public static CustomDecorationType<?,?> makeSimpleType(String modId,String name){
        return new CustomDecorationType<>(new ResourceLocation(modId,name),CustomDecoration::new);
    }

    @Nullable
    public static CustomDecorationType<?,?> get(ResourceLocation id){
        return get(id.toString());
    }

    @Nullable
    public static CustomDecorationType<?,?> get(String id){
        return DECORATION_TYPES.get(id);
    }

    @Nullable
    public static MapWorldMarker<?> readWorldMarker(CompoundNBT compound){
        for(String s : DECORATION_TYPES.keySet()){
            if(compound.contains(s)){
                return DECORATION_TYPES.get(s).loadWorldMarkerFromNBT(compound.getCompound(s));
            }
        }
        return null;
    }

    /**
     * returns a list of suitable world markers associated to a position
     * @param reader world
     * @param pos world position
     * @return markers found
     */
    @Nullable
    public static List<MapWorldMarker<?>> getMarkersFromWorld(IBlockReader reader, BlockPos pos){
        List<MapWorldMarker<?>> list = new ArrayList<>();
        for(CustomDecorationType<?,?> type : DECORATION_TYPES.values()){
            MapWorldMarker<?> c = type.getWorldMarkerFromWorld(reader,pos);
            if(c!=null)list.add(c);
        }
        return list;
    }

    /**
     * Adds a static decoration tp a map itemstack NBT.<br>
     * Such decoration will not have any world marker associated and wont be toggleable
     * @param stack map item stack
     * @param pos decoration world pos
     * @param id decorationType id. if invalid will default to generic structure decoration
     * @param mapColor map item tint color
     */
    public static void addTargetDecoration(ItemStack stack, BlockPos pos, ResourceLocation id, int mapColor) {

        CustomDecorationType<?,?> type = DECORATION_TYPES.getOrDefault(id.toString(),GENERIC_STRUCTURE_TYPE);

        ListNBT listnbt;
        if (stack.hasTag() && stack.getTag().contains("CustomDecorations", 9)) {
            listnbt = stack.getTag().getList("CustomDecorations", 10);
        } else {
            listnbt = new ListNBT();
            stack.addTagElement("CustomDecorations", listnbt);
        }
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.putString("type", type.getSerializeId());
        compoundnbt.putInt("x", pos.getX());
        compoundnbt.putInt("z", pos.getZ());
        listnbt.add(compoundnbt);
        if (mapColor!=0) {
            CompoundNBT com = stack.getOrCreateTagElement("display");
            com.putInt("MapColor", mapColor);
        }

    }



}
