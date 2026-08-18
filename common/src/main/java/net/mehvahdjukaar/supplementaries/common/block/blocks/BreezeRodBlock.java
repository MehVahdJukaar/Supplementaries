package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class BreezeRodBlock extends StickBlock {

    public BreezeRodBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(AXIS_Y, true).setValue(AXIS_X, false).setValue(AXIS_Z, false));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() > 0.2f) return;
        List<Direction.Axis> axes = new ArrayList<>(3);
        for (var e : AXIS2PROPERTY.entrySet()) {
            if (state.getValue(e.getValue())) axes.add(e.getKey());
        }
        if (axes.isEmpty()) return;

        Direction.Axis axis = axes.get(random.nextInt(axes.size()));
        double along = random.nextDouble();
        double x = pos.getX() + (axis == Direction.Axis.X ? along : 0.5);
        double y = pos.getY() + (axis == Direction.Axis.Y ? along : 0.5);
        double z = pos.getZ() + (axis == Direction.Axis.Z ? along : 0.5);
        level.addParticle(ModParticles.WIND_SWIRL.get(), x, y, z, axis.ordinal(), 0, 0);
    }
}
