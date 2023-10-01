package net.mehvahdjukaar.supplementaries.common.misc;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class ColoredMapHandler {


    public static void init() {
    }

    protected static int DITHERING = 1;

    public static final CustomMapData.Type<ColorData> COLOR_DATA =
            MapDecorationRegistry.registerCustomMapSavedData(Supplementaries.res("color_data"), ColorData::new);

    public static ColorData getColorData(MapItemSavedData data) {
        return COLOR_DATA.getOrCreate(data, ColorData::new);
    }

    public static boolean hasCustomColor(Block state) {
        return state.builtInRegistryHolder().is(ModTags.TINTED_ON_MAPS);
    }

    public static class DirtyCounter {
        private final long[] dirtyPatches = new long[256];

    }

    public static class ColorData implements CustomMapData, BlockAndTintGetter {

        //TODO: optimize data packet
        private final Data tintData;

        public ColorData() {
            tintData = Data.create(Map.of(), Map.of());
        }

        public ColorData(CompoundTag tag) {
            HashMap<Vec2b, Block> positionsToBlocks = new HashMap<>();
            HashMap<Vec2b, ResourceLocation> biomeCache = new HashMap<>();
            ListTag list = tag.getList("color_data", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag c = list.getCompound(i);
                long[] positions = c.getLongArray("positions");
                var block = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(c.getString("block")));
                if (block.isPresent()) {
                    for (char j = 0; j < positions.length; j++) {
                        long y = positions[j];
                        for (var x : decodePositions(y >> ((j % 2 == 0) ? 0 : 64))) {
                            x = x + ((j % 2 == 0) ? 0 : 64);
                            int z = (char) (j / 2);
                            if (x > 127 || z > 127 || x < 0 || z < 0) {
                                int error = 1;
                            } else {
                                positionsToBlocks.put(new Vec2b(x, z), block.get());
                            }
                        }
                    }
                }
            }
            ListTag list2 = tag.getList("biome_data", Tag.TAG_COMPOUND);
            for (int i = 0; i < list2.size(); ++i) {
                CompoundTag c = list2.getCompound(i);
                long[] positions = c.getLongArray("positions");
                var biome = ResourceLocation.tryParse(c.getString("biome"));
                if (biome == null) continue;
                for (char j = 0; j < positions.length; j++) {
                    long y = positions[j];
                    for (var x : decodePositions(y >> ((j % 2 == 0) ? 0 : 64))) {
                        x = x + ((j % 2 == 0) ? 0 : 64);
                        int z = (char) (j / 2);
                        if (x > 127 || z > 127 || x < 0 || z < 0) {
                            int error = 1;
                        } else {
                            biomeCache.put(new Vec2b(x, z), biome);
                        }
                    }
                }
            }
            tintData = Data.create(positionsToBlocks, biomeCache);
        }

        @Override
        public void save(CompoundTag tag) {
            ListTag blockTagList = new ListTag();
            ListTag biomeTagList = new ListTag();
            Map<Block, long[]> blockMap = new HashMap<>();
            Map<ResourceLocation, long[]> biomeMap = new HashMap<>();
            var iterator = tintData.getAllEntries();

            while (iterator.hasNext()) {
                var e = iterator.next();
                Vec2b v = e.getKey();
                var blockBiomePair = e.getValue();
                Block block = blockBiomePair.getFirst();
                ResourceLocation biome = blockBiomePair.getSecond();

                var blockArray = blockMap.computeIfAbsent(block, m -> new long[256]);
                var biomeArray = biomeMap.computeIfAbsent(biome, m -> new long[256]);

                int index = 2 * v.z + (v.x >= 64 ? 1 : 0);
                blockArray[index] |= encodePosition(v.x % 64);
                biomeArray[index] |= encodePosition(v.x % 64);
            }

            for (var blockEntry : blockMap.entrySet()) {
                CompoundTag blockCompound = new CompoundTag();
                blockCompound.putString("block", Utils.getID(blockEntry.getKey()).toString());
                blockCompound.putLongArray("positions", blockEntry.getValue());
                blockTagList.add(blockCompound);
            }

            for (var biomeEntry : biomeMap.entrySet()) {
                CompoundTag biomeCompound = new CompoundTag();
                biomeCompound.putString("biome", biomeEntry.getKey().toString());
                biomeCompound.putLongArray("positions", biomeEntry.getValue());
                biomeTagList.add(biomeCompound);
            }

            tag.put("color_data", blockTagList);
            tag.put("biome_data", biomeTagList);
        }


        public static long encodePosition(int position) {
            return (1L << (position));
        }

        public static List<Integer> decodePositions(long encodedValue) {
            List<Integer> positionsList = new ArrayList<>();
            long position = 1; // Start with position 1 (0-based index)

            while (encodedValue != 0) {
                if ((encodedValue & 1L) != 0) {
                    positionsList.add((int) position - 1);
                }
                encodedValue >>>= 1; // Use unsigned right shift to check the next bit
                position++;
            }

            return positionsList;
        }

        @Override
        public Type<?> getType() {
            return COLOR_DATA;
        }

        public void markColored(int x, int z, Block block, Level level, BlockPos pos, MapItemSavedData data) {
            if (hasCustomColor(block)) {
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                //dither biomes
                var biome = level.getBiome(pos).unwrapKey().get();
                Vec2b v = new Vec2b(x, z);
                Pair<Block, ResourceLocation> pair = Pair.of(block, biome.location());
                if (!Objects.equals(tintData.getEntry(v), pair)) {
                    tintData.addEntry(v, pair);
                    this.setDirty(data);
                }
            } else {
                if (tintData.removeIfPresent(new Vec2b(x, z))) {
                    // this.setDirty(data);
                    //dont need to sync these, they just stay unused
                }
            }
        }


        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            var b = tintData.getBlock(new Vec2b(pos.getX(), pos.getZ()));
            return b == null ? Blocks.AIR.defaultBlockState() : b.defaultBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }


        @Environment(EnvType.CLIENT)
        public void processTexture(DynamicTexture texture, byte[] colors) {
            var iterator = tintData.getAllEntries();
            while (iterator.hasNext()) {
                var e = iterator.next();
                var v = e.getKey();
                Block block = e.getValue().getFirst();
                BlockPos pos = new BlockPos(v.x, 0, v.z);
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                int tint = blockColors.getColor(block.defaultBlockState(),
                        this, pos, 0);
                if (tint != -1) {
                    int k = pos.getX() + pos.getZ() * 128;
                    byte packedId = colors[k];

                    float lumIncrease = 1.3f;
                    MapColor mapColor = MapColor.byId((packedId & 255) >> 2);
                    if (mapColor == MapColor.WATER) {
                        lumIncrease = 2f;
                    }
                    /*
                    else if(mapColor == MapColor.PLANT){
                        if(tint == blockColors.getColor(Blocks.GRASS.defaultBlockState(), this, pos, 0)){
                             packedId = MapColor.GRASS.getPackedId(MapColor.Brightness.byId(packedId & 3));
                        }
                    }*/
                    else if(mapColor == MapColor.PLANT &&  !block.defaultBlockState().isSolid()){
                        packedId = MapColor.GRASS.getPackedId(MapColor.Brightness.byId(packedId & 3));
                    }
                    int color = MapColor.getColorFromPackedId(packedId);

                    tint = ColorUtils.swapFormat(tint);
                    RGBColor tintColor = new RGBColor(tint);
                    LABColor c = new RGBColor(color).asLAB();
                    RGBColor gray = c.multiply(lumIncrease, 0, 0, 1).asRGB();
                    var grayscaled = gray
                            .multiply(tintColor.red(), tintColor.green(), tintColor.blue(), 1)
                            .asHSL().multiply(1, 1.3f, 1, 1).asRGB().toInt();
                    texture.getPixels().setPixelRGBA(pos.getX(), pos.getZ(), grayscaled);
                }
            }
        }

        @Override
        public float getShade(Direction direction, boolean shade) {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return ClientRegistry.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {

            int x = pos.getX();
            int z = pos.getZ();
            ResourceLocation biome = tintData.getBiome(new Vec2b(x, z));
            if (biome != null) {
                Biome b = Utils.hackyGetRegistry(Registries.BIOME).get(biome);
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                return colorResolver.getColor(b, pos.getX() + 0.5, pos.getZ() + 0.5);
            }
            return 0;
        }
    }

    private record Vec2b(byte x, byte z) {
        public Vec2b(int x, int y) {
            this((byte) x, (byte) y);
        }

        @Override
        public String toString() {
            return "X:" + (int) x + "Z:" + (int) z;
        }
    }

    //unnecessary questionable optimization stuff
    private interface Data {

        @Nullable
        default ResourceLocation getBiome(Vec2b v) {
            return getEntry(v).getSecond();
        }

        @Nullable
        default Block getBlock(Vec2b v) {
            return getEntry(v).getFirst();
        }

        Pair<Block, ResourceLocation> getEntry(Vec2b v);

        static Data create(Map<Vec2b, Block> blockMap, Map<Vec2b, ResourceLocation> biomeData) {
            if (blockMap.size() < 6000 || biomeData.size() < 6000) {
                return new HashMapData(blockMap, biomeData);
            } else return new ArrayData(blockMap, biomeData);
        }

        Iterator<Map.Entry<Vec2b, Pair<Block, ResourceLocation>>> getAllEntries();

        void addEntry(Vec2b v, Pair<Block, ResourceLocation> pair);

        boolean removeIfPresent(Vec2b v);
    }

    private static class HashMapData implements Data {

        private final Map<Vec2b, Pair<Block, ResourceLocation>> blockMap = new HashMap<>();

        public HashMapData(Map<Vec2b, Block> blocks, Map<Vec2b, ResourceLocation> biomes) {
            // Iterate over one of the maps (assuming they have the same keys)
            for (Map.Entry<Vec2b, Block> blockEntry : blocks.entrySet()) {
                Vec2b key = blockEntry.getKey();
                Block block = blockEntry.getValue();
                ResourceLocation biome = biomes.get(key); // Get the corresponding biome
                if (biome != null) {
                    Pair<Block, ResourceLocation> pair = Pair.of(block, biome);
                    blockMap.put(key, pair);
                }
            }
        }

        @Override
        public @Nullable Pair<Block, ResourceLocation> getEntry(Vec2b v) {
            return blockMap.get(v);
        }

        @Override
        public boolean removeIfPresent(Vec2b v) {
            if (blockMap.containsKey(v)) {
                blockMap.remove(v);
                return true;
            }
            return false;
        }

        @Override
        public Iterator<Map.Entry<Vec2b, Pair<Block, ResourceLocation>>> getAllEntries() {
            return blockMap.entrySet().iterator();
        }

        @Override
        public void addEntry(Vec2b v, Pair<Block, ResourceLocation> pair) {
            blockMap.put(v, pair);
        }
    }

    private static class ArrayData implements Data {

        private final List<Pair<Block, ResourceLocation>> indexes = new ArrayList<>();
        private final byte[][] blockArray = new byte[128][128];

        public ArrayData(Map<Vec2b, Block> blocks, Map<Vec2b, ResourceLocation> biomes) {
            for (var entry : blocks.entrySet()) {
                Vec2b key = entry.getKey();
                Block block = entry.getValue();
                ResourceLocation biome = biomes.get(key);
                int index = getOrCreateBlockIndex(Pair.of(block, biome));
                blockArray[key.x()][key.z()] = (byte) (index + 1);
            }
        }

        @Override
        public boolean removeIfPresent(Vec2b v) {
            if (blockArray[v.x][v.z] != 0) {
                blockArray[v.x][v.z] = 0;
                return true;
            }
            return false;
        }

        @Override
        public void addEntry(Vec2b v, Pair<Block, ResourceLocation> pair) {
            int index = getOrCreateBlockIndex(pair);
            blockArray[v.x][v.z] = (byte) (index + 1);
        }

        private int getOrCreateBlockIndex(Pair<Block, ResourceLocation> key) {
            int index = indexes.indexOf(key);
            if (index == -1) {
                indexes.add(key);
                index = indexes.indexOf(key);
            }
            return index;
        }

        @Override
        public @Nullable Pair<Block, ResourceLocation> getEntry(Vec2b v) {
            byte index = blockArray[v.x][v.z];
            if (index == 0) return null;
            return indexes.get(index - 1);
        }

        @Override
        public Iterator<Map.Entry<Vec2b, Pair<Block, ResourceLocation>>> getAllEntries() {
            return new ArrayIterator();
        }

        // Custom iterator for blocks
        private class ArrayIterator implements Iterator<Map.Entry<Vec2b, Pair<Block, ResourceLocation>>> {
            private int x = 0;
            private int z = 0;

            @Override
            public boolean hasNext() {
                while (x < 128) {
                    while (z < 128) {
                        if (blockArray[x][z] != 0) {
                            return true;
                        }
                        z++;
                    }
                    z = 0;
                    x++;
                }
                return false;
            }

            @Override
            public Map.Entry<Vec2b, Pair<Block, ResourceLocation>> next() {
                while (x < 128) {
                    while (z < 128) {
                        if (blockArray[x][z] != 0) {
                            var entry = indexes.get(blockArray[x][z] - 1);
                            Vec2b position = new Vec2b((byte) x, (byte) z);
                            z++;
                            return new AbstractMap.SimpleEntry<>(position, entry);
                        }
                        z++;
                    }
                    z = 0;
                    x++;
                }
                return null; // No more elements
            }
        }
    }

}