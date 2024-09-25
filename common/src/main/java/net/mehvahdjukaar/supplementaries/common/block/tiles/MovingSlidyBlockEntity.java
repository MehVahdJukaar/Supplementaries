package net.mehvahdjukaar.supplementaries.common.block.tiles;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MovingSlidyBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class MovingSlidyBlockEntity extends PistonMovingBlockEntity {


    public MovingSlidyBlockEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    public MovingSlidyBlockEntity(BlockPos pos, BlockState blockState, BlockState movedState, Direction direction, boolean extending, boolean isSourcePiston) {
        super(pos, blockState, movedState, direction, extending, isSourcePiston);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.MOVING_SLIDY_BLOCK_TILE.get();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MovingSlidyBlockEntity t) {

        if (level.isClientSide) {
            t.spawnSlidyParticles(level, pos);
        }

        BlockState movedState = t.getMovedState();
        t.lastTicked = level.getGameTime();
        t.progressO = t.progress;
        if (t.progressO >= 1.0F) {
            if (level.isClientSide && t.deathTicks < 5) {
                ++t.deathTicks;
            } else {
                level.removeBlockEntity(pos);
                t.setRemoved();
                if (level.getBlockState(pos).is(ModRegistry.MOVING_SLIDY_BLOCK.get())) {
                    BlockState blockState = Block.updateFromNeighbourShapes(t.getMovedState(), level, pos);
                    if (blockState.isAir()) {
                        level.setBlock(pos, movedState, 84);
                        Block.updateOrDestroy(movedState, blockState, level, pos, 3);
                    } else {
                        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
                            blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
                        }

                        level.setBlock(pos, blockState, 67 | Block.UPDATE_KNOWN_SHAPE);
                        level.neighborChanged(pos, blockState.getBlock(), pos);

                        if (level instanceof ServerLevel sl) blockState.tick(sl, pos, sl.random);
                    }
                }

            }
        } else {
            // all this just to change this single line
            float f = (float) (t.progress + CommonConfigs.Building.SLIDY_BLOCK_SPEED.get());
            moveCollidedEntities(level, pos, f, t);
            moveStuckEntities(level, pos, f, t);

            t.progress = f;
            if (t.progress >= 1.0F) {
                t.progress = 1.0F;

                Direction direction = t.getDirection();
                if (level.getBlockState(pos.below()).is(BlockTags.ICE)) {
                    MovingSlidyBlock.maybeMove(movedState, level, pos, direction);
                    level.gameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
                }
                if (level.getBlockState(pos.below()).is(ModRegistry.SOAP_BLOCK.get()) && !level.isClientSide) {

                    // we are rolling a dice here so we can only run on server
                    // catch is that animation will look a bit glitchy even tho we are sending a packet too
                    ObjectArrayList<Direction> dirs = ObjectArrayList.of(
                            direction.getClockWise(),
                            direction.getCounterClockWise(),
                            direction);
                    Util.shuffle(dirs, level.random);
                    for (Direction randomDir : dirs) {
                        if (MovingSlidyBlock.maybeMove(movedState, level, pos, randomDir)) {
                            level.blockEvent(pos.below(), ModRegistry.SOAP_BLOCK.get(), 0, 0);
                            level.gameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void spawnSlidyParticles(Level level, BlockPos pos) {
        //funny particles
        RandomSource rand = level.random;
        Direction dir = this.getDirection();
        Vector3f mul = dir.step().mul(-1.5f + this.progress);
        BlockState belowState = level.getBlockState(BlockPos.containing(pos.below().getCenter().add(new Vec3(mul))));
        if (belowState.isAir()) return;
        ParticleOptions opt = new BlockParticleOption(ParticleTypes.BLOCK, belowState);
        switch (dir) {
            case NORTH ->
                    level.addParticle(opt, pos.getX() + rand.nextFloat(), pos.getY(), pos.getZ() - this.progress + 2,
                            0.0D, 0.0D, 0.0D);
            case SOUTH ->
                    level.addParticle(opt, pos.getX() + rand.nextFloat(), pos.getY(), pos.getZ() + this.progress - 1,
                            0.0D, 0.0D, 0.0D);
            case WEST ->
                    level.addParticle(opt, pos.getX() - this.progress + 2, pos.getY(), pos.getZ() + rand.nextFloat(),
                            0.0D, 0.0D, 0.0D);
            case EAST ->
                    level.addParticle(opt, pos.getX() + this.progress - 1, pos.getY(), pos.getZ() + rand.nextFloat(),
                            0.0D, 0.0D, 0.0D);
            default -> {
            }
        }
    }

    public void addOffset(float offset) {
        this.progressO = this.progress;
        this.progress += offset;
    }
}
