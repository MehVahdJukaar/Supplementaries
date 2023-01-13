package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IPistonMotionReact;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class FlintBlock extends Block implements IPistonMotionReact {
    public FlintBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onMoved(BlockState movedState, Level level, BlockPos pos, Direction moveDirection, boolean extending) {
        if (!extending && !level.isClientSide) {
            BlockPos firePos = pos.relative(moveDirection);
            if (level.getBlockState(firePos).isAir()) {
                for (Direction ironDir : Direction.values()) {
                    if (ironDir.getAxis() == moveDirection.getAxis()) continue;
                    BlockPos ironPos = firePos.relative(ironDir);
                    BlockState facingState = level.getBlockState(ironPos);
                    if (canBlockCreateSpark(facingState, level, ironPos, ironDir.getOpposite())) {
                        ignitePosition(level, firePos, false);
                        return;
                    }
                }
            }
        }
    }

    private void ignitePosition(Level level, BlockPos firePos, boolean isIronMoving) {
        //send particle packet

        NetworkHandler.CHANNEL.sendToAllClientPlayersInRange(level, firePos, 64,
                new ClientBoundParticlePacket(Vec3.atCenterOf(firePos),
                        ClientBoundParticlePacket.EventType.FLINT_BLOCK_IGNITE,
                        isIronMoving ? 1 : 0));
        for (Direction dir : Direction.values()) {

            if (BaseFireBlock.canBePlacedAt(level, firePos, dir)) {
                level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
                level.gameEvent(null, GameEvent.BLOCK_PLACE, firePos);
                break;
            }
        }
        this.playSound(level, firePos);
    }

    public static boolean canBlockCreateSpark(BlockState state, Level level, BlockPos pos, Direction face) {
        return state.is(ModTags.FLINT_METALS) &&
                state.isFaceSturdy(level, pos, face);
    }

    private void playSound(Level level, BlockPos pos) {
        RandomSource randomSource = level.getRandom();
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block oldBlock, BlockPos targetPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, oldBlock, targetPos, isMoving);
        BlockState newState = level.getBlockState(targetPos);
        if (!newState.isAir() || !oldBlock.builtInRegistryHolder().is(ModTags.FLINT_METALS)) return;
        Direction dir = Direction.fromNormal(pos.subtract(targetPos));
        for (Direction pistonDir : Direction.values()) {
            if (dir.getAxis() == pistonDir.getAxis()) continue;

            BlockPos tilePos = targetPos.relative(pistonDir);
            BlockEntity be = level.getBlockEntity(tilePos);
            if (be instanceof PistonMovingBlockEntity piston) {
                if (piston.getDirection() == pistonDir.getOpposite()
                        && piston.getMovedState().is(oldBlock)) {
                    //correct check
                    if (canBlockCreateSpark(piston.getMovedState(), level, tilePos, dir)) {
                        ignitePosition(level, targetPos, true);
                    }
                }
            } else if (be != null && CompatHandler.QUARK) {
                BlockState magnetState = QuarkCompat.getMagnetStateForFlintBlock(be, pistonDir);
                if (magnetState != null && magnetState.is(oldBlock) &&
                        canBlockCreateSpark(magnetState, level, tilePos, dir)) {
                    ignitePosition(level, targetPos, true);
                }
            }
        }
    }

}