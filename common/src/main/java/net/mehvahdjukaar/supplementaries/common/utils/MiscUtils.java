package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.TetraCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.Calendar;
import java.util.function.Supplier;

public class MiscUtils {


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
                default -> 0;
                case HALLOWEEN -> 0.5f;
                case CHRISTMAS -> 1;
            };
        }

        private static Festivity get() {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DATE);
            if ((month == Calendar.OCTOBER && date >= 29) || (month == Calendar.NOVEMBER && date <= 1))
                return HALLOWEEN;
            if (month == Calendar.APRIL && date == 1) return APRILS_FOOL;
            if (month == Calendar.FEBRUARY && date == 14) return ST_VALENTINE;
            if (month == Calendar.APRIL && date == 22) return EARTH_DAY;
            if (month == Calendar.DECEMBER && date >= 23) return CHRISTMAS;
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

    public static boolean isAllowedInShulker(ItemStack stack) {
        var te = SHULKER_TILE.get();
        Level = PlatHelper.getCurrentServer();
        te.setLevel(level);
        var r = te.canPlaceItemThroughFace(0, stack, null);
        te.setLevel(null);
        return r;
    }

    //cylinder distance
    public static boolean withinDistanceDown(BlockPos pos, Vec3 vector, double distW, double distDown) {
        double dx = vector.x() - (pos.getX() + 0.5);
        double dy = vector.y() - (pos.getY() + 0.5);
        double dz = vector.z() - (pos.getZ() + 0.5);
        double myDistW = (dx * dx + dz * dz);
        return (myDistW < (distW * distW) && (dy < distW && dy > -distDown));
    }

    public static BlockState readBlockState(CompoundTag compound, @Nullable Level level) {
        HolderGetter<Block> holderGetter = level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
        return NbtUtils.readBlockState(holderGetter, compound);
    }


}
