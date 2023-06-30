package net.mehvahdjukaar.supplementaries.common.block.blocks;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.misc.CakeRegistry;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoubleCakeBlock extends DirectionalCakeBlock {

    protected static final VoxelShape[] SHAPES_WEST = new VoxelShape[]{
            Shapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(3, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(5, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(7, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(9, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(11, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(13, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_EAST = new VoxelShape[]{
            Shapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 13, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 11, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 9, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 7, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 5, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 3, 15, 14),
                    box(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_SOUTH = new VoxelShape[]{
            Shapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 14, 15, 13),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 14, 15, 11),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 14, 15, 9),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 14, 15, 7),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 14, 15, 5),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 2, 14, 15, 3),
                    box(1, 0, 1, 15, 8, 15))};
    protected static final VoxelShape[] SHAPES_NORTH = new VoxelShape[]{
            Shapes.or(box(2, 8, 2, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 3, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 5, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 7, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 9, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 11, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15)),
            Shapes.or(box(2, 8, 13, 14, 15, 14),
                    box(1, 0, 1, 15, 8, 15))};
    private final BlockState mimic;

    public DoubleCakeBlock(CakeRegistry.CakeType type) {
        super(type);
        this.mimic = type.cake.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> SHAPES_WEST[state.getValue(BITES)];
            case EAST -> SHAPES_EAST[state.getValue(BITES)];
            case SOUTH -> SHAPES_SOUTH[state.getValue(BITES)];
            case NORTH -> SHAPES_NORTH[state.getValue(BITES)];
        };
    }

    @Override
    public void removeSlice(BlockState state, BlockPos pos, LevelAccessor level, Direction dir) {
        int i = state.getValue(BITES);
        if (i < 6) {
            if (i == 0 && CommonConfigs.Tweaks.DIRECTIONAL_CAKE.get()) state = state.setValue(FACING, dir);
            level.setBlock(pos, state.setValue(BITES, i + 1), 3);
        } else {
            if (this.type == CakeRegistry.VANILLA && state.getValue(WATERLOGGED) && CommonConfigs.Tweaks.DIRECTIONAL_CAKE.get()) {
                level.setBlock(pos, ModRegistry.DIRECTIONAL_CAKE.get().defaultBlockState()
                        .setValue(FACING, state.getValue(FACING)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)), 3);
            } else {
                level.setBlock(pos, type.cake.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void animateTick(BlockState stateIn, Level level, BlockPos pos, RandomSource rand) {
        if (MiscUtils.FESTIVITY.isStValentine()) {
            if (rand.nextFloat() > 0.8) {
                double d0 = (pos.getX() + 0.5 + (rand.nextFloat() - 0.5));
                double d1 = (pos.getY() + 0.5 + (rand.nextFloat() - 0.5));
                double d2 = (pos.getZ() + 0.5 + (rand.nextFloat() - 0.5));
                level.addParticle(ParticleTypes.HEART, d0, d1, d2, 0, 0, 0);
            }
        }
        super.animateTick(stateIn, level, pos, rand);
        mimic.getBlock().animateTick(mimic, level, pos, rand);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        return Math.min(super.getDestroyProgress(state, player, worldIn, pos),
                mimic.getDestroyProgress(player, worldIn, pos));
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        return mimic.getSoundType();
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return mimic.getSoundType();
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return level instanceof Level l ?  Math.max(ForgeHelper.getExplosionResistance(mimic, l, pos, explosion),
                state.getBlock().getExplosionResistance()) : super.getExplosionResistance();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return mimic.getBlock().getCloneItemStack(level, pos, state);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        //hack
        if (!player.getItemInHand(handIn).is(ItemTags.CANDLES)) {
            BlockState newState = type.cake.withPropertiesOf(state);
            level.setBlock(pos, newState, Block.UPDATE_INVISIBLE);
            var res = newState.use(level, player, handIn, hit);
            level.setBlockAndUpdate(pos, state);
            if (res.consumesAction()) {
                if (!level.isClientSide()) {
                    this.removeSlice(state, pos, level, getHitDir(player, hit));
                }
                return res;
            }
        }

        return super.use(state, level, pos, player, handIn, hit);
    }

}