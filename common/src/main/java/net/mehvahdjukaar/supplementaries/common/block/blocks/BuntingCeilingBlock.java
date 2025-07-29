package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class BuntingCeilingBlock extends Block implements IColored {
    private static final MapCodec<BuntingWallBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            net.minecraft.world.item.DyeColor.CODEC.fieldOf("color").forGetter(BuntingWallBlock::getColor),
            BlockBehaviour.Properties.CODEC.fieldOf("properties").forGetter(BuntingWallBlock::properties)
    ).apply(i, BuntingWallBlock::new));

    private static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private final DyeColor color;

    public BuntingCeilingBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    @Nullable
    public DyeColor getColor() {
        return color;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);

    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_180 ? state : state.cycle(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }


}
