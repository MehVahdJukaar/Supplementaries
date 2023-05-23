package net.mehvahdjukaar.supplementaries.common.block.blocks;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class FlagBlock extends WaterBlock implements EntityBlock, IColored {
    protected static final VoxelShape SHAPE = Block.box(4, 0D, 4D, 12.0D, 16.0D, 12.0D);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final DyeColor color;

    public FlagBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));

        if(PlatformHelper.getPlatform().isFabric())
            RegHelper.registerBlockFlammability(this,60,60);
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(pos) instanceof FlagBlockTile tile ? tile.getItem(state) : super.getCloneItemStack(level, pos, state);
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }

    @Nullable
    @Override
    public Item changeItemColor(@Nullable DyeColor color) {
        var c =  ModRegistry.FLAGS.get(color);
        if(c != null)return c.get().asItem();
        return null;
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
        if (world.getBlockEntity(pos) instanceof FlagBlockTile) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (itemstack.getItem() instanceof MapItem) {
                if (!world.isClientSide) {
                    if (MapItem.getSavedData(itemstack, world) instanceof ExpandedMapData data) {
                        data.toggleCustomDecoration(world, pos);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            } else if (itemstack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                if (CommonConfigs.Building.FLAG_POLE.get()) {
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