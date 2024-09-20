package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.integration.AmendmentsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FirePitBlock extends LightUpWaterBlock {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D),
            Block.box(0.0D, 2.0D, 0.0D, 16, 5, 16));

    private final float fireDamage;

    public FirePitBlock(float fireDamage, BlockBehaviour.Properties properties) {
        super(properties.lightLevel((state) -> state.getValue(LIT) ? 15 : 0));
        this.fireDamage = fireDamage;
        this.registerDefaultState(this.defaultBlockState().setValue(HANGING, false));
    }

    @ForgeOverride
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        if (isLitUp(state, level, pos)) return PathType.DAMAGE_FIRE;
        else return null;
    }

    @ForgeOverride
    public @Nullable PathType getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, PathType originalType) {
        if (isLitUp(state, level, pos)) return PathType.DAMAGE_FIRE;
        else return null;
    }


    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            entity.hurt(level.damageSources().inFire(), this.fireDamage);
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HANGING) && CompatHandler.AMENDMENTS &&
                AmendmentsCompat.canSurviveCeilingAndMaybeFall(state, pos, level)) {
            return true;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return super.getStateForPlacement(context)
                .setValue(HANGING, IRopeConnection.isSupportingCeiling(pos.above(), level));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        stateIn = super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
        if(facing == Direction.UP){
            stateIn = stateIn.setValue(HANGING, IRopeConnection.isSupportingCeiling(currentPos.above(), level));
        }
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
        if (state.getValue(LIT)) {
            if (random.nextInt(10) == 0) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }
            for (int i = 0; i < 3; ++i) {
                double x = pos.getX() + 0.5 + Mth.randomBetween(random, -0.25f, 0.25f);
                double y = pos.getY() + random.nextDouble() * 0.5 + 6 / 16f;
                double z = pos.getZ() + 0.5 + Mth.randomBetween(random, -0.25f, 0.25f);
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
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