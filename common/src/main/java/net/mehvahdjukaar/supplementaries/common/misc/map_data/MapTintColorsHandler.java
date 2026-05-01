package net.mehvahdjukaar.supplementaries.common.misc.map_data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class MapTintColorsHandler {

    public static final CustomMapData.Type<Patch, ColorData> COLOR_DATA =
            MapDataRegistry.registerCustomMapSavedData(Supplementaries.res("color_data"), ColorData::new,
                    Patch.STREAM_CODEC);
    protected static final int DITHERING = 1;
    //probably very dumb idea
    private static final Map<Block, Integer> BLOCK_IDS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Block> IDS_TO_BLOCK_CACHE = new ConcurrentHashMap<>();
    private static final Map<Holder<Biome>, Integer> BIOME_IDS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Holder<Biome>> IDS_TO_BIOME_CACHE = new ConcurrentHashMap<>();
    // brightness index to block-biome pair mapping to a color

    public static void init() {
    }

    public static ColorData getColorData(MapItemSavedData data) {
        return COLOR_DATA.get(data);
    }

    //null if no color to be sent
    @Nullable
    public static Block getSimilarColoredBlock(Block block) {
        Holder.Reference<Block> blockReference = block.builtInRegistryHolder();
        if (blockReference.is(ModTags.NOT_TINTED_ON_MAPS)) return null;
        //packs similar colored blocks so we stay in the 16 blocks limit
        if (blockReference.is(ModTags.TINTED_ON_MAPS_GC)) {
            if (block instanceof BushBlock) return Blocks.SHORT_GRASS;
            return Blocks.GRASS_BLOCK;
        }
        if (blockReference.is(ModTags.TINTED_ON_MAPS_FC) || block instanceof LeavesBlock) {
            return Blocks.OAK_LEAVES;
        }
        if (blockReference.is(ModTags.TINTED_ON_MAPS_WC)) {
            return Blocks.WATER;
        }
        if (blockReference.is(ModTags.TINTED_ON_MAPS_GENERIC)) {
            return block;
        }
        return null;
    }

    public static void clearIdCache() {
        IDS_TO_BLOCK_CACHE.clear();
        BLOCK_IDS_CACHE.clear();
        IDS_TO_BIOME_CACHE.clear();
        BIOME_IDS_CACHE.clear();
    }

    private static int getBlockId(Block block) {
        return BLOCK_IDS_CACHE.computeIfAbsent(block, BuiltInRegistries.BLOCK::getId);
    }

    private static Block getBlockFromId(int id) {
        return IDS_TO_BLOCK_CACHE.computeIfAbsent(id, BuiltInRegistries.BLOCK::byId);
    }

    private static int getBiomeId(Holder<Biome> biome, RegistryAccess registryAccess) {
        return BIOME_IDS_CACHE.computeIfAbsent(biome, r -> {
            var biomeReg = registryAccess.registryOrThrow(Registries.BIOME);
            return biomeReg.getId(r.value());
        });
    }

    private static Holder<Biome> getBiomeFromId(int id, RegistryAccess registryAccess) {
        return IDS_TO_BIOME_CACHE.computeIfAbsent(id, r -> {
            var biomeReg = registryAccess.registryOrThrow(Registries.BIOME);
            return biomeReg.getHolder(r).orElseThrow();
        });
    }

    public static class Counter implements CustomMapData.DirtyCounter {
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

    public record BlockAndBiome(Block block, Holder<Biome> biome) {
        public static BlockAndBiome of(Block block, Holder<Biome> biome) {
            return new BlockAndBiome(block, biome);
        }
    }

    public static class ColorData implements CustomMapData<MapTintColorsHandler.Counter, Patch>, BlockAndTintGetter {

        public static final int BIOME_SIZE = 4;
        public static final String MIN_X = "min_x";
        public static final String MAX_X = "max_x";
        public static final String MIN_Z = "min_z";
        private final List<Holder<Biome>> biomesIndexesPalette = new ArrayList<>();
        private final List<Block> blockIndexesPalette = new ArrayList<>();
        private byte[][] data = null;

        //client local values. one per instnace.
        BlockAndBiome lastEntryHack;


        protected boolean isEmpty() {
            return data == null;
        }

        @Nullable
        protected BlockAndBiome getEntry(int x, int z) {
            if (data == null) return null;
            if (x < 0 || x >= 128 || z < 0 || z >= 128) {
                return null; //error
            }
            if (data[x] != null) {
                int paletteIndex = paletteIndex(x, z); //treated as unsigned
                return unpackPaletteIndex(paletteIndex);
            }
            return null;
        }

        private @Nullable BlockAndBiome unpackPaletteIndex(int packed) {
            if (packed == 0) return null;
            packed -= 1; //discard 0 index
            int bi = packed & ((1 << BIOME_SIZE) - 1);
            int bli = packed >> BIOME_SIZE;
            if (bi >= blockIndexesPalette.size() || bli >= biomesIndexesPalette.size()) {
                return null;
            }
            return BlockAndBiome.of(blockIndexesPalette.get(bi), biomesIndexesPalette.get(bli));
        }

        protected byte packPaletteIndex(BlockAndBiome entry) {
            int blockIndex = blockIndexesPalette.indexOf(entry.block);
            int biomeIndex = biomesIndexesPalette.indexOf(entry.biome);
            return (byte) (((blockIndex & ((1 << BIOME_SIZE) - 1)) | (biomeIndex << BIOME_SIZE)) + 1);
        }

        protected int paletteIndex(int x, int z) {
            if (data == null || data[x] == null) return 0;
            return Byte.toUnsignedInt(data[x][z]);
        }

        private void addEntry(MapItemSavedData md, int x, int z, BlockAndBiome entry) {

            boolean changedBiome;
            boolean changedBlock;
            Block block = entry.block;
            if (!blockIndexesPalette.contains(block)) {
                if (blockIndexesPalette.size() >= 16) {
                    //TODO: add counter and recompute every now and then when this exceeds
                    return;
                    //cant store biomes anymore... oh well
                }
                blockIndexesPalette.add(block);
                changedBlock = true;
            } else {
                changedBlock = false;
            }
            Holder<Biome> biome = entry.biome;
            if (!biomesIndexesPalette.contains(biome)) {
                if (biomesIndexesPalette.size() >= 16) {
                    return;
                    //cant store biomes anymore... oh well
                }
                biomesIndexesPalette.add(biome);
                changedBiome = true;
            } else {
                changedBiome = false;
            }

            if (data == null) {
                data = new byte[128][];
            }
            if (data[x] == null) data[x] = new byte[128];
            data[x][z] = packPaletteIndex(entry); //discard 0 index

            this.setDirty(md, counter -> counter.markDirty(x, z, changedBiome, changedBlock));
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider provider) {
            if (tag.contains("palette")) {
                CompoundTag t = tag.getCompound("palette");

                int minX = 0;
                int maxX = 127;
                int minZ = 0;

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
                biomesIndexesPalette.clear();
                var biomes = tag.getList("biomes", Tag.TAG_COMPOUND);
                for (int j = 0; j < biomes.size(); j++) {
                    CompoundTag c = biomes.getCompound(j);
                    int i = c.getByte("index");
                    String id = c.getString("id");
                    try {
                        ResourceKey<Biome> resourceKey = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(id));
                        provider.lookupOrThrow(Registries.BIOME).get(
                                        resourceKey)
                                .ifPresent(b -> biomesIndexesPalette.add(i, b));
                    } catch (Exception error) {
                        Supplementaries.LOGGER.error("Error loading biome palette index {} with id {}", i, id, error);
                        Supplementaries.error();
                    }
                }
            }
            if (tag.contains("blocks")) {
                blockIndexesPalette.clear();
                var blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
                for (int j = 0; j < blocks.size(); j++) {
                    CompoundTag c = blocks.getCompound(j);
                    int i = c.getByte("index");
                    String id = c.getString("id");
                    blockIndexesPalette.add(i, BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)));
                }
            }
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider registries) {
            // save all
            if (data != null) {
                this.clearUnusedPalette();
                CompoundTag t = new CompoundTag();
                for (int x = 0; x <= 127; x++) {
                    if (data[x] != null) {
                        byte[] rowData = new byte[127 - 0 + 1];

                        System.arraycopy(data[x], 0, rowData, 0, rowData.length);
                        t.putByteArray("pos_" + x, rowData);
                    }
                }
                tag.put("palette", t);

                if (!biomesIndexesPalette.isEmpty()) {
                    ListTag biomesList = new ListTag();
                    for (int i = 0; i < biomesIndexesPalette.size(); i++) {
                        CompoundTag biomeTag = new CompoundTag();
                        biomeTag.putByte("index", (byte) i);
                        biomeTag.putString("id", biomesIndexesPalette.get(i).getRegisteredName());
                        biomesList.add(biomeTag);
                    }
                    tag.put("biomes", biomesList);
                }
                if (!blockIndexesPalette.isEmpty()) {
                    ListTag blocksList = new ListTag();
                    // we could use a NBTlist here instead. keeping this for backward compat
                    for (int i = 0; i < blockIndexesPalette.size(); i++) {
                        CompoundTag blockTag = new CompoundTag();
                        blockTag.putByte("index", (byte) i);
                        blockTag.putString("id", Utils.getID(blockIndexesPalette.get(i)).toString());
                        blocksList.add(blockTag);
                    }
                    tag.put("blocks", blocksList);
                }
            }

        }

        private void clearUnusedPalette() {
            //iterate over all the bytes, unpack them and find all used blocks and biomes. if we have more biome indexes and block indexes than what we use we need to remove those and then update the index list and update all entries that use those now shifted ids

            //TODO: implement this this
        }

        @Override
        public Patch createUpdatePatch(Counter dc) {
            int minX = dc.minDirtyX;
            int maxX = dc.maxDirtyX;
            int minZ = dc.minDirtyZ;
            int maxZ = dc.maxDirtyZ;
            boolean pos = dc.posDirty;
            boolean block = dc.blockDirty;
            boolean biome = dc.biomesDirty;
            Int2ObjectArrayMap<byte[]> positions = null;

            if (pos && data != null && ((minX != maxX) || (minZ != maxZ))) {
                positions = new Int2ObjectArrayMap<>();
                for (int x = minX; x <= maxX; x++) {
                    byte[] rowData = new byte[maxZ - minZ + 1];
                    //we need to send a contiguous array
                    if (data[x] != null) {
                        System.arraycopy(data[x], minZ, rowData, 0, rowData.length);
                    }
                    positions.put(x, rowData);
                }
            }
            return new Patch(minX, maxX, minZ, Optional.ofNullable(positions),
                    biome ? Optional.of(biomesIndexesPalette) : Optional.empty(),
                    block ? Optional.of(blockIndexesPalette) : Optional.empty());

        }

        @Override
        public void applyUpdatePatch(Patch patch) {
            if (patch.positions.isPresent()) {
                var positions = patch.positions.get();

                int minX = patch.minX;
                int maxX = patch.maxX;
                int minZ = patch.minZ;

                for (int x = minX; x <= maxX; x++) {

                    if (data == null) {
                        data = new byte[128][];
                    }
                    if (data[x] == null) {
                        data[x] = new byte[128];
                    }

                    byte[] rowData = positions.get(x);
                    System.arraycopy(Preconditions.checkNotNull(rowData), 0, data[x], minZ, rowData.length);
                }
            }
            if (patch.biomes.isPresent()) {
                biomesIndexesPalette.clear();
                biomesIndexesPalette.addAll(patch.biomes.get());
            }
            if (patch.blocks.isPresent()) {
                blockIndexesPalette.clear();
                blockIndexesPalette.addAll(patch.blocks.get());
            }
        }

        @Override
        public boolean persistOnCopyOrLock() {
            return true;
        }

        @Override
        public boolean persistOnRescale() {
            return false;
        }

        @Override
        public Type<Patch, ?> getType() {
            return COLOR_DATA;
        }

        @Override
        public Counter createDirtyCounter() {
            return new Counter();
        }

        public void markColored(int x, int z, Block block, Level level, BlockPos pos, MapItemSavedData data) {
            Block simplifiedBlock = getSimilarColoredBlock(block);
            if (simplifiedBlock != null) {
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                //dither biomes
                Holder<Biome> biome = level.getBiome(pos);
                BlockAndBiome pair = BlockAndBiome.of(simplifiedBlock, biome);
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
                //TODO: clear biomes and blocks when they arent used anymore
            }
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            BlockAndBiome entry = this.getEntry(pos.getX(), pos.getZ());
            return entry == null ? Blocks.AIR.defaultBlockState() : entry.block.defaultBlockState();
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
            if (lastEntryHack != null) {
                if (pos == null || colorResolver == null) {
                    throw new IllegalStateException("Block position of Color resolvers were null. How? " + pos + colorResolver);
                }
                int x = pos.getX();
                int z = pos.getZ();
                Biome b = lastEntryHack.biome.value();
                boolean odd = x % 2 == 0 ^ z % 2 == 1;
                //used for position blend. not color blend. used for stuff like swamp
                pos = pos.offset((odd ? DITHERING : -DITHERING), 0, (odd ? DITHERING : -DITHERING));
                return colorResolver.getColor(b, pos.getX() + 0.5, pos.getZ() + 0.5);
            }
            return 0;
        }

        public void clear() {
            data = null;
            biomesIndexesPalette.clear();
            blockIndexesPalette.clear();
        }

    }


    public record Patch(int minX, int maxX, int minZ, Optional<Int2ObjectArrayMap<byte[]>> positions,
                        Optional<List<Holder<Biome>>> biomes, Optional<List<Block>> blocks) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Patch> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public Patch decode(RegistryFriendlyByteBuf buf) {
                int minX = 0;
                int maxX = 0;
                int minZ = 0;
                boolean hasPositions = buf.readBoolean();
                Int2ObjectArrayMap<byte[]> positions = null;
                if (hasPositions) {
                    minX = buf.readInt();
                    maxX = buf.readInt();
                    minZ = buf.readInt();
                    int size = buf.readVarInt();
                    positions = new Int2ObjectArrayMap<>(size);
                    for (int i = 0; i < size; i++) {
                        int x = buf.readVarInt();
                        byte[] rowData = buf.readByteArray();
                        positions.put(x, rowData);
                    }
                }

                boolean hasBiomes = buf.readBoolean();
                List<Holder<Biome>> biomes = null;
                if (hasBiomes) {
                    int size = buf.readVarInt();
                    biomes = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        int id = buf.readVarInt();
                        biomes.add(getBiomeFromId(id, buf.registryAccess()));
                    }
                }

                boolean hasBlocks = buf.readBoolean();
                List<Block> blocks = null;
                if (hasBlocks) {
                    int size = buf.readVarInt();
                    blocks = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        int id = buf.readVarInt();
                        blocks.add(getBlockFromId(id));
                    }
                }
                return new Patch(minX, maxX, minZ, Optional.ofNullable(positions),
                        Optional.ofNullable(biomes), Optional.ofNullable(blocks));

            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, Patch patch) {
                if (patch.positions.isPresent()) {
                    buf.writeBoolean(true);
                    buf.writeInt(patch.minX);
                    buf.writeInt(patch.maxX);
                    buf.writeInt(patch.minZ);
                    Int2ObjectArrayMap<byte[]> positions = patch.positions.get();
                    buf.writeVarInt(positions.size());
                    for (var entry : positions.int2ObjectEntrySet()) {
                        buf.writeVarInt(entry.getIntKey());
                        byte[] rowData = entry.getValue();
                        buf.writeByteArray(rowData);
                    }
                } else buf.writeBoolean(false);

                if (patch.biomes.isPresent()) {
                    buf.writeBoolean(true);
                    List<Holder<Biome>> biomes = patch.biomes.get();
                    buf.writeVarInt(biomes.size());
                    for (var biome : biomes) {
                        int id = getBiomeId(biome, buf.registryAccess());
                        buf.writeVarInt(id);
                    }
                } else buf.writeBoolean(false);

                if (patch.blocks.isPresent()) {
                    buf.writeBoolean(true);
                    List<Block> blocks = patch.blocks.get();
                    buf.writeVarInt(blocks.size());
                    for (var block : blocks) {
                        int id = getBlockId(block);
                        buf.writeVarInt(id);
                    }
                } else buf.writeBoolean(false);
            }
        };
    }

}