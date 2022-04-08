package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GobletBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class GobletBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 9, 11);

    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public GobletBlock(Properties properties) {
        super(properties.lightLevel(state->state.getValue(LIGHT_LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0).setValue(WATERLOGGED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof GobletBlockTile tile && tile.isAccessibleBy(player)) {
            // make te do the work
            if (tile.handleInteraction(player, handIn)) {
                if (!worldIn.isClientSide())
                    tile.setChanged();
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GobletBlockTile(pPos, pState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof GobletBlockTile tile) {
            return tile.fluidHolder.isEmpty() ? 0 : 15;
        }
        return 0;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
        if (0.05 > random.nextFloat()) {
            if (world.getBlockEntity(pos) instanceof GobletBlockTile tile) {
                SoftFluidHolder holder = tile.getSoftFluidHolder();
                SoftFluid fluid = holder.getFluid();
                if (fluid == SoftFluidRegistry.POTION.get()) {
                    int i = holder.getTintColor(world, pos);
                    double d0 = (double) (i >> 16 & 255) / 255.0D;
                    double d1 = (double) (i >> 8 & 255) / 255.0D;
                    double d2 = (double) (i & 255) / 255.0D;

                    world.addParticle(ParticleTypes.ENTITY_EFFECT, pos.getX() + 0.3125 + random.nextFloat() * 0.375, pos.getY() + 0.5625, pos.getZ() + 0.3125 + random.nextFloat() * 0.375, d0, d1, d2);
                }
            }
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }
}
