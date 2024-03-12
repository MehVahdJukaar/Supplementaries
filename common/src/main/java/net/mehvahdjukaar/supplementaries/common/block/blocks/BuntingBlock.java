package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BuntingBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BuntingBlock extends RopeBlock implements EntityBlock, IRotatable {

    public BuntingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, Rotation rotation, Direction direction, @Nullable Vec3 vec3) {
      //TODO: rotation stuff
        return Optional.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BuntingBlockTile(pos, state);
    }
}
