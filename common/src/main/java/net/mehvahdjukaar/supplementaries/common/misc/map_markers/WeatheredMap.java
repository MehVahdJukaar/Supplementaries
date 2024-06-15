package net.mehvahdjukaar.supplementaries.common.misc.map_markers;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.SliceMapItem;
import net.mehvahdjukaar.supplementaries.common.misc.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.common.misc.MapLightHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class WeatheredMap {

    private static final String ANTIQUE_KEY = "antique";

    private static final CustomMapData.Type<WeatheredMapData> ANTIQUE_DATA_KEY = MapDataRegistry.registerCustomMapSavedData(
            Supplementaries.res(ANTIQUE_KEY), WeatheredMapData::new
    );

    public static void init() {
    }

    public static WeatheredMapData getAntiqueData(MapItemSavedData data) {
        return ANTIQUE_DATA_KEY.get(data);
    }

    public static class WeatheredMapData implements CustomMapData<CustomMapData.SimpleDirtyCounter> {
        private boolean antique = false;

        public boolean isAntique() {
            return antique;
        }

        @Override
        public void load(CompoundTag tag) {
            if (tag.contains(ANTIQUE_KEY)) {
                antique = tag.getBoolean(ANTIQUE_KEY);
            } else antique = false;
        }

        @Override
        public void loadUpdateTag(CompoundTag tag) {
            if (tag.contains(ANTIQUE_KEY)) {
                antique = tag.getBoolean(ANTIQUE_KEY);
            }
        }

        @Override
        public void save(CompoundTag tag) {
            if (antique) tag.putBoolean(ANTIQUE_KEY, true);
        }

        @Override
        public void saveToUpdateTag(CompoundTag tag, SimpleDirtyCounter dirtyCounter) {
            tag.putBoolean(ANTIQUE_KEY, antique);
        }

        @Override
        public Type<WeatheredMapData> getType() {
            return ANTIQUE_DATA_KEY;
        }

        @Override
        public @Nullable Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
            if (antique) {
                return Component.translatable("filled_map.antique.tooltip").withStyle(ChatFormatting.GRAY);
            }
            return null;
        }

        @Override
        public SimpleDirtyCounter createDirtyCounter() {
            return new SimpleDirtyCounter();
        }

        // true if it should cancel the normal update
        @Override
        public boolean onItemUpdate(MapItemSavedData data, Entity entity) {
            if (!antique) return false;
            Level level = entity.level();

            if (!(level.dimension() == data.dimension && entity instanceof Player pl)) return true;

            int minHeight = SliceMapItem.getMapHeight(data);

            boolean hasDepthLock = minHeight != Integer.MAX_VALUE;
            if (hasDepthLock && !SliceMapItem.canPlayerSee(minHeight, pl)) {
                return true;
            }

            int scale = 1 << data.scale;
            int mapX = data.centerX;
            int mapZ = data.centerZ;
            int playerX = Mth.floor(entity.getX() - mapX) / scale + 64;
            int playerZ = Mth.floor(entity.getZ() - mapZ) / scale + 64;
            int range = 128 / scale;
            if (hasDepthLock) {
                range = (int) (range * SliceMapItem.getRangeMultiplier());
            }
            boolean hasCeiling = isHasCeiling(level, minHeight);
            if (hasCeiling) {
                range /= 2;
            }

            MapItemSavedData.HoldingPlayer player = data.getHoldingPlayer((Player) entity);

            boolean hasChangedAColorThisZ = false;


            for (int pixelX = playerX - range + 1; pixelX < playerX + range; ++pixelX) {
                if ((pixelX & 15) == (player.step & 15) || hasChangedAColorThisZ) {
                    hasChangedAColorThisZ = false;
                    double somethingY = 0.0D;

                    for (int pixelZ = playerZ - range - 1; pixelZ < playerZ + range; ++pixelZ) {
                        if (pixelX >= 0 && pixelZ >= -1 && pixelX < 128 && pixelZ < 128) {
                            int offsetX = pixelX - playerX;
                            int offsetZ = pixelZ - playerZ;
                            int dist = offsetX * offsetX + offsetZ * offsetZ;
                            boolean maxRadius = dist > range * range;
                            if (maxRadius) continue;

                            boolean innerRadius = dist > (range - 2) * (range - 2);
                            int worldX = (mapX / scale + pixelX - 64) * scale;
                            int worldZ = (mapZ / scale + pixelZ - 64) * scale;
                            Multiset<MapColor> multiset = LinkedHashMultiset.create();
                            var levelchunk = level.getChunk(SectionPos.blockToSectionCoord(worldX), SectionPos.blockToSectionCoord(worldZ),
                                    ChunkStatus.FULL, false);

                            if (levelchunk instanceof LevelChunk lc && !lc.isEmpty()) {
                                ChunkPos chunkpos = levelchunk.getPos();
                                int chunkCoordX = worldX & 15;
                                int chunkCoordZ = worldZ & 15;

                                double maxY = 0.0D;

                                int distanceFromLand = 8;
                                HashMap<BlockPos, Boolean> isWaterMap = new HashMap<>();

                                if (hasCeiling) {
                                    int l3 = worldX + worldZ * 231871;
                                    l3 = l3 * l3 * 31287121 + l3 * 11;
                                    if ((l3 >> 20 & 1) == 0) {
                                        multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                                    } else {
                                        multiset.add(Blocks.BROWN_WOOL.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                                    }

                                    maxY = 100.0D;
                                    distanceFromLand = 0;

                                } else {
                                    BlockPos.MutableBlockPos mutable1 = new BlockPos.MutableBlockPos();

                                    if (isWaterAt(level, isWaterMap, scale, worldX - scale, worldZ - scale))
                                        --distanceFromLand;
                                    if (isWaterAt(level, isWaterMap, scale, worldX - scale, worldZ))
                                        --distanceFromLand;
                                    if (isWaterAt(level, isWaterMap, scale, worldX - scale, worldZ + scale))
                                        --distanceFromLand;
                                    if (isWaterAt(level, isWaterMap, scale, worldX + scale, worldZ - scale))
                                        --distanceFromLand;
                                    if (isWaterAt(level, isWaterMap, scale, worldX + scale, worldZ))
                                        --distanceFromLand;
                                    if (isWaterAt(level, isWaterMap, scale, worldX + scale, worldZ + scale))
                                        --distanceFromLand;
                                    if (isWaterAt(level, isWaterMap, scale, worldX, worldZ - scale))
                                        --distanceFromLand;
                                    if (isWaterAt(level, isWaterMap, scale, worldX, worldZ + scale))
                                        --distanceFromLand;


                                    for (int scaleOffsetX = 0; scaleOffsetX < scale; ++scaleOffsetX) {
                                        for (int scaleOffsetZ = 0; scaleOffsetZ < scale; ++scaleOffsetZ) {
                                            int cY = Math.min(minHeight,
                                                    levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, scaleOffsetX + chunkCoordX, scaleOffsetZ + chunkCoordZ) + 1);
                                            BlockState blockState;
                                            MapColor newColor = null;

                                            if (cY <= level.getMinBuildHeight() + 1) {
                                                newColor = Blocks.BEDROCK.defaultBlockState().getMapColor(level, mutable1);
                                            } else {


                                                //get first non empty map color below chunk y
                                                MapColor temp;
                                                do {
                                                    --cY;
                                                    mutable1.set(chunkpos.getMinBlockX() + scaleOffsetX + chunkCoordX, cY, chunkpos.getMinBlockZ() + scaleOffsetZ + chunkCoordZ);
                                                    blockState = levelchunk.getBlockState(mutable1);
                                                    temp = blockState.getMapColor(level, mutable1);
                                                    if (temp != MapColor.NONE && temp != MapColor.WATER && blockState.getCollisionShape(level, mutable1).isEmpty()) {
                                                        newColor = MapColor.GRASS;
                                                        //temp = MapColor.NONE;
                                                    }
                                                } while (temp == MapColor.NONE && cY > level.getMinBuildHeight());

                                                if (newColor == null) {
                                                    newColor = blockState.getMapColor(level, mutable1);
                                                }
                                            }
                                            //add deco here
                                            data.checkBanners(level, chunkpos.getMinBlockX() + scaleOffsetX + chunkCoordX, chunkpos.getMinBlockZ() + scaleOffsetZ + chunkCoordZ);
                                            maxY += (double) cY / (double) (scale * scale);

                                            if (cY >= minHeight) {
                                                newColor = SliceMapItem.getCutoffColor(mutable1, levelchunk);
                                            }

                                            multiset.add(newColor);
                                        }
                                    }
                                }

                                int relativeShade = 1;


                                MapColor mc = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.NONE);
                                if (mc == MapColor.WATER) {


                                    mc = MapColor.COLOR_ORANGE;
                                    if (distanceFromLand > 7 && pixelZ % 2 == 0) {
                                        relativeShade = (pixelX + (int) (Mth.sin(pixelZ + 0.0F) * 7.0F)) / 8 % 5;
                                        if (relativeShade == 3) {
                                            relativeShade = 1;
                                        } else if (relativeShade == 4) {
                                            relativeShade = 0;
                                        }
                                    } else if (distanceFromLand > 7) {
                                        mc = ANTIQUE_LIGHT;
                                        relativeShade = 2;
                                    } else if (distanceFromLand > 5) {
                                        relativeShade = 1;
                                    } else if (distanceFromLand > 3) {
                                        relativeShade = 0;
                                    }


                                } else {

                                    if (distanceFromLand > 0) {
                                        relativeShade = 3;
                                        mc = MapColor.COLOR_BROWN;
                                        if (distanceFromLand > 3) {
                                            relativeShade = 1;
                                        }
                                    } else {
                                        double depthY = (maxY - somethingY) * 4.0D / (scale + 4) + ((pixelX + pixelZ & 1) - 0.5D) * 0.4D;

                                        if (depthY > 0.6D) {
                                            relativeShade = 2;
                                        } else if (depthY < -0.6D) {
                                            relativeShade = 0;
                                        }

                                        mc = ANTIQUE_COLORS.getOrDefault(mc, ANTIQUE_DARK);
                                    }
                                }
                                //if(MapColor == MapColor.WATER)

                                somethingY = maxY;


                                if (pixelZ >= 0 && (!innerRadius || (pixelX + pixelZ & 1) != 0)) {
                                    hasChangedAColorThisZ |= data.updateColor(pixelX, pixelZ, (byte) (mc.id * 4 + relativeShade));
                                }
                            }
                        }
                    }
                }
            }

            ++player.step;
            return true;
        }

        private static boolean isHasCeiling(Level level, int mapHeight) {
            boolean original = level.dimensionType().hasCeiling();
            if (original && mapHeight != Integer.MAX_VALUE && CommonConfigs.Tools.SLICE_MAP_ENABLED.get()) {
                return false;
            }
            return original;
        }


        public void set(boolean on) {
            this.antique = on;
        }

        private static boolean isWaterAt(Level level, Map<BlockPos, Boolean> map, int scale, int x, int z) {
            BlockPos pos = new BlockPos(x, 0, z);
            return map.computeIfAbsent(pos, p -> {
                        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                        return level.getFluidState(pos.above(y)).isEmpty();
                    }
            );
        }

    }

    public static final MapColor ANTIQUE_LIGHT;
    public static final MapColor ANTIQUE_DARK;
    private static final Object2ObjectArrayMap<MapColor, MapColor> ANTIQUE_COLORS = new Object2ObjectArrayMap<>();

    static {
        MapColor mc1;
        MapColor mc;
        try {
            Class<MapColor> cl = MapColor.class;

            Constructor<MapColor> cons = cl.getDeclaredConstructor(int.class, int.class);
            cons.setAccessible(true);

            mc = cons.newInstance(62, 0xd3a471);
            mc1 = cons.newInstance(63, 0xa77e52);
        } catch (Exception e) {
            mc = MapColor.TERRACOTTA_WHITE;
            mc1 = MapColor.RAW_IRON;
            Supplementaries.LOGGER.warn("Failed to add custom map colors for antique map: ", e);
        }
        ANTIQUE_DARK = mc1;
        ANTIQUE_LIGHT = mc;
        ANTIQUE_COLORS.put(MapColor.STONE, MapColor.DIRT);
        ANTIQUE_COLORS.put(MapColor.DEEPSLATE, MapColor.DIRT);
        ANTIQUE_COLORS.put(MapColor.PLANT, MapColor.COLOR_BROWN);
        ANTIQUE_COLORS.put(MapColor.DIRT, ANTIQUE_LIGHT);
        ANTIQUE_COLORS.put(MapColor.WOOD, MapColor.WOOD);
        ANTIQUE_COLORS.put(MapColor.COLOR_GRAY, MapColor.COLOR_BROWN);
        ANTIQUE_COLORS.put(MapColor.TERRACOTTA_BLACK, MapColor.TERRACOTTA_BLACK);
        ANTIQUE_COLORS.put(MapColor.COLOR_BLACK, MapColor.TERRACOTTA_BLACK);
        ANTIQUE_COLORS.put(MapColor.SAND, ANTIQUE_LIGHT);
        ANTIQUE_COLORS.put(MapColor.QUARTZ, ANTIQUE_LIGHT);
        ANTIQUE_COLORS.put(MapColor.SNOW, ANTIQUE_LIGHT);
        ANTIQUE_COLORS.put(MapColor.METAL, ANTIQUE_LIGHT);
        ANTIQUE_COLORS.put(MapColor.WOOL, ANTIQUE_LIGHT);
        ANTIQUE_COLORS.put(MapColor.COLOR_BROWN, MapColor.TERRACOTTA_BROWN);
    }

    public static void setAntique(Level level, ItemStack stack, boolean on) {
        setAntique(level, stack, on, false);
    }

    public static void setAntique(Level level, ItemStack stack, boolean on, boolean replaceOld) {
        MapItemSavedData mapitemsaveddata = MapItem.getSavedData(stack, level);
        Integer mapId = createAntiqueMapData(mapitemsaveddata, level, on, replaceOld);
        if (mapId != null) stack.getOrCreateTag().putInt("map", mapId);

    }

    public static Integer createAntiqueMapData(MapItemSavedData mapitemsaveddata, Level level, boolean on, boolean replaceOld) {
        if (mapitemsaveddata instanceof ExpandedMapData data) {

            MapItemSavedData newData = replaceOld ? mapitemsaveddata : data.copy();
            WeatheredMapData instance = getAntiqueData(newData);
            var colorData = ColoredMapHandler.getColorData(newData);
            colorData.clear();
            var lightData = MapLightHandler.getLightData(newData);
            lightData.clear();

            instance.set(on);
            instance.setDirty(newData, CustomMapData.SimpleDirtyCounter::markDirty);
            if (!replaceOld) {
                int mapId = level.getFreeMapId();
                String mapKey = MapItem.makeKey(mapId);

                level.setMapData(mapKey, newData);
                return mapId;
            }
        }
        return null;
    }
}
