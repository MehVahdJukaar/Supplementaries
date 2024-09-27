package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class NoticeBoardBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    public static final BooleanProperty CULLED = ModBlockProperties.CULLED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public NoticeBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                        .setValue(CULLED, false).setValue(POWERED,false)
                .setValue(FACING, Direction.NORTH).setValue(HAS_BOOK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOK, CULLED, POWERED);
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
        Direction dir = context.getHorizontalDirection().getOpposite();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos facingPos = pos.relative(dir);
        BlockState facingState = level.getBlockState(facingPos);
        boolean culled = facingState.isSolidRender(level, pos) &&
                facingState.isFaceSturdy(level, facingPos, dir.getOpposite());
        boolean powered = level.getBestNeighborSignal(pos) > 0;
        return this.defaultBlockState().setValue(FACING, dir).setValue(CULLED, culled).setValue(POWERED, powered);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof NoticeBoardBlockTile tile) {
            return tile.interact(player, handIn, pos, state,hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return worldIn.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new NoticeBoardBlockTile(pPos, pState);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == state.getValue(FACING)) {
            if (level.getBlockEntity(currentPos) instanceof NoticeBoardBlockTile) {
                boolean culled = facingState.isSolidRender(level, currentPos) &&
                        facingState.isFaceSturdy(level, facingPos, facing.getOpposite());
                state = state.setValue(CULLED, culled);
            }
        }
        return state;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof NoticeBoardBlockTile tile) {
                Containers.dropContents(level, pos, tile);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof Container tile)
            return tile.isEmpty() ? 0 : 15;
        else
            return 0;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(state, world, pos, pBlock, pFromPos, pIsMoving);
        boolean powered = world.getBestNeighborSignal(pos) > 0;
        if(powered != state.getValue(POWERED)){
            //reacts to rising edge
            if(powered && world.getBlockEntity(pos) instanceof NoticeBoardBlockTile tile){
                tile.turnPage();
            }
            world.setBlockAndUpdate(pos, state.setValue(POWERED, powered));

        }
    }

}