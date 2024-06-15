package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
        if (center.distanceToSqr(pos.getCenter()) > (r * r))
            return;
        if (!level.isInWorldBounds(pos) || visited.contains(pos))
            return;

        visited.add(pos);
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);

        boolean canPropagateExplosion = false;
        if (blockState.isSolid()) {
            Optional<Float> optional = damageCalculator.getBlockExplosionResistance(null, level, pos, blockState, fluidState);
            if (optional.isPresent()) {
                float resistance = (optional.get() + 0.3F) * 0.3F;

                float newB = explosionBudget.get() - resistance;
                if (newB > 0.0F && damageCalculator.shouldBlockExplode(null, level, pos, blockState, 1)) {
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

        if (spawnParticles) {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
        }

        ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
        LivingEntity indirectSource = this.getIndirectSourceEntity();
        boolean isPlayer = indirectSource instanceof Player;
        Util.shuffle((ObjectArrayList<?>) this.getToBlow(), this.level.random);

        for (BlockPos blockPos : this.getToBlow()) {
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (!blockState.isAir()) {
                BlockPos blockPos2 = blockPos.immutable();
                level.getProfiler().push("explosion_blocks");
                if (block.dropFromExplosion(this)) {
                    if (level instanceof ServerLevel serverLevel) {
                        BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
                        LootParams.Builder builder = (new LootParams.Builder(serverLevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);

                        blockState.spawnAfterBreak(serverLevel, blockPos, ItemStack.EMPTY, isPlayer);
                        blockState.getDrops(builder).forEach((itemStack) -> {
                            addBlockDrops(drops, itemStack, blockPos2);
                        });
                    }
                }

                this.level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                block.wasExploded(this.level, blockPos, this);
                this.level.getProfiler().pop();
            }
        }

        if (indirectSource instanceof Player player && player.isCreative()) return;
        for (var pair : drops)
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
    }
}
