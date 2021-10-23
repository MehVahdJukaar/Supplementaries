package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.PedestalBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = Shapes.or(Shapes.box(0.1875D, 0.125D, 0.1875D, 0.815D, 0.885D, 0.815D),
            Shapes.box(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D),
            Shapes.box(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D));
    protected static final VoxelShape SHAPE_UP = Shapes.or(Shapes.box(0.1875D, 0.125D, 0.1875D, 0.815D, 1, 0.815D),
            Shapes.box(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D));
    protected static final VoxelShape SHAPE_DOWN = Shapes.or(Shapes.box(0.1875D, 0, 0.1875D, 0.815D, 0.885D, 0.815D),
            Shapes.box(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D));
    protected static final VoxelShape SHAPE_UP_DOWN = Shapes.box(0.1875D, 0, 0.1875D, 0.815D, 1, 0.815D);

    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty HAS_ITEM = BlockProperties.HAS_ITEM;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public PedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(UP, false).setValue(AXIS, Direction.Axis.X)
                .setValue(DOWN, false).setValue(WATERLOGGED, false).setValue(HAS_ITEM, false));
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PedestalBlockTile) {
            if (((PedestalBlockTile) te).type == PedestalBlockTile.DisplayType.CRYSTAL) return 3;
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, WATERLOGGED, HAS_ITEM, AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean flag = world.getFluidState(pos).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(AXIS, context.getHorizontalDirection().getAxis())
                .setValue(UP, canConnect(world.getBlockState(pos.above()), pos, world, Direction.UP, false))
                .setValue(DOWN, canConnect(world.getBlockState(pos.below()), pos, world, Direction.DOWN, false));
    }

    public static boolean canConnect(BlockState state, BlockPos pos, LevelAccessor world, Direction dir, boolean hasItem) {
        if (state.getBlock() instanceof PedestalBlock) {
            if (dir == Direction.DOWN) {
                return !state.getValue(HAS_ITEM);
            } else if (dir == Direction.UP) {
                return !hasItem;
            }
        }
        return false;
    }

    //called when a neighbor is placed
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        if (facing == Direction.UP) {
            return stateIn.setValue(UP, canConnect(facingState, currentPos, worldIn, facing, stateIn.getValue(HAS_ITEM)));
        } else if (facing == Direction.DOWN) {
            return stateIn.setValue(DOWN, canConnect(facingState, currentPos, worldIn, facing, stateIn.getValue(HAS_ITEM)));
        }
        return stateIn;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (target.getLocation().y() > pos.getY() + 1 - 0.1875) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
                ItemStack i = tile.getDisplayedItem();
                if (!i.isEmpty()) return i;
            }
        }
        return new ItemStack(this, 1);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        //create new tile
        if (!state.getValue(HAS_ITEM)) {
            worldIn.setBlock(pos, state.setValue(HAS_ITEM, true), Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.BLOCK_UPDATE);
        }
        InteractionResult resultType = InteractionResult.PASS;
        if (worldIn.getBlockEntity(pos) instanceof PedestalBlockTile tile && tile.isAccessibleBy(player)) {

            ItemStack handItem = player.getItemInHand(handIn);

            //Indiana Jones swap
            if (handItem.getItem() instanceof SackItem) {

                ItemStack it = handItem.copy();
                it.setCount(1);
                ItemStack removed = tile.removeItemNoUpdate(0);
                tile.setDisplayedItem(it);

                if (!player.isCreative()) {
                    handItem.shrink(1);
                }
                if (!worldIn.isClientSide()) {
                    player.setItemInHand(handIn, removed);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, worldIn.random.nextFloat() * 0.10F + 0.95F);
                    tile.setChanged();
                } else {
                    //also update visuals on client. will get overwritten by packet tho
                    tile.updateClientVisualsOnLoad();
                }
                resultType = InteractionResult.sidedSuccess(worldIn.isClientSide);
            } else {
                resultType = tile.interact(player, handIn);
            }
            if (resultType.consumesAction()) {
                Direction.Axis axis = player.getDirection().getAxis();
                boolean isEmpty = tile.getDisplayedItem().isEmpty();
                if (axis != state.getValue(AXIS) || isEmpty) {
                    worldIn.setBlock(pos, state.setValue(AXIS, axis).setValue(HAS_ITEM, !isEmpty), 2);
                    if (isEmpty) worldIn.removeBlockEntity(pos);
                }
            }
        }
        return resultType;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        if (!up) {
            if (!down) {
                return SHAPE;
            } else {
                return SHAPE_DOWN;
            }
        } else {
            if (!down) {
                return SHAPE_UP;
            } else {
                return SHAPE_UP_DOWN;
            }
        }
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        if(pState.getValue(HAS_ITEM)){
            return new PedestalBlockTile();
        }
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
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
        if (world.getBlockEntity(pos) instanceof PedestalBlockTile tile)
            return tile.isEmpty() ? 0 : 15;
        else
            return 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if(rotation == Rotation.CLOCKWISE_180){
            return state;
        }
        else{
            return switch (state.getValue(AXIS)) {
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                default -> state;
            };
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }
}