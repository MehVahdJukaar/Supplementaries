package net.mehvahdjukaar.supplementaries.common.fluids;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.SoulFiredCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.IntStream;

// diff property means we need a diff class
public class FlammableLiquidBlock extends FiniteLiquidBlock implements ILightable {


    // age 0 = no fire
    // age 1 = startfire
    // age 15 = ded
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;

    public final VoxelShape[] interactionShapes;

    public FlammableLiquidBlock(Supplier<? extends FiniteFluid> supplier, Properties arg) {
        super(supplier, arg.lightLevel((state) -> state.getValue(AGE) > 0 ? 15 : 0));
        this.interactionShapes = IntStream.range(0, 16)
                .mapToObj(i -> box(0, 0, 0, 16, Math.max(0, 15 * (1 - i / (float) this.maxLevel)), 16))
                .toArray(VoxelShape[]::new);
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


    //TODO: check fabric

    @ForgeOverride
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        if (face.getAxis().isVertical()) return 0;
        return 60;
    }

    @ForgeOverride
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        if (face.getAxis().isVertical()) return 0;
        return 300;
    }

    @ForgeOverride
    public void onCaughtFire(BlockState state, Level world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        var newState = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);

        if (GunpowderBlock.isFireSource(neighborState)) {
            newState = newState.setValue(AGE, 1);
        }
        return newState;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            // doesn't set off immediately
            level.scheduleTick(pos, this, getReactToFireDelay());
        }
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        // super also calls schedule fluid tick. unrelated to fire
        super.onPlace(state, world, pos, oldState, moving);
        if (!oldState.is(state.getBlock()) && !world.isClientSide) {
            //doesn't ignite immediately
            world.scheduleTick(pos, this, getReactToFireDelay());
        }
    }

    private int getReactToFireDelay() {
        return 2;
    }

    /**
     * Gets the delay before this block ticks again (without counting random ticks)
     */
    private int getFireTickDelay(RandomSource random) {
        return 30 + random.nextInt(10);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        return interactWithPlayer(state, worldIn, pos, player, handIn);
    }

    @Override
    public boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world, FireSourceType fireSourceType) {
        if (state.getValue(LEVEL) < 10 || world.getFluidState(pos.above()).getType().isSame(state.getFluidState().getType()))
            return false; // prevnt lighting up when too many layers
        return ILightable.super.lightUp(player, state, pos, world, fireSourceType);
    }

    @Override
    public boolean isLitUp(BlockState state, BlockGetter level, BlockPos pos) {
        return isOnFire(state);
    }

    public static boolean isOnFire(BlockState state) {
        return state.getValue(AGE) > 0;
    }

    @Override
    public void setLitUp(BlockState state, LevelAccessor world, BlockPos pos, boolean lit) {
        world.setBlock(pos, state.setValue(AGE, lit ? 1 : 0), 3);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return isOnFire(state) ? interactionShapes[state.getValue(LEVEL)] : super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return this.interactionShapes[state.getValue(LEVEL)];
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile projectile) {
        BlockPos pos = pHit.getBlockPos();
        interactWithProjectile(level, state, projectile, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        boolean shouldBeOnFire = false;
        for (Direction direction : context.getNearestLookingDirections()) {
            if (GunpowderBlock.isFireSource(level, pos.relative(direction))) {
                shouldBeOnFire = true;
                break;
            }
        }
        return state.setValue(AGE, shouldBeOnFire ? 1 : 0);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isOnFire(state)) return;
        if (random.nextInt(24) == 0) {
            level.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }
        int i;
        double d;
        double e;
        double f;
        for (i = 0; i < 3; ++i) {
            d = (double) pos.getX() + random.nextDouble();
            e = (double) pos.getY() + random.nextDouble() * 0.5 + 0.5;
            f = (double) pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Projectile projectile) {
            interactWithProjectile(level, state, projectile, pos);
        }
        // same logic as fire block
        if (isOnFire(state)) {
            if (!entity.fireImmune()) {

                if(CompatHandler.SOUL_FIRED){
                    SoulFiredCompat.setOnFire(entity, 8);
                }else{
                    entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
                    if (entity.getRemainingFireTicks() == 0) {
                        entity.setSecondsOnFire(8);
                    }
                }
            }
            // normal fire damage
            entity.hurt(level.damageSources().inFire(), 1);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && isOnFire(state)) {
            //extinguish sound
            level.levelEvent(null, 1009, pos, 0);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, getFireTickDelay(level.random));
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK) && isOnFire(state)) {

            int age = state.getValue(AGE);
            int layers = state.getValue(LEVEL);
            int ageAdd = random.nextInt(3) / 2;
            int ageIncrease = Math.min(15, age + ageAdd);
            if (age != ageIncrease) {
                state = state.setValue(AGE, ageIncrease);
                level.setBlock(pos, state, 4);
            }

            //  if (age == 15 && random.nextInt(4) == 0 && !SuppPlatformStuff.canCatchFire(level, pos.below(), Direction.UP)) {
            //      level.removeBlock(pos, false);
            //      return;
            //  }

            boolean burnout = level.getBiome(pos).is(BiomeTags.INCREASED_FIRE_BURNOUT);
            int k = burnout ? -50 : 0;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        }

    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // calls fluid random tick
        super.randomTick(state, level, pos, random);

        // hack to burn blocks around like lava does. we could also movethis into tick instead like fire
        if (isOnFire(state)) {
            Blocks.LAVA.randomTick(Blocks.LAVA.defaultBlockState(), level, pos, random);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }
}
