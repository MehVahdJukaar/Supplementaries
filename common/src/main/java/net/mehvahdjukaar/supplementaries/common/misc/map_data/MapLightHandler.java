package net.mehvahdjukaar.supplementaries.common.misc.map_data;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAmbientLightPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


// Experimental. Can be activated by mods.

public class MapLightHandler {

    private static boolean enabled = false;

    public static void init() {
    }

    // call to activate light on maps
    public static void setActive(boolean on) {
        enabled = on;
    }

    public static final CustomMapData.Type<Patch, LightData> LIGHT_DATA =
            MapDataRegistry.registerCustomMapSavedData(Supplementaries.res("light_data"), LightData::new,
                    Patch.STREAM_CODEC);

    public static LightData getLightData(MapItemSavedData data) {
        return LIGHT_DATA.get(data);
    }

    public static boolean isActive() {
        return enabled;
    }


    public static class Counter implements CustomMapData.DirtyCounter {
        private int minDirtyX = 0;
        private int maxDirtyX = 127;
        private int minDirtyZ = 0;
        private int maxDirtyZ = 127;
        private boolean posDirty = true;

        public void markDirty(int x, int z) {
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
            return posDirty;
        }

        @Override
        public void clearDirty() {
            this.posDirty = false;
            this.minDirtyX = 0;
            this.minDirtyZ = 0;
            this.maxDirtyX = 0;
            this.maxDirtyZ = 0;
        }

    }

    public static class LightData implements CustomMapData<Counter, Patch> {

        private static final String LIGHTMAP_TAG = "lightmap";
        public static final String MIN_X = "min_x";
        public static final String MAX_X = "max_x";
        public static final String MIN_Z = "min_z";
        private byte[][] data = null;

        private int getEntry(int x, int z) {
            if (data == null) return 0;
            if (x < 0 || x >= 128 || z < 0 || z >= 128) {
                return 0; //error
            }
            if (data[x] != null) {
                return Byte.toUnsignedInt(data[x][z]);
            }
            return 0;
        }

        private void addEntry(MapItemSavedData md, int x, int z, int packedLight) {
            if (data == null) {
                data = new byte[128][];
            }
            if (data[x] == null) data[x] = new byte[128];
            data[x][z] = (byte) (packedLight);
            this.setDirty(md, counter -> counter.markDirty(x, z));
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider lookup) {
            if (tag.contains(LIGHTMAP_TAG)) {
                CompoundTag t = tag.getCompound(LIGHTMAP_TAG);
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
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider lookup) {
            // save all
            if (data != null) {
                CompoundTag t = new CompoundTag();
                for (int x = 0; x <= 127; x++) {
                    if (data[x] != null) {
                        byte[] rowData = new byte[127 - 0 + 1];

                        System.arraycopy(data[x], 0, rowData, 0, rowData.length);
                        t.putByteArray("pos_" + x, rowData);
                    }
                }
                tag.put(LIGHTMAP_TAG, t);
            }
        }

        @Override
        public void applyUpdatePatch(Patch patch) {
            int minX = patch.minX;
            int maxX = patch.maxX;
            int minZ = patch.minZ;

            for (int x = minX; x < maxX; x++) {
                byte[] rowData = patch.lights.get(x);

                if (data == null) {
                    data = new byte[128][];
                }
                if (data[x] == null) {
                    data[x] = new byte[128];
                }
                System.arraycopy(rowData, 0, data[x], minZ, rowData.length);
            }
        }

        @Override
        public Patch createUpdatePatch(Counter counter) {
            int minX = counter.minDirtyX;
            int maxX = counter.maxDirtyX;
            int minZ = counter.minDirtyZ;
            Int2ObjectArrayMap<byte[]> lights = new Int2ObjectArrayMap<>();
            if (data != null) {
                for (int x = minX; x <= maxX; x++) {
                    if (data[x] != null) {
                        lights.put(x, data[x]);
                    }
                }
            } else {
                minZ = 0;
                maxX = 0;
                minX = 0;
            }
            return new Patch(minX, maxX, minZ, lights);
        }

        @Override
        public boolean persistOnCopyOrLock() {
            return false;
        }

        @Override
        public Type<Patch, ?> getType() {
            return LIGHT_DATA;
        }

        @Override
        public Counter createDirtyCounter() {
            return new Counter();
        }

        public void setLightLevel(int x, int z, int blockLight, int skyLight, MapItemSavedData data) {
            int packed = (blockLight << 4) | (15 - skyLight);
            if (packed != 0) {
                //dither biomes
                if (!Objects.equals(this.getEntry(x, z), packed)) {
                    this.addEntry(data, x, z, packed);
                }
            } else {
                //remove unneded stufff
                if (this.data != null && this.data[x] != null && this.data[x][z] != 0) {
                    this.data[x][z] = 0;
                    this.setDirty(data, counter -> counter.markDirty(x, z));
                    for (var b : this.data[x]) {
                        if (b != 0) return;
                    }
                    this.data[x] = null;
                }
            }
        }


        @Environment(EnvType.CLIENT)
        public void processTexture(NativeImage texture, int startX, int startY, ResourceKey<Level> levelKey) {
            if (lightMap == null) return;
            int minL = LIGHT_PER_WORLD.getOrDefault(levelKey, 0);
            for (int x = 0; x < 128; ++x) {
                for (int z = 0; z < 128; ++z) {
                    int light = getEntry(x, z);
                    //  if (light == 0) continue;

                    int skyDarkness = light & 0b1111; // Extract the lower 4 bits
                    int blockLight = Math.max(minL, (light >> 4) & 0b1111); // Extract the higher 4 bits

                    int pX = startX + x;
                    int pY = startY + z;
                    int originalColor = texture.getPixelRGBA(pX, pY);

                    int skyLight = 15 - skyDarkness;

                    var lightColor = new RGBColor(((NativeImage) lightMap).getPixelRGBA(blockLight, skyLight));
                    float intensity = 1;
                    int newColor = new RGBColor(originalColor).multiply(
                            (lightColor.red() * intensity),
                            (lightColor.green() * intensity),
                            (lightColor.green() * intensity),
                            1).toInt();

                    texture.setPixelRGBA(pX, pY, newColor);
                }
            }
        }

        public void clear() {
            data = null;
        }
    }

    public record Patch(int minX, int maxX, int minZ, Int2ObjectArrayMap<byte[]> lights) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Patch> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public Patch decode(RegistryFriendlyByteBuf buf) {
                int minX = buf.readInt();
                int maxX = buf.readInt();
                int minZ = buf.readInt();
                int size = buf.readVarInt();
                Int2ObjectArrayMap<byte[]> positions = new Int2ObjectArrayMap<>(size);
                for (int i = 0; i < size; i++) {
                    int x = buf.readVarInt();
                    byte[] rowData = buf.readByteArray();
                    positions.put(x, rowData);
                }
                return new Patch(minX, maxX, minZ, positions);

            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, Patch patch) {
                buf.writeInt(patch.minX);
                buf.writeInt(patch.maxX);
                buf.writeInt(patch.minZ);
                buf.writeVarInt(patch.lights.size());
                for (var entry : patch.lights.int2ObjectEntrySet()) {
                    buf.writeVarInt(entry.getIntKey());
                    byte[] rowData = entry.getValue();
                    buf.writeByteArray(rowData);
                }
            }
        };
    }

    private static final Object2IntMap<ResourceKey<Level>> LIGHT_PER_WORLD = new Object2IntArrayMap<>();


    @Nullable
    private static Object lightMap = null;

    // Call to set lightmap. Has to be 16x16
    @Environment(EnvType.CLIENT)
    public static void setLightMap(@Nullable NativeImage map) {
        if (map != null) {
            Preconditions.checkArgument(map.getWidth() != 16 || map.getHeight() != 6, "Lightmap must be 16x16");
        }
        lightMap = map;
    }

    @ApiStatus.Internal
    public static void setAmbientLight(Object2IntMap<ResourceKey<Level>> ambientLight) {
        LIGHT_PER_WORLD.clear();
        LIGHT_PER_WORLD.putAll(ambientLight);
    }

    @ApiStatus.Internal
    public static void sendDataToClient(ServerPlayer player) {
        if (enabled) {
            NetworkHelper.sendToClientPlayer(player, new ClientBoundSyncAmbientLightPacket(player.level().registryAccess()));
        }
    }

}