package net.mehvahdjukaar.supplementaries.common.block.blocks;

import dev.architectury.injectables.annotations.PlatformOnly;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.List;

public class CandleSkullBlock extends AbstractCandleBlock implements EntityBlock, ILightable, ISoapWashable {

    private static final Int2ObjectMap<List<Vec3>> PARTICLE_OFFSETS = Util.make(() -> {
        Int2ObjectMap<List<Vec3>> map = new Int2ObjectOpenHashMap<>();
        map.defaultReturnValue(List.of());
        map.put(1, List.of(new Vec3(0.5D, 0.5 + 0.5D, 0.5D)));
        map.put(2, List.of(new Vec3(0.375D, 0.5 + 0.44D, 0.5D), new Vec3(0.625D, 0.5 + 0.5D, 0.44D)));
        map.put(3, List.of(new Vec3(0.5D, 0.5 + 0.313D, 0.625D), new Vec3(0.375D, 0.5 + 0.44D, 0.5D), new Vec3(0.56D, 0.5 + 0.5D, 0.44D)));
        map.put(4, List.of(new Vec3(0.44D, 0.5 + 0.313D, 0.56D), new Vec3(0.625D, 0.5 + 0.44D, 0.56D), new Vec3(0.375D, 0.5 + 0.44D, 0.375D), new Vec3(0.56D, 0.5 + 0.5D, 0.375D)));
        return Int2ObjectMaps.unmodifiable(map);
    });

    protected static final VoxelShape BASE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);

    private static final VoxelShape ONE_AABB = Shapes.or(BASE, Block.box(7.0D, 8.0D, 7.0D, 9.0D, 14.0D, 9.0D));
    private static final VoxelShape TWO_AABB = Shapes.or(BASE, Block.box(5.0D, 8.0D, 6.0D, 11.0D, 14.0D, 9.0D));
    private static final VoxelShape THREE_AABB = Shapes.or(BASE, Block.box(5.0D, 8.0D, 6.0D, 10.0D, 14.0D, 11.0D));
    private static final VoxelShape FOUR_AABB = Shapes.or(BASE, Block.box(5.0D, 8.0D, 5.0D, 11.0D, 14.0D, 10.0D));

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;

    public CandleSkullBlock(Properties properties) {
        super(properties.lightLevel(CandleBlock.LIGHT_EMISSION));
        this.registerDefaultState(this.defaultBlockState().setValue(CANDLES, 1)
                .setValue(ROTATION, 0).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(CANDLES, LIT, ROTATION);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CandleSkullBlockTile(pPos, pState);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CandleSkullBlockTile tile) {
            List<ItemStack> loot = tile.getCandle().setValue(CANDLES, state.getValue(CANDLES)).getDrops(builder);

            BlockEntity skullTile = tile.getSkullTile();
            if (skullTile != null) {
                BlockState skull = skullTile.getBlockState();
                builder = builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, skullTile);
                loot.addAll(skull.getDrops(builder));
            }

            return loot;
        }
        return super.getDrops(state, builder);
    }

    //crappy for fabric
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
            return tile.getSkullItem();
        }
        return super.getCloneItemStack(level, pos, state);
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public ItemStack getCloneItemStack(BlockState state, HitResult hitResult, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
            double y = hitResult.getLocation().y;
            boolean up = y % ((int) y) > 0.5d;
            return up ? tile.getCandle().getBlock().getCloneItemStack(world, pos, state) : tile.getSkullItem();
        }
        return super.getCloneItemStack(world, pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(CANDLES)) {
            default -> ONE_AABB;
            case 2 -> TWO_AABB;
            case 3 -> THREE_AABB;
            case 4 -> FOUR_AABB;
        };
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
        return PARTICLE_OFFSETS.get(pState.getValue(CANDLES).intValue());
    }

    //same as ILightUpBlock (todo: try to merge)
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        if (player.getAbilities().mayBuild) {
            ItemStack stack = player.getItemInHand(hand);
            //add candles
            if (stack.is(ItemTags.CANDLES) && stack.getItem() instanceof BlockItem blockItem) {
                int count = state.getValue(CANDLES);
                if (count < 4 && CommonConfigs.Tweaks.SKULL_CANDLES_MULTIPLE.get() &&
                        level.getBlockEntity(pos) instanceof CandleSkullBlockTile tile
                        && tile.getCandle().getBlock().asItem() == stack.getItem()) {

                    SoundType sound = blockItem.getBlock().defaultBlockState().getSoundType();
                    level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    }
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                    level.setBlock(pos, state.setValue(CANDLES, count + 1), 2);

                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                return InteractionResult.PASS;
            }
            //lightable logic
            return interactWithPlayer(state, level, pos, player, hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isLitUp(BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    public BlockState toggleLitState(BlockState state, boolean lit) {
        return state.setValue(LIT, lit);
    }

    @Override
    public boolean canBeExtinguishedBy(ItemStack item) {
        return item.isEmpty() || ILightable.super.canBeExtinguishedBy(item);
    }

    @Override
    public void playExtinguishSound(LevelAccessor world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void spawnSmokeParticles(BlockState state, BlockPos pos, LevelAccessor level) {
        ((CandleSkullBlock) state.getBlock()).getParticleOffsets(state).forEach((vec3) -> {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + vec3.x(), pos.getY() + vec3.y(), pos.getZ() + vec3.z(), 0.0, 0.10000000149011612, 0.0);
        });
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
            var c = tile.getCandle();
            if (c != null) {
                var n = BlocksColorAPI.changeColor(c.getBlock(), null);
                if (n != null && n != c.getBlock()) {
                    tile.setCandle(n.withPropertiesOf(c));
                    tile.setChanged();
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return Utils.getTicker(type, ModRegistry.SKULL_CANDLE_TILE.get(), CandleSkullBlockTile::tick);
    }

}
