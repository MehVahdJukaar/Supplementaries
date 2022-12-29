package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
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

    public static boolean isLanternBlock(Block b) {
        ResourceLocation id = Utils.getID(b);
        String namespace = id.getNamespace();
        if (namespace.equals("skinnedlanterns") || (namespace.equals("twigs") && id.getPath().contains("paper_lantern")))
            return true;
        if (b instanceof LanternBlock) { //!CommonConfigs.Tweaks.WALL_LANTERN_BLACKLIST.get().contains(namespace)
            return !b.defaultBlockState().hasBlockEntity() || b instanceof LightableLanternBlock;
        }
        return false;
    }

    public static boolean isSword(Item i) {
        if (i.builtInRegistryHolder().is(ModTags.STATUE_SWORDS)) return true;
        //if (CompatHandler.tetra && TetraToolHelper.isTetraSword(i)) return true;
        return i instanceof SwordItem;
    }

    public static boolean isTool(Item i) {
        if (i.builtInRegistryHolder().is(ModTags.STATUE_TOOLS)) return true;
        //if (CompatHandler.tetra && TetraToolHelper.isTetraTool(i)) return true;
        return i instanceof DiggerItem || i instanceof TridentItem;
    }

    public static boolean isCookie(Item i) {
        return (i.builtInRegistryHolder().is(ModTags.COOKIES));
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
        return SHULKER_TILE.get().canPlaceItemThroughFace(0, stack, null);
    }


    //cylinder distance
    public static boolean withinDistanceDown(BlockPos pos, Vec3 vector, double distW, double distDown) {
        double dx = vector.x() - (pos.getX() + 0.5);
        double dy = vector.y() - (pos.getY() + 0.5);
        double dz = vector.z() - (pos.getZ() + 0.5);
        double myDistW = (dx * dx + dz * dz);
        return (myDistW < (distW * distW) && (dy < distW && dy > -distDown));
    }

    public static Player getEntityStand(Entity copyFrom) {
        return getEntityStand(copyFrom, copyFrom);
    }

    @ExpectPlatform
    public static Player getEntityStand(Entity copyPosFrom, Entity copyRotFrom) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @CheckForNull
    @Nullable
    public static Player getFakePlayer(Level level) {
        throw new AssertionError();
    }

}