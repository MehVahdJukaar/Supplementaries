package net.mehvahdjukaar.supplementaries.common.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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


    public static class ColorData implements CustomMapData, BlockAndTintGetter {

        private final HashMap<Vec2c, Block> positionsToBlocks = new HashMap<>();
        private final HashMap<Vec2c, ResourceKey<Biome>> biomeCache = new HashMap<>();

        public ColorData() {
        }

        public ColorData(CompoundTag tag) {
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
                                positionsToBlocks.put(new Vec2c(x, z), block.get());
                            }
                        }
                    }
                }
            }
            ListTag list2 = tag.getList("biome_data", Tag.TAG_COMPOUND);
            for (int i = 0; i < list2.size(); ++i) {
                CompoundTag c = list2.getCompound(i);
                long[] positions = c.getLongArray("positions");
                var biome = ResourceKey.create(Registries.BIOME, new ResourceLocation(c.getString("biome")));
                for (char j = 0; j < positions.length; j++) {
                    long y = positions[j];
                    for (var x : decodePositions(y >> ((j % 2 == 0) ? 0 : 64))) {
                        x = x + ((j % 2 == 0) ? 0 : 64);
                        int z = (char) (j / 2);
                        if (x > 127 || z > 127 || x < 0 || z < 0) {
                            int error = 1;
                        } else {
                            biomeCache.put(new Vec2c(x, z), biome);
                        }
                    }
                }
            }
        }

        @Override
        public void save(CompoundTag tag) {
            ListTag tagList = new ListTag();
            Map<Block, long[]> map = new HashMap<>();
            for (var e : positionsToBlocks.entrySet()) {
                Vec2c v = e.getKey();
                Block b = e.getValue();
                var array = map.computeIfAbsent(b, m -> new long[256]);
                int index = 2 * v.z + (v.x >= 64 ? 1 : 0);
                array[index] |= encodePosition(v.x % 64);

            }
            for (var m : map.entrySet()) {
                CompoundTag c = new CompoundTag();
                c.putString("block", Utils.getID(m.getKey()).toString());
                c.putLongArray("positions", m.getValue());
                tagList.add(c);
            }
            tag.put("color_data", tagList);

            ListTag tagList1 = new ListTag();
            Map<ResourceKey<Biome>, long[]> map1 = new HashMap<>();
            for (var e : biomeCache.entrySet()) {
                Vec2c v = e.getKey();
                var b = e.getValue();
                var array = map1.computeIfAbsent(b, m -> new long[256]);
                int index = 2 * v.z + (v.x >= 64 ? 1 : 0);
                array[index] |= encodePosition(v.x % 64);

            }
            for (var m : map1.entrySet()) {
                CompoundTag c = new CompoundTag();
                c.putString("biome", m.getKey().location().toString());
                c.putLongArray("positions", m.getValue());
                tagList1.add(c);
            }
            tag.put("biome_data", tagList1);
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
            Vec2c pair = new Vec2c((char) x, (char) z);
            if (hasCustomColor(block)) {
                positionsToBlocks.put(pair, block);
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                //dither biomes
                var biome = level.getBiome(pos).unwrapKey().get();
                biomeCache.put(pair, biome);
                ((ExpandedMapData) data).setCustomDataDirty();
            } else {
                if (positionsToBlocks.containsKey(pair)) {
                    positionsToBlocks.remove(pair);
                    ((ExpandedMapData) data).setCustomDataDirty();
                }
            }
        }

        private record Vec2c(char x, char z) {
            public Vec2c(int x, int y) {
                this((char) x, (char) y);
            }

            @Override
            public String toString() {
                return "X:" + (int) x + "Z:" + (int) z;
            }
        }


        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            var b = positionsToBlocks.get(new Vec2c(pos.getX(), pos.getZ()));
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
            for (var e : positionsToBlocks.entrySet()) {
                var v = e.getKey();
                BlockPos pos = new BlockPos(v.x, 0, v.z);
                int tint = Minecraft.getInstance().getBlockColors().getColor(e.getValue().defaultBlockState(),
                        this, pos, 0);
                tint = ColorUtils.swapFormat(tint);
                int k = pos.getX() + pos.getZ() * 128;
                byte packedId = colors[k];
                int color = MapColor.getColorFromPackedId(packedId);

                float lumIncrease = 1.3f;
                boolean water = false;
                if (packedId >= (12 << 2) && ((12 << 2) | 3) > packedId) {
                    water = true;
                    lumIncrease = 2f;
                }
                RGBColor tintColor = new RGBColor(tint);
                LABColor c = new RGBColor(color).asLAB();
                RGBColor gray = c.multiply(lumIncrease, 0, 0, 1).asRGB();
                var grayscaled = gray
                        .multiply(tintColor.red(), tintColor.green(), tintColor.blue(), 1)
                        .asHSL().multiply(1, 1.3f, 1, 1).asRGB().toInt();
                texture.getPixels().setPixelRGBA(pos.getX(), pos.getZ(), grayscaled);
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
            ResourceKey<Biome> biome = biomeCache.get(new Vec2c(x, z));
            if (biome != null) {
                Biome b = Utils.hackyGetRegistry(Registries.BIOME).get(biome);
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                return colorResolver.getColor(b, pos.getX() + 0.5, pos.getZ() + 0.5);
            }
            return 0;
        }
    }

}
