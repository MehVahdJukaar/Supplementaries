package net.mehvahdjukaar.supplementaries.common.misc.map_data;

import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DepthDataHandler {
    private static final String DEPTH_LOCK_KEY = "depth_lock";

    public static void init() {
    }

    public static final CustomMapData.Type<Optional<Integer>, DepthMapData> DEPTH_DATA_KEY = MapDataRegistry.registerCustomMapSavedData(
            Supplementaries.res(DEPTH_LOCK_KEY), DepthMapData::new,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT)
    );

    public static Optional<Integer> getMapHeight(MapItemSavedData data) {
        return DEPTH_DATA_KEY.get(data).getValue();
    }

    public static ItemStack createSliceMap(Level level, int x, int z, byte scale, boolean trackingPosition, boolean unlimitedTracking,
                                           int slice) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        MapItemSavedData data = MapItemSavedData.createFresh(x, z, scale, trackingPosition, unlimitedTracking, level.dimension());
        DepthMapData instance = DEPTH_DATA_KEY.get(data);
        instance.set(slice);
        instance.setDirty(data, CustomMapData.SimpleDirtyCounter::markDirty);
        MapId mapId = level.getFreeMapId();
        level.setMapData(mapId, data);
        itemStack.set(DataComponents.MAP_ID, mapId);
        return itemStack;
    }

    public static MapColor getCutoffColor(BlockPos pos, BlockGetter level) {
        /*for(Direction d : Direction.Plane.HORIZONTAL){
            BlockPos p = pos.relative(d);
            if(level.getBlockState(p).getMapColor(level, pos) == MapColor.NONE){
               // return WeatheredMap.ANTIQUE_LIGHT;
            }
        }*/
        return (pos.getX() + pos.getZ()) % 2 == 0 ? MapColor.NONE : WeatheredHandler.ANTIQUE_LIGHT;
    }

    public static double getRangeMultiplier() {
        return CommonConfigs.Tools.SLICE_MAP_RANGE.get();
    }

    private static final RandomSource RAND = RandomSource.createNewThreadLocalInstance();

    public static boolean canPlayerSee(int targetY, Entity entity) {
        Level level = entity.level();
        int py = entity.getBlockY();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int spread = 3;
        p.set(entity.blockPosition().offset(RAND.nextInt(spread) - RAND.nextInt(spread),
                0, RAND.nextInt(spread) - RAND.nextInt(spread)));

        int direction = Integer.compare(targetY, py);

        while (p.getY() != targetY) {
            if (level.getBlockState(p).getMapColor(level, p) != MapColor.NONE) {
                return false;
            }
            p.setY(p.getY() + direction);
        }
        return true;
    }

    public static class DepthMapData extends CustomMapData.Simple<Optional<Integer>> {

        public DepthMapData() {
            this.value = Optional.empty();
        }

        @NotNull
        public Optional<Integer> getValue() {
            return this.value;
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider provider) {
            if (tag.contains(DEPTH_LOCK_KEY)) {
                int anInt = tag.getInt(DEPTH_LOCK_KEY);
                if (anInt != Integer.MAX_VALUE) {
                    this.value = Optional.of(anInt);
                } else this.value = Optional.empty();
            } else this.value = Optional.empty();
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider provider) {
            this.value.ifPresent(integer -> tag.putInt(DEPTH_LOCK_KEY, integer));
        }

        @Override
        public Type<Optional<Integer>, DepthMapData> getType() {
            return DEPTH_DATA_KEY;
        }

        @Override
        public @Nullable Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
            return value.map(integer -> Component.translatable("filled_map.sliced.tooltip", integer)
                    .withStyle(ChatFormatting.GRAY)).orElse(null);
        }

        public void set(int slice) {
            this.value = Optional.of(slice);
        }

        @Override
        public SimpleDirtyCounter createDirtyCounter() {
            return new SimpleDirtyCounter();
        }
    }

}
