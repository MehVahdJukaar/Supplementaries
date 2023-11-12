package net.mehvahdjukaar.supplementaries.common.block.blocks;


import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.supplementaries.common.block.present.IPresentItemBehavior;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class TrappedPresentBlock extends AbstractPresentBlock {

    private static final Map<Item, IPresentItemBehavior> TRAPPED_PRESENT_INTERACTIONS_REGISTRY =  new IdentityHashMap<>();

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ON_COOLDOWN = BlockStateProperties.TRIGGERED;

    public TrappedPresentBlock(DyeColor color, Properties properties) {
        super(color, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH)
                .setValue(ON_COOLDOWN,false));
    }

    public static void registerBehavior(ItemLike pItem, IPresentItemBehavior pBehavior) {
        TRAPPED_PRESENT_INTERACTIONS_REGISTRY.put(pItem.asItem(), pBehavior);
    }

    public static IPresentItemBehavior getPresentBehavior(ItemStack pStack) {
        return TRAPPED_PRESENT_INTERACTIONS_REGISTRY.getOrDefault(pStack.getItem(), (source, stack) -> Optional.empty());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ON_COOLDOWN);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TrappedPresentBlockTile(pPos, pState);
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        if (pId == 0 && pState.getValue(ON_COOLDOWN)) {
            if (pLevel.isClientSide) {
                RandomSource random = pLevel.random;

                double cx = pPos.getX() + 0.5D;
                double cy = pPos.getY() + 0.5 + 0.4;
                double cz = pPos.getZ() + 0.5D;

                for (int i = 0; i < 10; ++i) {
                    double speed = random.nextDouble() * 0.15D + 0.015D;
                    double py = cy + 0.02D + (random.nextDouble() - 0.5D) * 0.3D;
                    double dx = random.nextGaussian() * 0.01D;
                    double dy = speed + random.nextGaussian() * 0.01D;
                    double dz = random.nextGaussian() * 0.01D;
                    pLevel.addParticle(ParticleTypes.CLOUD, cx, py, cz, dx, dy, dz);
                }

                ParticleUtil.spawnBreakParticles(PresentBlock.SHAPE_LID, pPos, pState, pLevel);
                // ((ClientLevel)pLevel).playLocalSound(pPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 0.7f,false);
            }
            return true;
        }
        return super.triggerEvent(pState, pLevel, pPos, pId, pParam);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if(state.getValue(ON_COOLDOWN)){
            level.setBlockAndUpdate(pos, state.setValue(ON_COOLDOWN, false));
        }
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
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        boolean isPowered = world.hasNeighborSignal(pos);
        if (world instanceof ServerLevel serverLevel && isPowered && state.getValue(PACKED)
                && world.getBlockEntity(pos) instanceof TrappedPresentBlockTile tile) {
            tile.detonate(serverLevel, pos);
        }
    }

}
