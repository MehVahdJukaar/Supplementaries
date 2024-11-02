package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BubbleBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BubbleBlock extends Block implements EntityBlock {

    public BubbleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext placeContext) {
        return !placeContext.isSecondaryUseActive();
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
        if (collisionContext.isAbove(Shapes.block(), pos, false)
                && collisionContext instanceof EntityCollisionContext ec && ec.getEntity() instanceof LivingEntity)
            return Shapes.block();
        else return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (CommonConfigs.Tools.BUBBLE_BREAK.get() && level instanceof ServerLevel serverLevel) {
            breakBubble(serverLevel, pos, state);
        }
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        makeParticle(pos, level);
        playBreakSound(state, level, pos, player);
    }

    private void playBreakSound(BlockState state, Level level, BlockPos pos, Player player) {
        SoundType soundtype = state.getSoundType();
        level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
    }

    @ForgeOverride
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    public void makeParticle(BlockPos pos, Level level) {
        level.addParticle(ModParticles.BUBBLE_BLOCK_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
    }

    public void sendParticles(BlockPos pos, ServerLevel level) {
        level.sendParticles(ModParticles.BUBBLE_BLOCK_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1, 0, 0, 0, 0);
    }

    public void breakBubble(ServerLevel level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
        sendParticles(pos, level);
        playBreakSound(state, level, pos, null);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (canPopBubble(entity)) level.scheduleTick(pos, this, 5);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float v) {
        super.fallOn(level, state, pos, entity, v);
        if (!level.isClientSide && CommonConfigs.Tools.BUBBLE_BREAK.get()) {
            if (canPopBubble(entity)) {
                if (v > 3) breakBubble((ServerLevel) level, pos, state);
                else level.scheduleTick(pos, this, (int) Mth.clamp(7 - v / 2, 1, 5));
            }
        }
    }

    private static final DynamicHolder<Enchantment> FEATHER_FALLING = DynamicHolder.of(Enchantments.FEATHER_FALLING);

    protected boolean canPopBubble(Entity entity) {
        if (!CommonConfigs.Tools.BUBBLE_BREAK.get()) return false;
        if (entity instanceof LivingEntity le) {
            return !CommonConfigs.Tools.BUBBLE_FEATHER_FALLING.get() ||
                    EnchantmentHelper.getEnchantmentLevel(FEATHER_FALLING, le) == 0;
        }
        return true;
    }

    @Override
    public void tick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random) {
        breakBubble(serverLevel, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BubbleBlockTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> tBlockEntityType) {
        return Utils.getTicker(tBlockEntityType, ModRegistry.BUBBLE_BLOCK_TILE.get(), BubbleBlockTile::tick);
    }
}
