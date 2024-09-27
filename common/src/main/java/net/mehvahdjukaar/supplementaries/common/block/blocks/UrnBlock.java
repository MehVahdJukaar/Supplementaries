package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.UrnBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.FallingUrnEntity;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModCreativeTabs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class UrnBlock extends FallingBlock implements EntityBlock, SimpleWaterloggedBlock {

    public static final MapCodec<UrnBlock> CODEC = simpleCodec(UrnBlock::new);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty TREASURE = ModBlockProperties.TREASURE;

    private static final VoxelShape SHAPE = Shapes.or(box(4, 0, 4, 12, 10, 12),
            box(5, 10, 5, 11, 12, 11),
            box(4, 12, 4, 12, 14, 12));

    public UrnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false).setValue(TREASURE, false));
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return 0x5e341a;
    }

    //falling block
    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getBlock() != oldState.getBlock()) {
            worldIn.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, TREASURE);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    //called when a neighbor is placed
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        worldIn.scheduleTick(currentPos, this, this.getDelayAfterPlace());
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel pLevel, BlockPos pos, RandomSource pRand) {
        if (isFree(pLevel.getBlockState(pos.below())) && pos.getY() >= pLevel.getMinBuildHeight()) {
            CompoundTag tag = null;
            if (pLevel.getBlockEntity(pos) instanceof UrnBlockTile tile) {
                tag = tile.saveWithoutMetadata(pLevel.registryAccess());
                tile.clearContent();
                tile.setRemoved();
            }
            FallingBlockEntity fallingblockentity = FallingUrnEntity.fall(pLevel, pos, state);
            fallingblockentity.blockData = tag;

            this.falling(fallingblockentity);
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        if (!pState.getValue(TREASURE)) {
            return new UrnBlockTile(pPos, pState);
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ItemDisplayTile tile && tile.isEmpty()) {
            return tile.interactWithPlayerItem(player, hand, stack);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof UrnBlockTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (blockState.getValue(TREASURE) || (world.getBlockEntity(pos) instanceof Container tile && !tile.isEmpty()))
            return 15;
        else
            return 0;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        //needed for when it drops from falling block since it has a block entity
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof UrnBlockTile tile) {
            // Idk, why needed, got a crash report for it
            if (!tile.hasLevel()) tile.setLevel(builder.getLevel());
            List<ItemStack> l = super.getDrops(state, builder); //if it's not treasure
            for (int i = 0; i < tile.getContainerSize(); ++i) {
                l.add(tile.getItem(i));
            }
            return l;
        }
        //hax
        ResourceKey<LootTable> tableKey = this.getLootTable();
        if (tableKey == BuiltInLootTables.EMPTY) {
            return super.getDrops(state, builder);
        } else {
            float oldLuck = builder.luck;
            ItemStack stack = builder.getOptionalParameter(LootContextParams.TOOL);
            int f = stack == null ? 0 : getFortuneLevel(stack);
            builder.withLuck(oldLuck + 0.25f * f);
            var lootContext = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
            ServerLevel serverlevel = lootContext.getLevel();
            LootTable loottable = serverlevel.getServer().reloadableRegistries().getLootTable(tableKey);
            List<ItemStack> selectedLoot;
            do {
                selectedLoot = loottable.getRandomItems(lootContext);
                if (selectedLoot.isEmpty()) break;
                //remove disabled stuff. hacky
                selectedLoot = selectedLoot.stream().filter(e -> !ModCreativeTabs.isHidden(e.getItem())).toList();
            } while (selectedLoot.isEmpty());
            return selectedLoot;
        }
    }

    private static int getFortuneLevel(ItemStack stack) {
        var holder = DynamicHolder.of(Enchantments.FORTUNE);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }

    @Override
    public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        if (pLevel.isClientSide && getFortuneLevel(pPlayer.getUseItem()) == 0) {
            spawnExtraBrokenParticles(pState, pPos, pLevel);
        }
        return super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    public static void spawnExtraBrokenParticles(BlockState state, BlockPos pos, Level level) {
        if (level.isClientSide && state.getValue(TREASURE)) {
            level.addDestroyBlockEffect(pos, state);
            if (level.random.nextInt(20) == 0) {
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.3125;
                double z = pos.getZ() + 0.5;
                level.addParticle(ParticleTypes.SOUL, x, y, z, 0, 0.05, 0);
                float f = level.random.nextFloat() * 0.4F + level.random.nextFloat() > 0.9F ? 0.6F : 0.0F;
                level.playSound(null, x, y, z, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, f, 0.6F + level.random.nextFloat() * 0.4F);
            }
        }
    }

    @Override
    public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
        super.onProjectileHit(pLevel, pState, pHit, pProjectile);
        BlockPos pos = pHit.getBlockPos();
        pLevel.destroyBlock(pos, true);
        spawnExtraBrokenParticles(pState, pos, pLevel);
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean bl) {
        super.spawnAfterBreak(state, level, pos, stack, bl);
        if (level.random.nextFloat() < CommonConfigs.Functional.URN_ENTITY_SPAWN_CHANCE.get() &&
                level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) &&
                !EnchantmentHelper.hasTag(stack, EnchantmentTags.PREVENTS_INFESTED_SPAWNS)) {
            List<EntityType<?>> list = new ArrayList<>();
            for (var e : BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(ModTags.URN_SPAWN)) {
                list.add(e.value());
            }

            if (!list.isEmpty()) {
                var e = list.get(level.getRandom().nextInt(list.size()));
                Entity entity = e.create(level);
                if (entity != null) {
                    if (entity instanceof Slime slime) slime.setSize(0, true);
                    entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                    level.addFreshEntity(entity);
                }
            }
        }
    }
}