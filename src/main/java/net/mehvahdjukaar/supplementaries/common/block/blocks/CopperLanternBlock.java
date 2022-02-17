package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.content.building.block.StoolBlock;
import vazkii.quark.content.building.client.render.entity.StoolEntityRenderer;

public class CopperLanternBlock extends LightableLanternBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public CopperLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Z)
                .setValue(WATERLOGGED, false).setValue(LIT,true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var b = super.getStateForPlacement(context);
        if (b != null) b = b.setValue(AXIS, context.getHorizontalDirection().getAxis());
        return b;
    }
}
