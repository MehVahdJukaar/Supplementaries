package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CannonBallExplosion extends Explosion {

    private final BlockPos centerPos;
    private final float maxExplodedAmount;

    private float explosionAmountLeft;

    public CannonBallExplosion(Level level, @Nullable Entity source, double toBlowX, double toBlowY, double toBlowZ,
                               BlockPos centerPos, float maxExplodedAmount, float maxRadius) {
        super(level, source, toBlowX, toBlowY, toBlowZ, maxRadius, false, BlockInteraction.DESTROY);
        this.centerPos = centerPos;
        this.maxExplodedAmount = maxExplodedAmount;
    }

    //client factory
    public CannonBallExplosion(Level level, @Nullable Entity source, double toBlowX, double toBlowY, double toBlowZ,
                               float radius, List<BlockPos> toBlow) {
        super(level, source, toBlowX, toBlowY, toBlowZ, radius, toBlow);
        this.centerPos = BlockPos.containing(new Vec3(toBlowX, toBlowY, toBlowZ));
        this.maxExplodedAmount = 0;
    }

    @Override
    public void explode() {
        Vec3 center = new Vec3(this.x, this.y, this.z);
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toVisit = new ArrayDeque<>();
        AtomicReference<Float> explosionBudget = new AtomicReference<>(maxExplodedAmount); // Global budget

        toVisit.add(centerPos);

        while (!toVisit.isEmpty() && explosionBudget.get() > 0.25) {
            BlockPos currentPos = toVisit.poll();
            visit(currentPos, center, explosionBudget, this.getToBlow(), visited, toVisit);
        }
        this.explosionAmountLeft = explosionBudget.get();
    }

    public float getExploded() {
        return maxExplodedAmount - explosionAmountLeft;
    }

    private void visit(BlockPos pos, Vec3 center,
                       AtomicReference<Float> explosionBudget,
                       List<BlockPos> toExplode,
                       Set<BlockPos> visited,
                       Queue<BlockPos> toVisit) {

        float r = this.radius + level.random.nextFloat();
        if (!pos.equals(centerPos) && center.distanceToSqr(pos.getCenter()) > (r * r))
            return;
        if (!level.isInWorldBounds(pos) || visited.contains(pos))
            return;

        visited.add(pos);
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);

        boolean canPropagateExplosion = false;
        if (!blockState.isAir()) {
            Optional<Float> optional = damageCalculator.getBlockExplosionResistance(this, level, pos, blockState, fluidState);
            if (optional.isPresent()) {
                float resistance = (optional.get() + 0.3F) * 0.3F;

                float newB = explosionBudget.get() - resistance;
                if (newB > 0.0F && damageCalculator.shouldBlockExplode(this, level, pos, blockState, 1)) {
                    toExplode.add(pos);
                    explosionBudget.set(newB);
                    canPropagateExplosion = true;
                }
            }
        }
        if (!canPropagateExplosion) return;

        List<BlockPos> neighborPos = new ArrayList<>();
        for (Direction d : Direction.values()) {
            neighborPos.add(pos.relative(d));
        }
        neighborPos.sort(Comparator.comparingDouble(p -> center.distanceToSqr(p.getCenter())));
        toVisit.addAll(neighborPos);
    }


    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        if (level.isClientSide) ClientEvents.onExplosion(this);
        if (spawnParticles) {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }
        LivingEntity indirectSource = this.getIndirectSourceEntity();
        boolean hasDrop = !(indirectSource instanceof Player player && player.isCreative());

        Util.shuffle((ObjectArrayList<?>) this.getToBlow(), this.level.random);

        Multiset<SoundEvent> sounds = LinkedHashMultiset.create();
        for (BlockPos blockPos : this.getToBlow()) {
            BlockState blockState = level.getBlockState(blockPos);
            if (!blockState.isAir()) {
                //this.level.destroyBlock(blockPos, hasDrop);
                destroyBlockNoEffects(blockPos, level, indirectSource, 512, sounds);
            }
        }
        if (level.isClientSide && !sounds.isEmpty()) {
            var iter = Multisets.copyHighestCountFirst(sounds).iterator();
            for (int i = 0; i < 3 && iter.hasNext(); i++) {
                SoundEvent sound = iter.next();

                //TODO: make this depend on blocks broken
                this.level.playLocalSound(x, y, z, sound, SoundSource.BLOCKS,
                        2.5f, 0.6F + level.random.nextFloat() * 0.2f, false);
            }
        }
    }

    public boolean destroyBlockNoEffects(BlockPos pos, Level level, @Nullable Entity entity, int recursionLeft,
                                         Multiset<SoundEvent> sounds) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.isAir()) {
            return false;
        } else {
            FluidState fluidState = level.getFluidState(pos);
            if (level.isClientSide && !(blockState.getBlock() instanceof BaseFireBlock)) {
                sounds.add(SuppPlatformStuff.getSoundType(blockState, pos, level, entity).getBreakSound());
                level.addDestroyBlockEffect(pos, blockState);
                //level.levelEvent(2001, pos, Block.getId(blockState));
            }

            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(blockState, level, pos, blockEntity, entity, ItemStack.EMPTY);

            boolean bl = level.setBlock(pos, fluidState.createLegacyBlock(), BaseFireBlock.UPDATE_ALL, recursionLeft);
            if (bl) {
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(entity, blockState));
            }

            return bl;
        }
    }
}
