package net.mehvahdjukaar.supplementaries.common.misc;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.moonlight.core.mixins.MapDataMixin;
import net.mehvahdjukaar.moonlight.core.mixins.MapItemDataPacketMixin;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ColoredMapHandler {


    public static void init() {
    }

    protected static int DITHERING = 1;

    public static final CustomMapData.Type<ColorData> COLOR_DATA =
            MapDataRegistry.registerCustomMapSavedData(Supplementaries.res("color_data"), ColorData::new);

    public static ColorData getColorData(MapItemSavedData data) {
        return COLOR_DATA.get(data);
    }

    //null if no color to be sent
    @Nullable
    public static Block getCustomColor(Block state) {
        Holder.Reference<Block> blockReference = state.builtInRegistryHolder();
        if (blockReference.is(ModTags.NOT_TINTED_ON_MAPS)) return null;
        //packs similar colored blocks so we stay in the 16 blocks limit
        if (blockReference.is(ModTags.TINTED_ON_MAPS_GC)) {
            if (state instanceof BushBlock) return Blocks.GRASS;
            return Blocks.GRASS_BLOCK;
        }
        if (blockReference.is(ModTags.TINTED_ON_MAPS_FC)) {
            return Blocks.OAK_LEAVES;
        }
        if (blockReference.is(ModTags.TINTED_ON_MAPS_WC)) {
            return Blocks.WATER;
        }
        if (blockReference.is(ModTags.TINTED_ON_MAPS_GENERIC)) {
            return state;
        }
        return null;
    }

    private static class Counter implements CustomMapData.DirtyCounter {
        private int minDirtyX = 0;
        private int maxDirtyX = 127;
        private int minDirtyZ = 0;
        private int maxDirtyZ = 127;
        private boolean posDirty = true;
        private boolean blockDirty = true;
        private boolean biomesDirty = true;

        public void markDirty(int x, int z, boolean changedBiome, boolean changedBlock) {
            if (changedBiome) this.biomesDirty = true;
            if (changedBlock) this.blockDirty = true;
            if (this.posDirty) {
                this.minDirtyX = Math.min(this.minDirtyX, x);
                this.minDirtyZ = Math.min(this.minDirtyZ, z);
                this.maxDirtyX = Math.max(this.maxDirtyX, x);
                this.maxDirtyZ = Math.max(this.maxDirtyZ, z);
            } else {
                //reset
                this.posDirty = true;
                this.minDirtyX = x;
                this.minDirtyZ = z;
                this.maxDirtyX = x;
                this.maxDirtyZ = z;
            }

        }

        @Override
        public boolean isDirty() {
            return posDirty || biomesDirty || blockDirty;
        }

        @Override
        public void clearDirty() {
            this.biomesDirty = false;
            this.blockDirty = false;
            this.posDirty = false;
            this.minDirtyX = 0;
            this.minDirtyZ = 0;
            this.maxDirtyX = 0;
            this.maxDirtyZ = 0;
        }

    }

    public static class ColorData implements CustomMapData<ColoredMapHandler.Counter>, BlockAndTintGetter {

        public static final int BIOME_SIZE = 4;
        public static final String MIN_X = "min_x";
        public static final String MAX_X = "max_x";
        public static final String MIN_Z = "min_z";
        private byte[][] data = null;
        private final List<ResourceLocation> biomesIndexes = new ArrayList<>();
        private final List<Block> blockIndexes = new ArrayList<>();

        @Nullable
        private Pair<Block, ResourceLocation> getEntry(int x, int z) {
            if (data == null) return null;
            if(x<0 || x>=128 || z<0 || z>=128){
                return null; //error
            }
            if (data[x] != null) {
                int packed = Byte.toUnsignedInt(data[x][z]); //treated as unsigned
                if (packed == 0) return null;
                packed -= 1; //discard 0 index
                int bi = packed & ((1 << BIOME_SIZE) - 1);
                int bli = packed >> BIOME_SIZE;
                if (bi >= blockIndexes.size() || bli >= biomesIndexes.size()) {
                    return null; //error
                }
                if (bi < 0 || bli < 0) {
                    return null; //error
                }
                return Pair.of(blockIndexes.get(bi), biomesIndexes.get(bli));
            }
            return null;
        }

        private void addEntry(MapItemSavedData md, int x, int z, Pair<Block, ResourceLocation> res) {

            boolean changedBiome;
            boolean changedBlock;
            Block block = res.getFirst();
            if (!blockIndexes.contains(block)) {
                if (blockIndexes.size() >= 16) {
                    //TODO: add counter and recompute every now and then when this exceeds
                    return;
                    //cant store biomes anymore... oh well
                }
                blockIndexes.add(block);
                changedBlock = true;
            } else {
                changedBlock = false;
            }
            int blockIndex = blockIndexes.indexOf(block);
            ResourceLocation biome = res.getSecond();
            if (!biomesIndexes.contains(biome)) {
                if (biomesIndexes.size() >= 16) {
                    return;
                    //cant store biomes anymore... oh well
                }
                biomesIndexes.add(biome);
                changedBiome = true;
            } else {
                changedBiome = false;
            }
            int biomeIndex = biomesIndexes.indexOf(biome);


            if (data == null) {
                data = new byte[128][];
            }
            if (data[x] == null) data[x] = new byte[128];
            data[x][z] = (byte) (((blockIndex & ((1 << BIOME_SIZE) - 1)) | (biomeIndex << BIOME_SIZE)) + 1); //discard 0 index


            this.setDirty(md, counter -> counter.markDirty(x, z, changedBiome, changedBlock));

        }

        @Override
        public void load(CompoundTag tag) {
            if (tag.contains("positions")) {
                CompoundTag t = tag.getCompound("positions");

                int minX = 0;
                if (t.contains(MIN_X)) minX = t.getInt(MIN_X);
                int maxX = 127;
                if (t.contains(MAX_X)) maxX = t.getInt(MAX_X);
                int minZ = 0;
                if (t.contains(MIN_Z)) minZ = t.getInt(MIN_Z);

                for (int x = minX; x <= maxX; x++) {
                    byte[] rowData = t.getByteArray("pos_" + x);
                    if (data == null) {
                        data = new byte[128][];
                    }
                    if (data[x] == null) {
                        data[x] = new byte[128];
                    }
                    System.arraycopy(rowData, 0, data[x], minZ, rowData.length);
                }
            }
            if (tag.contains("biomes")) {
                biomesIndexes.clear();
                var biomes = tag.getList("biomes", Tag.TAG_COMPOUND);
                for (int j = 0; j < biomes.size(); j++) {
                    CompoundTag c = biomes.getCompound(j);
                    int i = c.getByte("index");
                    String id = c.getString("id");
                    biomesIndexes.add(i, new ResourceLocation(id));
                }
            }
            if (tag.contains("blocks")) {
                blockIndexes.clear();
                var blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
                for (int j = 0; j < blocks.size(); j++) {
                    CompoundTag c = blocks.getCompound(j);
                    int i = c.getByte("index");
                    String id = c.getString("id");
                    blockIndexes.add(i, BuiltInRegistries.BLOCK.get(new ResourceLocation(id)));
                }
            }
        }

        private void savePatch(CompoundTag tag, int minX, int maxX, int minZ, int maxZ,
                               boolean pos, boolean block, boolean biome) {

            if (pos && data != null) {
                CompoundTag t = new CompoundTag();
                if (minX != 0) t.putInt(MIN_X, minX);
                if (maxX != 127) t.putInt(MAX_X, maxX);
                if (minZ != 0) t.putInt(MIN_Z, minZ);

                for (int x = minX; x <= maxX; x++) {
                    if (data[x] != null) {
                        byte[] rowData = new byte[maxZ - minZ + 1];

                        System.arraycopy(data[x], minZ, rowData, 0, rowData.length);
                        t.putByteArray("pos_" + x, rowData);
                    }
                }
                tag.put("positions", t);
            }

            if (biome && !biomesIndexes.isEmpty()) {
                ListTag biomesList = new ListTag();
                for (int i = 0; i < biomesIndexes.size(); i++) {
                    CompoundTag biomeTag = new CompoundTag();
                    biomeTag.putByte("index", (byte) i);
                    biomeTag.putString("id", biomesIndexes.get(i).toString());
                    biomesList.add(biomeTag);
                }
                tag.put("biomes", biomesList);
            }
            if (block && !blockIndexes.isEmpty()) {
                ListTag blocksList = new ListTag();
                for (int i = 0; i < blockIndexes.size(); i++) {
                    CompoundTag blockTag = new CompoundTag();
                    blockTag.putByte("index", (byte) i);
                    blockTag.putString("id", Utils.getID(blockIndexes.get(i)).toString());
                    blocksList.add(blockTag);
                }
                tag.put("blocks", blocksList);
            }
        }

        @Override
        public void save(CompoundTag tag) {
            // save all
            savePatch(tag, 0, 127, 0, 127, true, true, true);
        }

        @Override
        public void saveToUpdateTag(CompoundTag tag, ColoredMapHandler.Counter dc) {
            this.savePatch(tag, dc.minDirtyX, dc.maxDirtyX, dc.minDirtyZ, dc.maxDirtyZ,
                    dc.posDirty, dc.blockDirty, dc.biomesDirty);
        }

        @Override
        public void loadUpdateTag(CompoundTag tag) {
            load(tag);
        }

        @Override
        public boolean persistOnCopyOrLock() {
            return false;
        }

        @Override
        public Type<?> getType() {
            return COLOR_DATA;
        }

        @Override
        public Counter createDirtyCounter() {
            return new Counter();
        }

        public void markColored(int x, int z, Block block, Level level, BlockPos pos, MapItemSavedData data) {
            Block customColor = getCustomColor(block);
            if(block == Blocks.BLUE_WOOL){
                int aa = 1;
            }
            if (customColor != null) {
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                //dither biomes
                var biome = level.getBiome(pos).unwrapKey().get();
                Pair<Block, ResourceLocation> pair = Pair.of(customColor, biome.location());
                if (!Objects.equals(this.getEntry(x, z), pair)) {
                    this.addEntry(data, x, z, pair);
                }
            } else {
                //remove unneded stufff
                if (this.data != null && this.data[x] != null && this.data[x][z] != 0) {
                    this.data[x][z] = 0;
                    this.setDirty(data, counter -> counter.markDirty(x, z, false, false));
                    for (var b : this.data[x]) {
                        if (b != 0) return;
                    }
                    this.data[x] = null;
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
            Pair<Block, ResourceLocation> entry = this.getEntry(pos.getX(), pos.getZ());
            return entry == null ? Blocks.AIR.defaultBlockState() : entry.getFirst().defaultBlockState();
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
        public void processTexture(NativeImage texture, int startX, int startY,  byte[] colors) {
            if (!ClientConfigs.Tweaks.COLORED_MAPS.get()) return;
            boolean tg = ClientConfigs.Tweaks.TALL_GRASS_COLOR_CHANGE.get();
            for (int x = 0; x < 128; ++x) {
                for (int z = 0; z < 128; ++z) {
                    var e = getEntry(x, z);
                    if (e == null) continue;
                    Block block = e.getFirst();
                    BlockPos pos = new BlockPos(x, 64, z); //this is bad. dont want to send extra data tho
                    BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                    int tint = blockColors.getColor(block.defaultBlockState(),
                            this, pos, 0);
                    if (tint != -1) {
                        int k = x + z * 128;
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
                        else if (mapColor == MapColor.PLANT && block instanceof BushBlock && tg) {
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
                        texture.setPixelRGBA(startX+ x,startY + z, grayscaled);
                    }
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
            var biome = this.getEntry(x, z);
            if (biome != null) {
                Biome b = Utils.hackyGetRegistry(Registries.BIOME).get(biome.getSecond());
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                return colorResolver.getColor(b, pos.getX() + 0.5, pos.getZ() + 0.5);
            }
            return 0;
        }

        public void clear() {
            data = null;
            biomesIndexes.clear();
            blockIndexes.clear();
        }
    }

}