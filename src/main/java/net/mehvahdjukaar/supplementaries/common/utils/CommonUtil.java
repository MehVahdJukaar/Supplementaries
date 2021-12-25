package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.tetra.TetraToolHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;

import java.util.Calendar;

public class CommonUtil {

    public static DamageSource SPIKE_DAMAGE = (new DamageSource("supplementaries.bamboo_spikes"));
    public static DamageSource BOTTLING_DAMAGE = (new DamageSource("supplementaries.xp_extracting"));
    public static DamageSource AMETHYST_SHARD_DAMAGE = (new DamageSource("supplementaries.amethyst_shard"));


    //public static DamageSource getAmethystDamageSource(AbstractArrowEntity arrowEntity, @Nullable Entity shooter) {
    //    return (new IndirectEntityDamageSource("amethyst_shard", arrowEntity, shooter)).setProjectile();
    //}
    //public static DamageSource getBombExplosionDamage(@Nullable BombExplosion p_94539_0_) {
    //    return getBombExplosionDamage(p_94539_0_ != null ? p_94539_0_.getSourceMob() : null);
    //}


    //public static DamageSource getBombExplosionDamage(@Nullable LivingEntity p_188405_0_) {
    //    return p_188405_0_ != null ? (new EntityDamageSource("explosion.player", p_188405_0_)).setScalesWithDifficulty().setExplosion() : (new DamageSource("explosion")).setScalesWithDifficulty().setExplosion();
    //}

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

        public int getCandyWrappingIndex() {
            return switch (this) {
                default -> 0;
                case HALLOWEEN -> 1;
                case CHRISTMAS -> 2;
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
            if (month == Calendar.DECEMBER && date >= 23 && date <= 27) return CHRISTMAS;
            if (month == Calendar.FEBRUARY && date == 7) return MY_BIRTHDAY;
            if (month == Calendar.OCTOBER && date == 9) return MOD_BIRTHDAY;
            return NONE;
        }
    }

    public static Festivity FESTIVITY = Festivity.get();


    public static boolean isSword(Item i) {
        if (CompatHandler.tetra && TetraToolHelper.isTetraSword(i)) return true;
        return i instanceof SwordItem;
    }

    public static boolean isTool(Item i) {
        if (CompatHandler.tetra && TetraToolHelper.isTetraTool(i)) return true;
        return i instanceof DiggerItem || i instanceof TridentItem;
    }

    //TODO: move to tag
    public static boolean isLantern(Item i) {
        if (i instanceof BlockItem) {
            Block b = ((BlockItem) i).getBlock();
            String namespace = b.getRegistryName().getNamespace();
            if (namespace.equals("skinnedlanterns")) return true;
            if (b instanceof LanternBlock && !ServerConfigs.cached.WALL_LANTERN_BLACKLIST.contains(namespace)) {
                return !b.defaultBlockState().hasBlockEntity();
            }
        }
        return false;
    }

    public static boolean isCookie(Item i) {
        return (ModTags.COOKIES.contains(i));
    }

    public static boolean isBrick(Item i) {
        return (ModTags.BRICKS.contains(i));
    }

    public static boolean isCake(Item i) {
        return i == Items.CAKE;
    }

    public static boolean isPot(Item i) {
        if (i instanceof BlockItem) {
            Block b = ((BlockItem) i).getBlock();
            return ((b instanceof FlowerPotBlock));
        }
        return false;
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


    //equals is not working...
    public static boolean isShapeEqual(AABB s1, AABB s2) {
        return s1.minX == s2.minX && s1.minY == s2.minY && s1.minZ == s2.minZ && s1.maxX == s2.maxX && s1.maxY == s2.maxY && s1.maxZ == s2.maxZ;
    }

    public static final AABB FENCE_SHAPE = Block.box(6, 0, 6, 10, 16, 10).bounds();
    public static final AABB POST_SHAPE = Block.box(5, 0, 5, 11, 16, 11).bounds();
    public static final AABB WALL_SHAPE = Block.box(7, 0, 7, 12, 16, 12).bounds();

    //0 normal, 1 fence, 2 walls TODO: change 1 with 2
    public static int getPostSize(BlockState state, BlockPos pos, LevelReader world) {
        Block block = state.getBlock();

        VoxelShape shape = state.getShape(world, pos);
        if (shape != Shapes.empty()) {
            AABB s = shape.bounds();
            if (block instanceof FenceBlock || block instanceof SignPostBlock || state.is(Tags.Blocks.FENCES) || isShapeEqual(FENCE_SHAPE, s))
                return 1;
            if (block instanceof WallBlock || state.is(BlockTags.WALLS) ||
                    (isShapeEqual(WALL_SHAPE, s))) return 2;
            if (isShapeEqual(POST_SHAPE, s)) return 1;
        }

        return 0;
    }

    public static boolean isVertical(BlockState state) {
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            return state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y;
        }
        return true;
    }


    //TODO: unify this with rope knot, hanging sings and wall lanterns
    public static boolean isPost(BlockState state) {
        return isVertical(state) && state.is(ModTags.POSTS);
    }

    //this is how you do it :D
    private static final ShulkerBoxBlockEntity SHULKER_TILE = new ShulkerBoxBlockEntity(BlockPos.ZERO, Blocks.SHULKER_BOX.defaultBlockState());


    public static boolean isAllowedInShulker(ItemStack stack) {
        return SHULKER_TILE.canPlaceItemThroughFace(0, stack, null);
    }


    //cylinder distance
    public static boolean withinDistanceDown(BlockPos pos, Vec3 vector, double distW, double distDown) {
        double dx = vector.x() - ((double) pos.getX() + 0.5);
        double dy = vector.y() - ((double) pos.getY() + 0.5);
        double dz = vector.z() - ((double) pos.getZ() + 0.5);
        double mydistW = (dx * dx + dz * dz);
        return (mydistW < (distW * distW) && (dy < distW && dy > -distDown));
    }


    @OnlyIn(Dist.CLIENT)
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }


    public static HitResult rayTrace(LivingEntity entity, Level world, ClipContext.Block blockMode, ClipContext.Fluid fluidMode) {
        return rayTrace(entity, world, blockMode, fluidMode, entity.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue());
    }

    public static HitResult rayTrace(Entity entity, Level world, ClipContext.Block blockMode, ClipContext.Fluid fluidMode, double range) {
        Vec3 startPos = entity.getEyePosition();;
        Vec3 ray = entity.getViewVector(1).scale(range);
        Vec3 endPos = startPos.add(ray);
        ClipContext context = new ClipContext(startPos, endPos, blockMode, fluidMode, entity);
        return world.clip(context);
    }

}