package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FirePitBlock extends LightUpWaterBlock {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    protected static final VoxelShape SHAPE = Shapes.or(Block.box(1.0D, 4.0D, 1.0D, 15.0D, 9.0D, 15.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D));

    private final float fireDamage;

    public FirePitBlock(float fireDamage, BlockBehaviour.Properties properties) {
        super(properties);
        this.fireDamage = fireDamage;
        this.registerDefaultState(this.defaultBlockState().setValue(HANGING, false));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            entity.hurt(level.damageSources().inFire(), this.fireDamage);
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis() == Direction.Axis.Y) {
                BlockState blockstate = this.defaultBlockState().setValue(HANGING, direction == Direction.UP);
                if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
                    return super.getStateForPlacement(context);
                }
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);

        return getDir(stateIn).getOpposite() == facing && !stateIn.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
    }

    private static Direction getDir(BlockState state) {
        return state.getValue(HANGING) ? Direction.DOWN : Direction.UP;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        }
    }


    public static void spawnSmokeParticles(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5D + random.nextDouble() / 2.0D * (random.nextBoolean() ? 1 : -1), pos.getY() + 0.4D, pos.getZ() + 0.5D + random.nextDouble() / 2.0D * (random.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = getDir(state).getOpposite();
        return Block.canSupportCenter(level, pos.relative(direction), direction.getOpposite());
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @ForgeOverride
    @Nullable
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob entity) {
        return isLitUp(state, (LevelAccessor) level, pos) ? BlockPathTypes.DAMAGE_FIRE : null;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }
}