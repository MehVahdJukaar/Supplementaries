package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.content.building.block.StoolBlock;
import vazkii.quark.content.building.entity.Stool;

public class CrimsonLanternBlock extends LanternBlock {
    public static final VoxelShape SHAPE_DOWN = Shapes.or(Block.box(4.0D, 1.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(6.0D, 0.0D, 6.0D, 10.0D, 9.0D, 10.0D));
    public static final VoxelShape SHAPE_UP = Shapes.or(Block.box(4.0D, 6.0D, 4.0D, 12.0D, 13.0D, 12.0D),
            Block.box(6.0D, 5.0D, 6.0D, 10.0D, 14.0D, 10.0D));

    public CrimsonLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_153475_, BlockPos p_153476_, CollisionContext p_153477_) {
        return state.getValue(HANGING) ? SHAPE_UP : SHAPE_DOWN;
    }
}
