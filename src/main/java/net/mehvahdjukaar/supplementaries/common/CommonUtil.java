package net.mehvahdjukaar.supplementaries.common;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.common.capabilities.SupplementariesCapabilities;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.tetra.TetraToolHelper;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

        public boolean isBirthday(){
            return this == MOD_BIRTHDAY || this == MY_BIRTHDAY;
        }

        public int getCandyWrappingIndex() {
            switch (this) {
                default:
                case NONE:
                    return 0;
                case HALLOWEEN:
                    return 1;
                case CHRISTMAS:
                    return 2;
            }
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
            if (month == Calendar.DECEMBER && date >= 24 && date <= 26) return CHRISTMAS;
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
        return i instanceof ToolItem || i instanceof TridentItem;
    }

    //TODO: move to tag
    public static boolean isLantern(Item i) {
        if (i instanceof BlockItem) {
            Block b = ((BlockItem) i).getBlock();
            String namespace = b.getRegistryName().getNamespace();
            if (namespace.equals("skinnedlanterns")) return true;
            if (b instanceof LanternBlock && !ServerConfigs.cached.WALL_LANTERN_BLACKLIST.contains(namespace)) {
                return !b.hasTileEntity(b.defaultBlockState());
            }
        }
        return false;
    }

    public static boolean isCookie(Item i) {
        return (i.is(ModTags.COOKIES));
    }

    public static boolean isBrick(Item i) {
        return (i.is(ModTags.BRICKS));
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
    public static AxisAlignedBB getDirectionBB(BlockPos pos, Direction facing, int offset) {
        BlockPos endPos = pos.relative(facing, offset);
        switch (facing) {
            default:
            case NORTH:
                endPos = endPos.offset(1, 1, 0);
                break;
            case SOUTH:
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(0, 0, 1);
                break;
            case UP:
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(0, 1, 0);
                break;
            case EAST:
                endPos = endPos.offset(1, 1, 1);
                pos = pos.offset(1, 0, 0);
                break;
            case WEST:
                endPos = endPos.offset(0, 1, 1);
                break;
            case DOWN:
                endPos = endPos.offset(1, 0, 1);
                break;
        }
        return new AxisAlignedBB(pos, endPos);
    }


    //equals is not working...
    public static boolean isShapeEqual(AxisAlignedBB s1, AxisAlignedBB s2) {
        return s1.minX == s2.minX && s1.minY == s2.minY && s1.minZ == s2.minZ && s1.maxX == s2.maxX && s1.maxY == s2.maxY && s1.maxZ == s2.maxZ;
    }

    public static final AxisAlignedBB FENCE_SHAPE = Block.box(6, 0, 6, 10, 16, 10).bounds();
    public static final AxisAlignedBB POST_SHAPE = Block.box(5, 0, 5, 11, 16, 11).bounds();
    public static final AxisAlignedBB WALL_SHAPE = Block.box(7, 0, 7, 12, 16, 12).bounds();

    //0 normal, 1 fence, 2 walls TODO: change 1 with 2
    public static int getPostSize(BlockState state, BlockPos pos, IWorldReader world) {
        Block block = state.getBlock();

        VoxelShape shape = state.getShape(world, pos);
        if (shape != VoxelShapes.empty()) {
            AxisAlignedBB s = shape.bounds();
            if (block instanceof FenceBlock || block instanceof SignPostBlock || block.is(Tags.Blocks.FENCES) || isShapeEqual(FENCE_SHAPE, s))
                return 1;
            if (block instanceof WallBlock || block.is(BlockTags.WALLS) ||
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
    private static final ShulkerBoxTileEntity SHULKER_TILE = new ShulkerBoxTileEntity();


    public static boolean isAllowedInShulker(ItemStack stack) {
        return SHULKER_TILE.canPlaceItemThroughFace(0, stack, null);
    }


    //cylinder distance
    public static boolean withinDistanceDown(BlockPos pos, Vector3d vector, double distW, double distDown) {
        double dx = vector.x() - ((double) pos.getX() + 0.5);
        double dy = vector.y() - ((double) pos.getY() + 0.5);
        double dz = vector.z() - ((double) pos.getZ() + 0.5);
        double mydistW = (dx * dx + dz * dz);
        return (mydistW < (distW * distW) && (dy < distW && dy > -distDown));
    }


    @OnlyIn(Dist.CLIENT)
    public static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static void doStuff(TileEntity tile, Runnable callable){
        tile.getCapability(SupplementariesCapabilities.ANTIQUE_TEXT_CAP).ifPresent(c -> {
            if (c.hasAntiqueInk()) {
                IAntiqueTextProvider FONT = (IAntiqueTextProvider) (Minecraft.getInstance().font);
                FONT.setAntiqueInk(true);
                callable.run();;
                //antiqueFontActive = true;
            }
        });
    }


}