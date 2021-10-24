package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.supplementaries.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;

public class FlagBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = Block.box(4, 0D, 4D, 12.0D, 16.0D, 12.0D);
    private static final Map<DyeColor, Block> BY_COLOR = Maps.newHashMap();
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final DyeColor color;

    public FlagBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        BY_COLOR.put(color, this);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return world.getBlockEntity(pos) instanceof FlagBlockTile tile ? tile.getItem(state) : super.getPickBlock(state, target, world, pos, player);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public static Block byColor(DyeColor color) {
        return BY_COLOR.getOrDefault(color, Blocks.WHITE_BANNER);
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
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
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FlagBlockTile(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (world.getBlockEntity(pos) instanceof FlagBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof FlagBlockTile tile) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (itemstack.getItem() instanceof MapItem) {
                if (!world.isClientSide) {
                    MapItemSavedData data = MapItem.getOrCreateSavedData(itemstack, world);
                    if (data instanceof CustomDecorationHolder) {
                        ((CustomDecorationHolder) data).toggleCustomDecoration(world, pos);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            } else if (itemstack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                if (ServerConfigs.cached.STICK_POLE) {
                    if (world.isClientSide) return InteractionResult.SUCCESS;
                    else {
                        Direction moveDir = player.isShiftKeyDown() ? Direction.DOWN : Direction.UP;
                        StickBlock.findConnectedFlag(world, pos.below(), Direction.UP, moveDir, 0);
                        StickBlock.findConnectedFlag(world, pos.above(), Direction.DOWN, moveDir, 0);
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }
}