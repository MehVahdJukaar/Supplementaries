package net.mehvahdjukaar.supplementaries.common.fluids;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.SoulFiredCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
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

    public FlammableLiquidBlock(Supplier<? extends FiniteFluid> supplier, Properties arg, int baseLight) {
        super(supplier, arg.lightLevel((state) -> state.getValue(AGE) > baseLight ? 15 : 0));
        this.interactionShapes = IntStream.range(0, 16)
                .mapToObj(i -> box(0, 0, 0, 16, Math.max(0, 15 * (1 - i / (float) this.maxLevel)), 16))
                .toArray(VoxelShape[]::new);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
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

    @ForgeOverride
    public void onCaughtFire(BlockState state, Level world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level instanceof ServerLevel sl) {
            // doesn't set off immediately
            MiscUtils.scheduleTickOverridingExisting(sl, pos, this, getReactToFireDelay());
        }
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        // super also calls schedule fluid tick. unrelated to fire
        super.onPlace(state, world, pos, oldState, moving);
        if (!oldState.is(state.getBlock()) && world instanceof ServerLevel sl) {
            //doesn't ignite immediately
            MiscUtils.scheduleTickOverridingExisting(sl, pos, this, getReactToFireDelay());
        }
    }

    protected int getReactToFireDelay() {
        return 2;
    }

    /**
     * Gets the delay before this block ticks again (without counting random ticks)
     */
    protected int getFireTickDelay(RandomSource random) {
        return 30 + random.nextInt(10);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        return this.interactWithPlayer(state, worldIn, pos, player, handIn);
    }

    @Override
    public boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor level, FireSourceType fireSourceType) {
        //extra can light up checks
        if (shouldNotHaveFire(state, pos, level)) return false;
        var success = ILightable.super.lightUp(player, state, pos, level, fireSourceType);
        if (success && level instanceof ServerLevel sl) {
            MiscUtils.scheduleTickOverridingExisting(sl, pos, this, getReactToFireDelay());
        }
        return success;
    }

    //TODO: in fire check
    public static boolean shouldNotHaveFire(BlockState state, BlockPos pos, LevelAccessor levelAccessor) {
        return
                //state.getValue(MISSING_LEVELS) < 5 || //its inverted
                levelAccessor.getFluidState(pos.above()).is(state.getFluidState().getType());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        var newShape = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        if (isLitUp(state, level, currentPos) && shouldNotHaveFire(newShape, currentPos, level)) {
            return newShape.setValue(AGE, 0);
        }
        return newShape;
    }

    @Override
    public boolean isLitUp(BlockState state, BlockGetter level, BlockPos pos) {
        return FireStage.fromAge(state.getValue(AGE)).isBurning();
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
        return isLitUp(state, level, pos) ? interactionShapes[state.getValue(MISSING_LEVELS)] : super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return this.interactionShapes[state.getValue(MISSING_LEVELS)];
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile projectile) {
        BlockPos pos = pHit.getBlockPos();
        this.interactWithEntity(level, state, projectile, pos);
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
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Projectile projectile) {
            this.interactWithEntity(level, state, projectile, pos);
        }
        // same logic as fire block
        if (isLitUp(state, level, pos)) {
            if (!entity.fireImmune()) {

                if (CompatHandler.SOUL_FIRED) {
                    SoulFiredCompat.setOnFire(entity, 8);
                } else {
                    entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
                    if (entity.getRemainingFireTicks() == 0) {
                        entity.setSecondsOnFire(8);
                    }
                }
            }
            // normal fire damage
            entity.hurt(level.damageSources().inFire(), 1);

        } else if (entity.isOnFire()) {
            this.lightUp(entity, state, pos, level, FireSourceType.FLAMING_ARROW);
        }
        Integer duration = CommonConfigs.Functional.FLAMMABLE_FROM_LUMISENE.get();
        if (entity instanceof LivingEntity le && duration > 0) {
            le.addEffect(new MobEffectInstance(ModRegistry.FLAMMABLE.get(), duration,
                    0, false, false));
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && isLitUp(state, level, pos)) {
            //extinguish sound
            level.levelEvent(null, 1009, pos, 0);
        }
        super.playerWillDestroy(level, pos, state, player);
    }


    // fire block stuff

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isLitUp(state, level, pos)) return;
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                    1.0F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.3F, false);
        }

        int age = state.getValue(AGE);
        double baseY = Math.max((age + 1) / 5, 1) * 0.5f;
        for (int i = 0; i < 3; ++i) {
            double x = (double) pos.getX() + random.nextDouble();
            double y = (double) pos.getY() + random.nextDouble() * 0.25 + baseY;
            double z = (double) pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (SuppPlatformStuff.canCatchFire(level, pos.relative(direction), direction)) {
                for (int i = 0; i < 2; ++i) {
                    var step = direction.step();
                    double x = pos.getX() + 0.5 + step.x * 0.5 + (step.x == 0 ? (random.nextDouble() - 0.5) : (-step.x * random.nextDouble() * 0.1));
                    double y = pos.getY() + 0.5 + step.y * 0.5 + (step.y == 0 ? (random.nextDouble() - 0.5) : (-step.y * random.nextDouble() * 0.1));
                    double z = pos.getZ() + 0.5 + step.z * 0.5 + (step.z == 0 ? (random.nextDouble() - 0.5) : (-step.z * random.nextDouble() * 0.1));
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) return;
        int age = state.getValue(AGE);
        FireStage stage = FireStage.fromAge(age);

        if (stage == FireStage.OFF) {
            // lights up from neighbors
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN) continue;
                if (GunpowderBlock.isFireSource(level, pos.relative(dir))) {
                    //plays sound too
                    this.lightUp(null, state, pos, level, FireSourceType.FLAMING_ARROW);
                    return;
                }
            }
            return;
        }

        // super.tick(state, level, pos, random);
        if (stage == FireStage.RISING) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 3);
            MiscUtils.scheduleTickOverridingExisting(level, pos, this, getReactToFireDelay());
            return;
        }

        if (stage == FireStage.DYING) {
            int missingLayers = state.getValue(MISSING_LEVELS);
            // remove block/ turn to fire
            if (missingLayers == 15) {
                // cant remove block or liquid would be left behind
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                if (SuppPlatformStuff.canCatchFire(level, pos.below(), Direction.UP)) {
                    // replace with fire block if it has flammable below. Since we cant burn down
                    int newAge = random.nextInt(8);
                    level.setBlock(pos, getFireDelegate().getStateWithAge(level, pos, newAge), 3);
                }
                return;
            }
            // burns down. Starts at raging age
            level.setBlockAndUpdate(pos, state.setValue(MISSING_LEVELS, missingLayers + 1)
                    .setValue(AGE, 4));
            level.scheduleTick(pos, this, getFireTickDelay(level.random));
            return;
        }

        if (stage == FireStage.RAGING) {
            level.scheduleTick(pos, this, getFireTickDelay(level.random));

            //tick normal fire
            int ageAdd = random.nextInt(3) / 2;
            int ageIncrease = Math.min(15, age + ageAdd);
            if (age != ageIncrease) {
                state = state.setValue(AGE, ageIncrease);
                level.setBlock(pos, state, Block.UPDATE_INVISIBLE);
            }


            burnStuffAroundLikeFire(state, level, pos, random, age);
        }


    }

    //TODO: add fire spread speed

    @ForgeOverride
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return isLitUp(state, level, pos) ? 0 : 60;
        //high chance to have fire. Cant burn however
    }


    // Mimics what FireBlock .tick does
    private static void burnStuffAroundLikeFire(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, int age) {
        boolean increaseFireBurnout = level.getBiome(pos).is(BiomeTags.INCREASED_FIRE_BURNOUT);
        int extraChance = increaseFireBurnout ? -50 : 0;
        SuppPlatformStuff.tryBurningByFire(level, pos.east(), 300 + extraChance, random, age, Direction.WEST);
        SuppPlatformStuff.tryBurningByFire(level, pos.west(), 300 + extraChance, random, age, Direction.EAST);
        SuppPlatformStuff.tryBurningByFire(level, pos.above(), 250 + extraChance, random, age, Direction.DOWN);
        SuppPlatformStuff.tryBurningByFire(level, pos.north(), 300 + extraChance, random, age, Direction.SOUTH);
        SuppPlatformStuff.tryBurningByFire(level, pos.south(), 300 + extraChance, random, age, Direction.NORTH);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                for (int dy = -1; dy <= 4; ++dy) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    int chance = 100;
                    if (dy > 1) {
                        chance += (dy - 1) * 100;
                    }

                    mutableBlockPos.setWithOffset(pos, dx, dy, dz);
                    FireBlock fireBlock = getFireDelegate();
                    int igniteOdds = fireBlock.getIgniteOdds(level, mutableBlockPos);
                    // purposefully cant ignite other lumisene next to it. would look bad
                    boolean isLumisene = false;
                    if (igniteOdds == 0 && (dy != 0 || (dx != 0 && dz != 0))) {
                        // same as mixin
                        BlockState nextState = level.getBlockState(pos);
                        if (state.getBlock() instanceof FlammableLiquidBlock) {
                            igniteOdds = PlatHelper.getFireSpreadSpeed(nextState, level, pos, Direction.UP);
                            isLumisene = true;
                        }
                    }
                    if (igniteOdds > 0) {
                        int i2 = (igniteOdds + 40 + level.getDifficulty().getId() * 7) / (age + 30);
                        if (increaseFireBurnout) {
                            i2 /= 2;
                        }

                        if (i2 > 0 && random.nextInt(chance) <= i2 && (!level.isRaining() || !fireBlock.isNearRain(level, mutableBlockPos))) {
                            int newAge = Math.min(15, age + random.nextInt(5) / 4);

                            if (isLumisene && level.getBlockState(pos).getBlock() instanceof FlammableLiquidBlock fl) {
                                fl.lightUp(null, state, pos, level, FireSourceType.FLAMING_ARROW);
                            } else {
                                // sets fire block
                                level.setBlock(mutableBlockPos, fireBlock.getStateWithAge(level, mutableBlockPos, newAge), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    @ForgeOverride
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        if (isLitUp(state, level, pos)) return BlockPathTypes.DAMAGE_FIRE;
        else return null;
    }

    @ForgeOverride
    public @Nullable BlockPathTypes getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, BlockPathTypes originalType) {
        if (isLitUp(state, level, pos)) return BlockPathTypes.DAMAGE_FIRE;
        else return null;
    }

    private static @NotNull FireBlock getFireDelegate() {
        return (FireBlock) Blocks.FIRE;
    }


    public enum FireStage {
        OFF, RISING, RAGING, DYING;

        public boolean isBurning() {
            return this != OFF;
        }

        public static FireStage fromAge(int age) {
            if (age == 0) return OFF;
            if (age == 15) return DYING;
            else if (age < 4) return RISING;
            else return RAGING;
        }
    }

}
