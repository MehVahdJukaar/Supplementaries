package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class LeadDoorBlock extends DoorBlock {
    public static final IntegerProperty OPENING_PROGRESS = ModBlockProperties.OPENING_PROGRESS;

    public LeadDoorBlock(Properties builder) {
        super(builder, BlockSetType.IRON);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(OPENING_PROGRESS);
    }

    public boolean canBeOpened(BlockState state) {
        return state.getValue(OPENING_PROGRESS) == 2;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (this.canBeOpened(state)) {
            GoldDoorBlock.tryOpenDoubleDoor(level, state, pos);

            state = state.cycle(OPEN).setValue(OPENING_PROGRESS, 0);
            level.setBlock(pos, state, 10);
            this.playSound(player, level, pos, state.getValue(OPEN));

        } else {
            //sound here
            int p = state.getValue(OPENING_PROGRESS) + 1;
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                level.setBlock(pos.below(), level.getBlockState(pos.below()).setValue(OPENING_PROGRESS, p), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
            } else {
                level.setBlock(pos.above(), level.getBlockState(pos.above()).setValue(OPENING_PROGRESS, p), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
            }
            level.setBlock(pos, state.setValue(OPENING_PROGRESS, p), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);

            level.playSound(player, pos, SoundEvents.NETHERITE_BLOCK_STEP, SoundSource.BLOCKS, 1, 1);
            level.scheduleTick(pos, this, 20);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource pRandom) {
        level.setBlock(pos, state.setValue(OPENING_PROGRESS, 0), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            level.setBlock(pos.below(), level.getBlockState(pos.below()).setValue(OPENING_PROGRESS, 0), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        } else {
            level.setBlock(pos.above(), level.getBlockState(pos.above()).setValue(OPENING_PROGRESS, 0), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(worldIn, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) state.setValue(OPEN, false).setValue(POWERED, false);
        return state;
    }

    private int getCloseSound() {
        return 1011;
    }

    private int getOpenSound() {
        return 1005;
    }

}
