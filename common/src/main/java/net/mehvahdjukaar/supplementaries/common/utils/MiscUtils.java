package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.TetraCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.function.Supplier;

public class MiscUtils {


    public static boolean showsHints(BlockGetter worldIn, TooltipFlag flagIn) {
        if (worldIn instanceof Level l && l.isClientSide) {
            return ClientConfigs.General.TOOLTIP_HINTS.get();
        }
        return false;
    }

    @Nullable
    public static Entity cloneEntity(Entity entity, ServerLevel level) {
        CompoundTag c = new CompoundTag();
        entity.save(c);
        var opt = EntityType.create(c, level); // create new to reset level properly
        return opt.orElse(null);
    }

    public enum Festivity {
        NONE,
        HALLOWEEN,
        APRILS_FOOL,
        CHRISTMAS,
        EARTH_DAY,
        ST_VALENTINE,
        MY_BIRTHDAY,
        MOD_BIRTHDAY;

        public boolean isHalloween() {
            return this == HALLOWEEN;
        }

        public boolean isAprilsFool() {
            return this == APRILS_FOOL;
        }

        public boolean isStValentine() {
            return this == ST_VALENTINE;
        }

        public boolean isChristmas() {
            return this == CHRISTMAS;
        }

        public boolean isEarthDay() {
            return this == EARTH_DAY;
        }

        public boolean isBirthday() {
            return this == MOD_BIRTHDAY || this == MY_BIRTHDAY;
        }

        public float getCandyWrappingIndex() {
            return switch (this) {
                case HALLOWEEN -> 0.5f;
                case CHRISTMAS -> 1;
                default -> 0;
            };
        }

        private static Festivity get() {
            if (PlatHelper.getPhysicalSide().isClient() && ClientConfigs.General.UNFUNNY.get()) {
                return NONE;
            }
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DATE);
            if ((month == Calendar.OCTOBER && date >= 29) || (month == Calendar.NOVEMBER && date == 1))
                return HALLOWEEN;
            if (month == Calendar.APRIL && date == 1) return APRILS_FOOL;
            if (month == Calendar.FEBRUARY && date == 14) return ST_VALENTINE;
            if (month == Calendar.APRIL && date == 22) return EARTH_DAY;
            if (month == Calendar.DECEMBER && date >= 20) return CHRISTMAS;
            if (month == Calendar.FEBRUARY && date == 7) return MY_BIRTHDAY;
            if (month == Calendar.OCTOBER && date == 9) return MOD_BIRTHDAY;
            return NONE;
        }
    }

    public static final Festivity FESTIVITY = Festivity.get();

    public static boolean isSword(Item i) {
        if (i.builtInRegistryHolder().is(ModTags.STATUE_SWORDS)) return true;
        if (CompatHandler.TETRA && TetraCompat.isTetraSword(i)) return true;
        return i instanceof SwordItem;
    }

    public static boolean isTool(Item i) {
        if (i.builtInRegistryHolder().is(ModTags.STATUE_TOOLS)) return true;
        if (CompatHandler.TETRA && TetraCompat.isTetraTool(i)) return true;
        return i instanceof DiggerItem || i instanceof TridentItem;
    }

    //bounding box
    public static AABB getDirectionBB(BlockPos pos, Direction facing, int offset) {
        BlockPos endPos = pos.relative(facing, offset);
        switch (facing) {
            case NORTH -> endPos = endPos.offset(1, 1, 0);
            case SOUTH -> {
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(0, 0, 1);
            }
            case UP -> {
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(0, 1, 0);
            }
            case EAST -> {
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(1, 0, 0);
            }
            case WEST -> endPos = endPos.offset(0, 1, 1);
            case DOWN -> endPos = endPos.offset(1, 0, 1);
        }
        return new AABB(pos, endPos);
    }

    //this is how you do it :D
    private static final Supplier<ShulkerBoxBlockEntity> SHULKER_TILE =
            Suppliers.memoize(() -> new ShulkerBoxBlockEntity(BlockPos.ZERO, Blocks.SHULKER_BOX.defaultBlockState()));

    public static boolean isAllowedInShulker(ItemStack stack, Level level) {
        var te = SHULKER_TILE.get();
        te.setLevel(level);
        boolean first = te.canPlaceItemThroughFace(0, stack, null);
        te.setLevel(null);
        //also check if its container item. Shulker is super inconsistent here. block checks instanceof, gui checks canfitinsidecontainer
        return first && stack.getItem().canFitInsideContainerItems();
    }

    //cylinder distance
    public static boolean withinDistanceDown(BlockPos pos, Vec3 vector, double distW, double distDown) {
        double dx = vector.x() - (pos.getX() + 0.5);
        double dy = vector.y() - (pos.getY() + 0.5);
        double dz = vector.z() - (pos.getZ() + 0.5);
        double myDistW = (dx * dx + dz * dz);
        return (myDistW < (distW * distW) && (dy < distW && dy > -distDown));
    }

    // vanilla is wont allow to tick a block that already has a scheduled tick, even if at an earlier time
    public static void scheduleTickOverridingExisting(ServerLevel level, BlockPos pos, Block block, int delay) {
        var tick = new ScheduledTick<>(block, pos, level.getGameTime() + (long) delay, level.nextSubTickCount());

        long l = ChunkPos.asLong(tick.pos());

        var container = level.getBlockTicks().allContainers.get(l);
        container.removeIf(t -> t.pos().equals(tick.pos()) && t.type() == tick.type());
        container.schedule(tick);
    }

}
