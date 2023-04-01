package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class WallCandleSkullBlock extends AbstractCandleSkullBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final Map<Direction, VoxelShape[]> SHAPES = Util.make(() -> {
        Map<Direction, VoxelShape[]> m = new HashMap<>();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Vec3 step = MthUtils.V3itoV3(dir.getNormal()).scale(-0.25);
            m.put(dir, new VoxelShape[]{
                            Utils.rotateVoxelShape(ONE_AABB.move(step.x,step.y,step.z), dir),
                            Utils.rotateVoxelShape(TWO_AABB.move(step.x,step.y,step.z), dir),
                            Utils.rotateVoxelShape(THREE_AABB.move(step.x,step.y,step.z), dir),
                            Utils.rotateVoxelShape(FOUR_AABB.move(step.x,step.y,step.z), dir),
                    }
            );
        }
        return m;
    });

    public WallCandleSkullBlock(Properties properties) {
        this(properties, () -> ParticleTypes.SMALL_FLAME);
    }

    public WallCandleSkullBlock(Properties properties, Supplier<ParticleType<? extends ParticleOptions>> particle) {
        super(properties, particle);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPES.get(pState.getValue(FACING))[pState.getValue(CANDLES) - 1];
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
        var step = pState.getValue(FACING).step();
        return PARTICLE_OFFSETS.get(pState.getValue(CANDLES).intValue())
                .stream().map(v->v.add(step.x(),step.y(),step.z())).toList();
    }
}
