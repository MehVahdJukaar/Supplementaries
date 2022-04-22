package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class HourGlassBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE_Y = Block.box(4D, 0D, 4.0D, 12.0D, 16D, 12.0D);
    protected static final VoxelShape SHAPE_Z = Block.box(4D, 4D, 0.0D, 12.0D, 12D, 16.0D);
    protected static final VoxelShape SHAPE_X = Block.box(0D, 4D, 4D, 16D, 12D, 12.0D);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public HourGlassBlock(Properties properties) {
        super(properties.lightLevel(state->state.getValue(LIGHT_LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(LIGHT_LEVEL, 0)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, LIGHT_LEVEL);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace());
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof ItemDisplayTile tile && tile.isAccessibleBy(player)) {

            if (player.isShiftKeyDown() && player.getItemInHand(handIn).isEmpty() && state.getValue(FACING).getAxis() == Direction.Axis.Y) {
                if (!worldIn.isClientSide) {
                    worldIn.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getOpposite()), 3);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1, 1);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.SUCCESS;
            }

            return tile.interact(player, handIn);
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case Z -> SHAPE_Z;
            default -> SHAPE_Y;
            case X -> SHAPE_X;
        };
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HourGlassBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof HourGlassBlockTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof HourGlassBlockTile tile) {
            return tile.power;
        } else
            return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add((new TranslatableComponent("message.supplementaries.hourglass")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.HOURGLASS_TILE.get(), HourGlassBlockTile::tick);
    }
}