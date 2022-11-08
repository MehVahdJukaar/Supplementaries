package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.base.Suppliers;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidConsumer;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BambooSpikesBlock extends WaterBlock implements ISoftFluidConsumer, EntityBlock, ISoapWashable {
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);
    protected static final VoxelShape SHAPE_UP = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    protected static final VoxelShape SHAPE_DOWN = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SHAPE_WEST = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TIPPED = ModBlockProperties.TIPPED;

    public BambooSpikesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(TIPPED, false));
    }
    
    public static DamageSource getDamageSource(Level level) {
        if ( CommonConfigs.Blocks.BAMBOO_SPIKES_DROP_LOOT.get() && PlatformHelper.getPlatform().isForge()) {
            return new ModDamageSources.SpikePlayer("spike", CommonUtil.getFakePlayer(level)).setProjectile();
        }
        return ModDamageSources.SPIKE_DAMAGE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    //this could be improved
    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof BambooSpikesBlockTile tile) {
            CompoundTag com = stack.getTag();
            if (com != null) {
                Potion p = PotionUtils.getPotion(stack);
                if (p != Potions.EMPTY && com.contains("Damage")) {
                    tile.potion = p;
                    tile.setMissingCharges(com.getInt("Damage"));
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        CompoundTag com = context.getItemInHand().getTag();
        int charges = com != null ? context.getItemInHand().getMaxDamage() - com.getInt("Damage") : 0;
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, flag)
                .setValue(TIPPED, charges != 0 && PotionUtils.getPotion(com) != Potions.EMPTY);
    }

    public ItemStack getSpikeItem(BlockEntity te) {
        if (te instanceof BambooSpikesBlockTile tile) {
            return tile.getSpikeItem();
        }
        return new ItemStack(ModRegistry.BAMBOO_SPIKES_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> list = new ArrayList<>();
        list.add(this.getSpikeItem(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)));
        return list;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
        };
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return Shapes.block();
    }

    //TODO: fix pathfinding

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof Player player && player.isCreative()) return;
        if (entityIn instanceof LivingEntity le && entityIn.isAlive()) {
            boolean up = state.getValue(FACING) == Direction.UP;
            double vy = up ? 0.45 : 0.95;
            //does not reset fall distance
            float fall = entityIn.fallDistance;
            entityIn.makeStuckInBlock(state, new Vec3(0.95D, vy, 0.95D));
            entityIn.fallDistance = fall;

            if (!level.isClientSide) {
                if (up && entityIn instanceof Player && entityIn.isShiftKeyDown()) return;
                float damage = entityIn.getY() > (pos.getY() + 0.0625) ? 3 : 1.5f;
                entityIn.hurt(getDamageSource(level), damage);
                if (state.getValue(TIPPED)) {
                    if (level.getBlockEntity(pos) instanceof BambooSpikesBlockTile te) {
                        if (te.interactWithEntity(le, level)) {
                            level.setBlock(pos, state.setValue(BambooSpikesBlock.TIPPED, false), 3);
                        }
                    }
                }
            }
        }
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return BlockPathTypes.DAMAGE_OTHER;
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public @Nullable BlockPathTypes getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, BlockPathTypes originalType) {
        return BlockPathTypes.DAMAGE_OTHER;
    }

    public static boolean tryAddingPotion(BlockState state, LevelAccessor world, BlockPos pos, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BambooSpikesBlockTile tile && tile.tryApplyPotion(PotionUtils.getPotion(stack))) {
            world.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundSource.BLOCKS, 0.5F, 1.5F);
            world.setBlock(pos, state.setValue(TIPPED, true), 3);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!TIPPED_ENABLED.get()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(handIn);

        if (stack.getItem() instanceof LingeringPotionItem) {
            if (tryAddingPotion(state, worldIn, pos, stack)) {
                if (!player.isCreative())
                    player.setItemInHand(handIn, ItemUtils.createFilledResult(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, TIPPED);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return this.getSpikeItem(level.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return pState.getValue(TIPPED) ? new BambooSpikesBlockTile(pPos, pState) : null;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (0.01 > random.nextFloat() && state.getValue(TIPPED)) {
            if (world.getBlockEntity(pos) instanceof BambooSpikesBlockTile tile) {
                tile.makeParticle(world);
            }
        }
    }

    private static final Supplier<Boolean> TIPPED_ENABLED = Suppliers.memoize(RegistryConfigs.TIPPED_SPIKES_ENABLED::get);

    @Override
    public boolean tryAcceptingFluid(Level world, BlockState state, BlockPos pos, SoftFluid f, @Nullable CompoundTag nbt, int amount) {
        if (!TIPPED_ENABLED.get()) return false;
        if (f == VanillaSoftFluids.POTION.get() && nbt != null && !state.getValue(TIPPED) && nbt.getString("PotionType").equals("Lingering")) {
            if (world.getBlockEntity(pos) instanceof BambooSpikesBlockTile te) {
                if (te.tryApplyPotion(PotionUtils.getPotion(nbt))) {
                    world.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundSource.BLOCKS, 0.5F, 1.5F);
                    world.setBlock(pos, state.setValue(TIPPED, true), 3);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(TIPPED)) {
            if (!level.isClientSide) {
                var te = level.getBlockEntity(pos);
                if (te != null) te.setRemoved();
                level.setBlock(pos, state.setValue(TIPPED, false), 3);
            }
            return true;
        }
        return false;
    }
}