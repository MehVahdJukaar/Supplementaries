package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.block.IPistonMotionReact;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidConsumer;
import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem.getPotion;

public class BambooSpikesBlock extends WaterBlock implements ISoftFluidConsumer, EntityBlock, IWashable, IPistonMotionReact {
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

    private static final GameProfile SPIKE_PLAYER = new GameProfile(UUID.randomUUID(), "Spike Fake Player");

    public static DamageSource getDamageSource(Level level) {
        if (CommonConfigs.Functional.BAMBOO_SPIKES_DROP_LOOT.get()) {
            ServerPlayer fakePlayer = (ServerPlayer) FakePlayerManager.get(SPIKE_PLAYER, level);
            fakePlayer.getAdvancements().stopListening();
            fakePlayer.setGameMode(GameType.SPECTATOR);
            return ModDamageSources.spikePlayer(fakePlayer);
        }
        return ModDamageSources.spike();
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
            PotionContents p = getPotion(stack);
            int charges = stack.getOrDefault(ModComponents.CHARGES.get(), 0);
            tile.tryApplyPotion(p, charges);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        var damage = stack.get(DataComponents.DAMAGE);
        boolean hasPotion = stack.has(DataComponents.POTION_CONTENTS);
        int charges = damage != null ? stack.getMaxDamage() - damage : 0;
        return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace())
                .setValue(TIPPED, charges != 0 && hasPotion);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
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
                            level.gameEvent(entityIn, GameEvent.BLOCK_CHANGE, pos);
                        }
                    }
                }
            }
        }
    }

    @ForgeOverride
    public @Nullable PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.DAMAGE_OTHER;
    }

    @ForgeOverride
    public @Nullable PathType getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, PathType originalType) {
        return PathType.DAMAGE_OTHER;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (!TIPPED_ENABLED.get() || state.getValue(TIPPED))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stack.getItem() instanceof LingeringPotionItem) {
            PotionContents potion = getPotion(stack);
            if (tryAddingPotion(state, level, pos, potion, player)) {
                if (!player.isCreative())
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, TIPPED);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
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

    private static final Supplier<Boolean> TIPPED_ENABLED = Suppliers.memoize(CommonConfigs.Functional.TIPPED_SPIKES_ENABLED::get);

    @Override
    public boolean tryAcceptingFluid(Level world, BlockState state, BlockPos pos, SoftFluidStack fluid) {
        if (!TIPPED_ENABLED.get() || state.getValue(TIPPED)) return false;
        if (fluid.is(BuiltInSoftFluids.POTION) && PotionBottleType.getOrDefault(fluid) == PotionBottleType.LINGERING) {
            var content = getPotion(fluid);
            return tryAddingPotion(state, world, pos, content, null);
        }
        return false;
    }

    public static boolean tryAddingPotion(BlockState state, LevelAccessor world, BlockPos pos, PotionContents potion, @Nullable Entity adder) {
        world.setBlock(pos, state.setValue(TIPPED, true), 0);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BambooSpikesBlockTile tile && tile.tryApplyPotion(potion)) {
            world.playSound(null, pos, SoundEvents.HONEY_BLOCK_FALL, SoundSource.BLOCKS, 0.5F, 1.5F);
            world.setBlock(pos, state.setValue(TIPPED, true), 3);
            world.gameEvent(adder, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        if (te != null) te.setRemoved();
        world.setBlock(pos, state.setValue(TIPPED, false), 0);
        return false;
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {
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

    @Override
    public boolean ticksWhileMoved() {
        return true;
    }

    @Override
    public void moveTick(Level level, BlockPos pos, BlockState movedState, AABB aabb, PistonMovingBlockEntity tile) {
        boolean sameDir = (movedState.getValue(BambooSpikesBlock.FACING).equals(tile.getDirection()));
        if (CompatHandler.QUARK) QuarkCompat.tickPiston(level, pos, movedState, aabb, sameDir, tile);
    }

    public ItemStack getSpikeItem(@Nullable BlockEntity te) {
        ItemStack stack = new ItemStack(this);
        if (te instanceof BambooSpikesBlockTile tile) {
            return BlockUtil.saveTileToItem(tile);
        }
        return stack;
    }
}