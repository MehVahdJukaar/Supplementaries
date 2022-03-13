package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.entities.FallingAshEntity;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class AshLayerBlock extends FallingBlock {
    private static final int MAX_LAYERS = 8;
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[MAX_LAYERS + 1];

    static {
        Arrays.setAll(SHAPE_BY_LAYER, l -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, l * 2, 16.0D));
        SHAPE_BY_LAYER[0] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.1f, 16.0D);
    }

    public AshLayerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult pHit, Projectile projectile) {
        BlockPos pos = pHit.getBlockPos();
        if (projectile instanceof ThrownPotion potion && PotionUtils.getPotion(potion.getItem()) == Potions.WATER) {
            Entity entity = projectile.getOwner();
            boolean flag = entity == null || entity instanceof Player || ForgeEventFactory.getMobGriefingEvent(level, entity);
            if (flag) {
                this.removeOneLayer(state, pos, level);
            }
        }
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return 0x9a9090;
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getBlock() != oldState.getBlock())
            worldIn.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pContext instanceof EntityCollisionContext c) {
            var e = c.getEntity();
            if (e instanceof LivingEntity) {
                return SHAPE_BY_LAYER[pState.getValue(LAYERS) - 1];
            }
        }
        return this.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter blockGetter, BlockPos pos, PathComputationType pathType) {
        if (pathType == PathComputationType.LAND) {
            return state.getValue(LAYERS) <= MAX_LAYERS / 2;
        }
        return false;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    //ugly but works
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos otherPos) {
        if (world instanceof ServerLevel serverLevel) {
            BlockPos pos = currentPos.above();
            BlockState state1 = world.getBlockState(pos);
            ;
            while (state1.is(this)) {
                serverLevel.scheduleTick(pos, this, this.getDelayAfterPlace());
                pos = pos.above();
                state1 = serverLevel.getBlockState(pos);
            }
        }
        return super.updateShape(state, direction, facingState, world, currentPos, otherPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random pRand) {
        BlockState below = level.getBlockState(pos.below());
        if ((FallingAshEntity.isFree(below) || hasIncompleteAshPileBelow(below)) && pos.getY() >= level.getMinBuildHeight()) {

            while (state.is(this)) {
                FallingBlockEntity fallingblockentity = FallingAshEntity.fall(level, pos, state);
                this.falling(fallingblockentity);

                pos = pos.above();
                state = level.getBlockState(pos);
            }
        }
    }

    private boolean hasIncompleteAshPileBelow(BlockState state) {
        return state.is(this) && state.getValue(LAYERS) != MAX_LAYERS;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(MAX_LAYERS, i + 1));
        } else {
            return super.getStateForPlacement(context);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel level, BlockPos pPos, Random pRandom) {
        if (ServerConfigs.cached.ASH_RAIN) {
            if (level.isRainingAt(pPos.above()) && level.random.nextInt(4) == 0) {
                this.removeOneLayer(pState, pPos, level);
            }
        }
    }

    @Override
    public void handlePrecipitation(BlockState pState, Level level, BlockPos pPos, Biome.Precipitation pPrecipitation) {
        super.handlePrecipitation(pState, level, pPos, pPrecipitation);
        if (ServerConfigs.cached.ASH_RAIN) {
            if (level.random.nextInt(2) == 0) {
                this.removeOneLayer(pState, pPos, level);
            }
        }
    }

    private void removeOneLayer(BlockState state, BlockPos pos, Level level) {
        int levels = state.getValue(LAYERS);
        if (levels > 1) level.setBlockAndUpdate(pos, state.setValue(LAYERS, levels - 1));
        else level.removeBlock(pos, false);
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        int i = pState.getValue(LAYERS);
        if (pUseContext.getItemInHand().is(this.asItem()) && i < MAX_LAYERS) {
            return true;
        } else {
            return i == 1;
        }
    }

    public static boolean tryConvertToAsh(Level level, BlockPos pPos) {
        if (ServerConfigs.cached.ASH_BURN) {
            BlockState state = level.getBlockState(pPos);

            Item i = state.getBlock().asItem();
            int count = ForgeHooks.getBurnTime(i.getDefaultInstance(), null) / 100;
            if (i.builtInRegistryHolder().is(ItemTags.LOGS_THAT_BURN)) count += 2;

            if (count > 0) {
                int layers = Mth.clamp(level.random.nextInt(count), 1, 8);
                if (layers != 0) {
                    ((ServerLevel) level).sendParticles(ModRegistry.ASH_PARTICLE.get(), (double) pPos.getX() + 0.5D,
                            (double) pPos.getY() + 0.5D, (double) pPos.getZ() + 0.5D, 10 + layers,
                            0.5D, 0.5D, 0.5D, 0.0D);
                    return level.setBlock(pPos, ModRegistry.ASH_BLOCK.get()
                            .defaultBlockState().setValue(AshLayerBlock.LAYERS, layers), 3);
                }
            }
        }
        return false;
    }

    private void addParticle(Entity entity, BlockPos pos, Level level, int layers, float upSpeed) {
        level.addParticle(ModRegistry.ASH_PARTICLE.get(), entity.getX(), pos.getY() + layers * (1 / 8f), entity.getZ(),
                Mth.randomBetween(level.random, -1.0F, 1.0F) * 0.083333336F,
                upSpeed,
                Mth.randomBetween(level.random, -1.0F, 1.0F) * 0.083333336F);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide && level.random.nextInt(8) == 0 && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
            addParticle(entity, pos, level, state.getValue(LAYERS), 0.05f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float height) {
        int layers = state.getValue(LAYERS);
        entity.causeFallDamage(height, layers > 2 ? 0.3f : 1, DamageSource.FALL);
        if (level.isClientSide) {
            for (int i = 0; i < Math.min(12, height * 1.4); i++) {

                addParticle(entity, pos, level, layers, 0.12f);
            }
        }
    }

    //TODO: bonemeal thing
    public static boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, Player player) {
        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.getBlock() instanceof BonemealableBlock bonemealableblock) {
            if (bonemealableblock.isValidBonemealTarget(level, pos, blockstate, level.isClientSide)) {

                if (level instanceof ServerLevel) {
                    if (bonemealableblock.isBonemealSuccess(level, level.random, pos, blockstate)) {
                        bonemealableblock.performBonemeal((ServerLevel) level, level.random, pos, blockstate);
                    }

                    stack.shrink(1);
                }

                return true;
            }
        }

        return false;
    }

    //TODO: add this
    public static final int GRASS_SPREAD_WIDTH = 3;
    /*
    public void performBonemeal(ServerLevel level, Random random, BlockPos pos, BlockState state) {
        BlockPos blockpos = pos.above();
        BlockState blockstate = Blocks.GRASS.defaultBlockState();

        label46:
        for(int i = 0; i < 128; ++i) {
            BlockPos pos1 = blockpos;

            for(int j = 0; j < i / 16; ++j) {
                pos1 = pos1.offset(random.nextInt(GRASS_SPREAD_WIDTH) - 1,
                        (random.nextInt(GRASS_SPREAD_WIDTH) - 1) * random.nextInt(3) / 2,
                        random.nextInt(GRASS_SPREAD_WIDTH) - 1);
                if (!level.getBlockState(pos1.below()).is(this) ||
                        level.getBlockState(pos1).isCollisionShapeFullBlock(level, pos1)) {
                    continue label46;
                }
            }

            BlockState state1 = level.getBlockState(pos1);
            //if (state1.is(blockstate.getBlock()) && random.nextInt(10) == 0) {
            //    ((BonemealableBlock)blockstate.getBlock()).performBonemeal(level, random, pos1, state1);
            //}

            if (state1.isAir()) {
                PlacedFeature placedfeature;
                if (random.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> list = level.getBiome(pos1).getGenerationSettings().getFlowerFeatures();
                    if (list.isEmpty()) {
                        continue;
                    }

                    placedfeature = ((RandomPatchConfiguration)list.get(0).config()).feature().get();
                } else {
                    placedfeature = VegetationPlacements.GRASS_BONEMEAL;
                }

                placedfeature.place(level, level.getChunkSource().getGenerator(), random, pos1);
            }
        }

    }
    */

}
