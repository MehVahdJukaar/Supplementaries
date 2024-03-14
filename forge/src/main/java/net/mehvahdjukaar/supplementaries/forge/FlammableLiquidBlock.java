package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

// diff property means we need a diff class
public class FlammableLiquidBlock extends FiniteLiquidBlock implements ILightable {


    // age 0 = no fire
    // age 1 = startfire
    // age 15 = ded
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;;


    public FlammableLiquidBlock(Supplier<? extends FiniteFluid> supplier, Properties arg) {
        super(supplier, arg.lightLevel((state) -> state.getValue(AGE) > 0 ? 15 : 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState toggleLitState(BlockState state, boolean lit) {
        return state.setValue(AGE, lit ? 1 : 0);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        return interactWithPlayer(state, worldIn, pos, player, handIn);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile projectile) {
        BlockPos pos = pHit.getBlockPos();
        interactWithProjectile(level, state, projectile, pos);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof Projectile projectile) {
            interactWithProjectile(worldIn, state, projectile, pos);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState();
        return toggleLitState(state, !flag);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getFluidState().getShape(level, pos);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
