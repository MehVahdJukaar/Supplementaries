package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.WeatheredMap;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

import static net.minecraft.world.item.MapItem.makeKey;

public class SliceMapItem extends EmptyMapItem {

    public SliceMapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(itemStack);
        } else {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            player.level().playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);

            int slice = (int) player.getY() + 1;
            ItemStack itemStack2 = SliceMapItem.createSliced(level, player.getBlockX(), player.getBlockZ(), (byte) 0,
                    true, false, slice);
            if (itemStack.isEmpty()) {
                return InteractionResultHolder.consume(itemStack2);
            } else {
                if (!player.getInventory().add(itemStack2.copy())) {
                    player.drop(itemStack2, false);
                }

                return InteractionResultHolder.consume(itemStack);
            }
        }
    }

    public static ItemStack createSliced(Level level, int x, int z, byte scale, boolean trackingPosition, boolean unlimitedTracking,
                                         int slice) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        MapItemSavedData data = MapItemSavedData.createFresh(x, z, scale, trackingPosition, unlimitedTracking, level.dimension());
        DepthMapData instance = DEPTH_DATA_KEY.getOrCreate(data, DepthMapData::new);
        instance.set(slice);
        instance.setDirty(data);
        int mapId = level.getFreeMapId();
        level.setMapData(makeKey(mapId), data);
        itemStack.getOrCreateTag().putInt("map", mapId);
        return itemStack;
    }

    private static final String DEPTH_LOCK_KEY = "depth_lock";

    public static void init() {
    }

    public static final CustomMapData.Type<DepthMapData> DEPTH_DATA_KEY = MapDecorationRegistry.registerCustomMapSavedData(
            Supplementaries.res(DEPTH_LOCK_KEY), DepthMapData::new
    );


    public static int getMapHeight(MapItemSavedData data) {
        DepthMapData depth = DEPTH_DATA_KEY.get(data);
        return depth == null ? Integer.MAX_VALUE : depth.height;
    }

    public static MapColor getCutoffColor(BlockPos pos, BlockGetter level) {
        /*for(Direction d : Direction.Plane.HORIZONTAL){
            BlockPos p = pos.relative(d);
            if(level.getBlockState(p).getMapColor(level, pos) == MapColor.NONE){
               // return WeatheredMap.ANTIQUE_LIGHT;
            }
        }*/
        return (pos.getX() + pos.getZ()) % 2 == 0 ? MapColor.NONE : WeatheredMap.ANTIQUE_LIGHT;
    }

    public static double getRangeMultiplier() {
        return CommonConfigs.Tools.SLICE_MAP_RANGE.get();
    }

    public static boolean canPlayerSee(int targetY, Entity entity) {
        Level level = entity.level();
        int py = entity.getBlockY();
        RandomSource rand = level.getRandom();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int spread = 3;
        p.set(entity.blockPosition().offset(rand.nextInt(spread) - rand.nextInt(spread),
                0, rand.nextInt(spread) - rand.nextInt(spread)));

        int direction = Integer.compare(targetY, py);

        while (p.getY() != targetY) {
            if (level.getBlockState(p).getMapColor(level, p) != MapColor.NONE) {
                return false;
            }
            p.setY(p.getY() + direction);
        }
        return true;
    }

    private static class DepthMapData implements CustomMapData {

        private int height;

        public DepthMapData(CompoundTag tag) {
            if (tag.contains(DEPTH_LOCK_KEY)) {
                this.height = tag.getInt(DEPTH_LOCK_KEY);
            } else this.height = Integer.MAX_VALUE;
        }

        public DepthMapData() {
        }

        @Override
        public Type<DepthMapData> getType() {
            return DEPTH_DATA_KEY;
        }

        @Override
        public void save(CompoundTag tag) {
            if (height != Integer.MAX_VALUE) tag.putInt(DEPTH_LOCK_KEY, height);
        }

        @Override
        public @Nullable Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
            if (height == Integer.MAX_VALUE) return null;
            return Component.translatable("filled_map.sliced.tooltip", height).withStyle(ChatFormatting.GRAY);
        }

        public void set(int slice) {
            this.height = slice;
        }
    }
}
