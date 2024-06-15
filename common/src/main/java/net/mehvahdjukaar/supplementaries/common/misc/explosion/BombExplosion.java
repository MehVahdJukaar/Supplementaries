package net.mehvahdjukaar.supplementaries.common.misc.explosion;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BombExplosion extends Explosion {

    private final BombEntity.BombType bombType;
    private final ExplosionDamageCalculator damageCalculator;


    public BombExplosion(Level world, @Nullable Entity entity,
                         @Nullable ExplosionDamageCalculator context, double x, double y, double z,
                         float radius, BombEntity.BombType bombType, BlockInteraction interaction) {
        super(world, entity, null, context, x, y, z, radius, false, interaction);
        this.bombType = bombType;
        this.damageCalculator = context == null ? this.bombMakeDamageCalculator(entity) : context;
        this.damageSource = ModDamageSources.bombExplosion(getDirectSourceEntity(), getIndirectSourceEntity());
    }


    //client factory
    public BombExplosion(Level level, @Nullable Entity source, double toBlowX, double toBlowY, double toBlowZ,
                         float radius, List<BlockPos> toBlow, BombEntity.BombType bombType) {
        super(level, source, toBlowX, toBlowY, toBlowZ, radius, toBlow);
        this.bombType = bombType;
        this.damageCalculator = this.bombMakeDamageCalculator(source);
    }

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();

    private ExplosionDamageCalculator bombMakeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    @Override
    public ObjectArrayList<BlockPos> getToBlow() {
        return (ObjectArrayList<BlockPos>) super.getToBlow();
    }


    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        boolean interactsWithBlocks = this.interactsWithBlocks();

        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, ModSounds.BOMB_EXPLOSION.get(), SoundSource.BLOCKS, bombType.volume(), (1.2F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F), false);

            if (spawnParticles) {
                if (!(this.radius < 2.0F) && interactsWithBlocks) {
                    this.level.addParticle(ModParticles.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), this.x, this.y, this.z, 1.0, 0.0, 0.0);
                } else {
                    this.level.addParticle(ModParticles.BOMB_EXPLOSION_PARTICLE.get(), this.x, this.y, this.z, 1.0, 0.0, 0.0);
                }
            }
            bombType.spawnExtraParticles(x, y, z, level);
        }


        ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
        Util.shuffle(this.getToBlow(), this.level.random);

        if (!interactsWithBlocks) return;
        for (BlockPos blockpos : this.getToBlow()) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (!blockstate.isAir()) {
                BlockPos immutable = blockpos.immutable();
                this.level.getProfiler().push("explosion_blocks");
                if (ForgeHelper.canDropFromExplosion(blockstate, this.level, blockpos, this) && this.level instanceof ServerLevel serverLevel) {
                    BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                    LootParams.Builder builder = (new LootParams.Builder(serverLevel))
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos))
                            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                            .withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);

                    if (this.blockInteraction == BlockInteraction.DESTROY) {
                        builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                    }

                    blockstate.getDrops(builder).forEach(stack -> addBlockDrops(drops, stack, immutable));
                }

                ForgeHelper.onBlockExploded(blockstate, this.level, blockpos, this);
                this.level.getProfiler().pop();
            }
        }

        for (Pair<ItemStack, BlockPos> pair : drops) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
        }

    }

    @Override
    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, BlockPos.containing(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();

        Player owner = this.source instanceof Projectile pr && pr.getOwner() instanceof Player pl ? pl : null;

        if (blockInteraction != BlockInteraction.KEEP) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int l = 0; l < 16; ++l) {
                        if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                            double d0 = j / 15.0F * 2.0F - 1.0F;
                            double d1 = k / 15.0F * 2.0F - 1.0F;
                            double d2 = l / 15.0F * 2.0F - 1.0F;
                            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 = d0 / d3;
                            d1 = d1 / d3;
                            d2 = d2 / d3;
                            float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                            double d4 = this.x;
                            double d6 = this.y;
                            double d8 = this.z;

                            for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                                BlockState blockstate = this.level.getBlockState(blockpos);
                                FluidState fluidstate = this.level.getFluidState(blockpos);
                                Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
                                if (optional.isPresent()) {
                                    f -= (optional.get() + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, f)) {
                                    if (owner == null || !CompatHandler.FLAN || FlanCompat.canBreak(owner, blockpos)) {
                                        set.add(blockpos);
                                    }
                                }

                                d4 += d0 * 0.3F;
                                d6 += d1 * 0.3F;
                                d8 += d2 * 0.3F;
                            }
                        }
                    }
                }
            }
        }

        this.getToBlow().addAll(set);
        float diameter = this.radius * 2.0F;
        int k1 = Mth.floor(this.x - diameter - 1.0D);
        int l1 = Mth.floor(this.x + diameter + 1.0D);
        int i2 = Mth.floor(this.y - diameter - 1.0D);
        int i1 = Mth.floor(this.y + diameter + 1.0D);
        int j2 = Mth.floor(this.z - diameter - 1.0D);
        int j1 = Mth.floor(this.z + diameter + 1.0D);

        List<Entity> list = this.level.getEntities(this.getDirectSourceEntity(), new AABB(k1, i2, j2, l1, i1, j1));
        ForgeHelper.onExplosionDetonate(this.level, this, list, diameter);
        Vec3 vector3d = new Vec3(this.x, this.y, this.z);

        for (Entity entity : list) {
            if (!entity.ignoreExplosion()) {

                if (owner != null && CompatHandler.FLAN && !FlanCompat.canAttack(owner, entity)) {
                    continue;
                }

                double distSq = entity.distanceToSqr(vector3d);
                double normalizedDist = Mth.sqrt((float) distSq) / diameter;
                if (normalizedDist <= 1.0D) {
                    double dx = entity.getX() - this.x;
                    double dy = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    double dz = entity.getZ() - this.z;
                    double distFromCenterSqr = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
                    if (distFromCenterSqr != 0.0D) {
                        dx = dx / distFromCenterSqr;
                        dy = dy / distFromCenterSqr;
                        dz = dz / distFromCenterSqr;
                        double d14 = getSeenPercent(vector3d, entity);
                        double d10 = (1.0D - normalizedDist) * d14;
                        entity.hurt(this.getDamageSource(), ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * diameter + 1.0D)));
                        double d11 = d10;
                        boolean isPlayer = entity instanceof Player;
                        Player playerEntity = null;

                        if (isPlayer) {
                            playerEntity = (Player) entity;
                            if (!playerEntity.isSpectator() && (!playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                                this.getHitPlayers().put(playerEntity, new Vec3(dx * d10, dy * d10, dz * d10));
                            }
                        }

                        if (entity instanceof LivingEntity livingEntity) {

                            if (!isPlayer || (!playerEntity.isSpectator() && !playerEntity.isCreative())) {
                                bombType.applyStatusEffects(livingEntity, distSq);
                            }
                            if (entity instanceof Creeper creeper) {
                                creeper.ignite();
                            }

                            d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, d10);
                        }

                        entity.setDeltaMovement(entity.getDeltaMovement().add(dx * d11, dy * d11, dz * d11));

                    }
                }
            }
        }

        this.bombType.afterExploded(this, level);

    }
}

