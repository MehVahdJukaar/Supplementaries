package net.mehvahdjukaar.supplementaries.common.misc;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColoredMapHandler {

    public static void init() {
    }


    public static final CustomMapData.Type<ColorData> COLOR_DATA =
            MapDecorationRegistry.registerCustomMapSavedData(Supplementaries.res("color_data"), ColorData::new);

    public static ColorData getColorData(MapItemSavedData data) {
        return COLOR_DATA.getOrCreate(data, ColorData::new);
    }

    public static boolean hasCustomColor(Block state) {
        return state == (Blocks.GRASS_BLOCK);
    }


    public static class ColorData implements CustomMapData {

        private final Int2ObjectArrayMap<Block> positionsToBlocks = new Int2ObjectArrayMap<>();

        public ColorData() {
        }

        public ColorData(CompoundTag tag) {
            ListTag list = tag.getList("color_data", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag c = list.getCompound(i);
                int positions = c.getInt("positions");
                var block = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(c.getString("block")));
                if (block.isPresent()) {
                    for (var p : decodePositions(positions)) {
                        positionsToBlocks.put(p.intValue(), block.get());
                    }
                }
            }
        }

        @Override
        public void save(CompoundTag tag) {
            ListTag tagList = new ListTag();
            Map<Block, Integer> map = new HashMap<>();
            for (var e : positionsToBlocks.int2ObjectEntrySet()) {
                int index = e.getIntKey();
                Block b = e.getValue();
                map.merge(b, encodePosition(0, index),
                        (existingValue, newValue) -> existingValue | newValue);
            }
            for (var m : map.entrySet()) {
                CompoundTag c = new CompoundTag();
                c.putString("block", Utils.getID(m.getKey()).toString());
                c.putInt("positions", m.getValue());
                tagList.add(c);
            }
            tag.put("color_data", tagList);
        }

        public static int encodePosition(int encodedValue, int position) {
            return encodedValue | (1 << (position - 1));
        }

        public static List<Integer> decodePositions(int encodedValue) {
            List<Integer> positionsList = new ArrayList<>();
            int position = 1; // Start with position 1 (0-based index)
            int mask = 1;

            while (encodedValue != 0) {
                if ((encodedValue & mask) != 0) {
                    positionsList.add(position);
                }
                encodedValue >>= 1; // Right shift to check the next bit
                position++;
            }
            return positionsList;
        }

        @Override
        public Type<?> getType() {
            return COLOR_DATA;
        }

        public void markColored(int x, int z, Block block) {
            if (hasCustomColor(block)) {
                int index = (x << 7) | z;
                positionsToBlocks.put(index, block);
            }
        }
    }

}
