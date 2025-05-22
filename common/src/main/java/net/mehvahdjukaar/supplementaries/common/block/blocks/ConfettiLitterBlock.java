package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

public class ConfettiLitterBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ConfettiLitterBlock> CODEC = simpleCodec(ConfettiLitterBlock::new);

    public static final IntegerProperty AMOUNT = ModBlockProperties.CONFETTI_AMOUNT;
    private static final BiFunction<Direction, Integer, VoxelShape> SHAPE_BY_PROPERTIES = Util.memoize((direction, integer) -> {
        VoxelShape[] voxelShapes = new VoxelShape[]{Block.box(8.0, 0.0, 8.0, 16.0, 3.0, 16.0), Block.box(8.0, 0.0, 0.0, 16.0, 3.0, 8.0), Block.box(0.0, 0.0, 0.0, 8.0, 3.0, 8.0), Block.box(0.0, 0.0, 8.0, 8.0, 3.0, 16.0)};
        VoxelShape voxelShape = Shapes.empty();

        for (int i = 0; i < integer; ++i) {
            int j = Math.floorMod(i - direction.get2DDataValue(), 4);
            voxelShape = Shapes.or(voxelShape, voxelShapes[j]);
        }

        return voxelShape.singleEncompassing();
    });

    public ConfettiLitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
