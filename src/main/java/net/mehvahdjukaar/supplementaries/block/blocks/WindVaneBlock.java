package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WindVaneBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = Shapes.box(0.125D, 0D, 0.125D, 0.875D, 1D, 0.875D);

    public static final BooleanProperty TILE = BlockProperties.TILE; // is it rooster only?
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public WindVaneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(TILE, false).setValue(POWER, 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips) return;
        tooltip.add(new TranslatableComponent("message.supplementaries.wind_vane").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));

    }

    //model=block model+ tile. animated=tile, inv=inv
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static void updatePower(BlockState bs, Level world, BlockPos pos) {
        int weather = 0;
        if (world.isThundering()) {
            weather = 2;
        } else if (world.isRaining()) {
            weather = 1;
        }
        if (weather != bs.getValue(POWER)) {
            world.setBlock(pos, bs.setValue(POWER, weather), 3);
            world.updateNeighborsAt(pos.below(), bs.getBlock());
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        return blockState.getValue(POWER);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return side == Direction.UP ? this.getSignal(blockState, blockAccess, pos, side) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER, TILE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
        return BlockPathTypes.BLOCKED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new WindVaneBlockTile();
    }
}