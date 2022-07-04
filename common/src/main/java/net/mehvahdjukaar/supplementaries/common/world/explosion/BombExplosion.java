package net.mehvahdjukaar.supplementaries.common.world.explosion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendKnockbackPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BombExplosion extends Explosion {

    private final float radius;

    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    private final BombEntity.BombType bombType;

    private final ExplosionDamageCalculator damageCalculator;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();
    private final BlockInteraction mode;


    public BombExplosion(Level world, @Nullable Entity entity, @Nullable DamageSource damageSource,
                         @Nullable ExplosionDamageCalculator context, double x, double y, double z,
                         float radius, BombEntity.BombType bombType, BlockInteraction interaction) {
        super(world, entity, damageSource, context, x, y, z, radius, false, interaction);
        this.level = world;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bombType = bombType;
        this.mode = interaction;
        this.damageCalculator = context == null ? this.makeDamageCalculator(entity) : context;
    }

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity p_234894_1_) {
        return p_234894_1_ == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(p_234894_1_);
    }


    public void doFinalizeExplosion() {

        this.level.playSound(null, this.x, this.y, this.z, ModSounds.BOMB_EXPLOSION.get(), SoundSource.NEUTRAL, bombType.volume(), (1.2F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F));

        ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
        Util.shuffle(this.toBlow, this.level.random);


        for (BlockPos blockpos : this.toBlow) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (!blockstate.isAir()) {
                BlockPos immutable = blockpos.immutable();
                this.level.getProfiler().push("explosion_blocks");
                if (blockstate.canDropFromExplosion(this.level, blockpos, this) && this.level instanceof ServerLevel) {
                    BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                    LootContext.Builder builder = (new LootContext.Builder((ServerLevel) this.level)).withRandom(this.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.getExploder());

                    if (this.mode == BlockInteraction.DESTROY) {
                        builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                    }

                    blockstate.getDrops(builder).forEach((p_229977_2_) -> {
                        addBlockDrops(drops, p_229977_2_, immutable);
                    });
                }

                blockstate.onBlockExploded(this.level, blockpos, this);
                this.level.getProfiler().pop();
            }
        }

        for (Pair<ItemStack, BlockPos> pair : drops) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
        }

    }

    private void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos) {
        int i = drops.size();
        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = drops.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, stack)) {
                ItemStack itemStack = ItemEntity.merge(itemstack, stack, 16);
                drops.set(j, Pair.of(itemStack, pair.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        drops.add(Pair.of(stack, pos));
    }

    @Override
    public void explode() {
        this.level.gameEvent(this.getExploder(), GameEvent.EXPLODE, new BlockPos(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();
        int i = 16;

        if (mode != BlockInteraction.NONE) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int l = 0; l < 16; ++l) {
                        if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                            double d0 = (float) j / 15.0F * 2.0F - 1.0F;
                            double d1 = (float) k / 15.0F * 2.0F - 1.0F;
                            double d2 = (float) l / 15.0F * 2.0F - 1.0F;
                            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 = d0 / d3;
                            d1 = d1 / d3;
                            d2 = d2 / d3;
                            float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                            double d4 = this.x;
                            double d6 = this.y;
                            double d8 = this.z;

                            for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                BlockPos blockpos = new BlockPos(d4, d6, d8);
                                BlockState blockstate = this.level.getBlockState(blockpos);
                                FluidState fluidstate = this.level.getFluidState(blockpos);
                                Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
                                if (optional.isPresent()) {
                                    f -= (optional.get() + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, f)) {
                                    set.add(blockpos);
                                }

                                d4 += d0 * (double) 0.3F;
                                d6 += d1 * (double) 0.3F;
                                d8 += d2 * (double) 0.3F;
                            }
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);
        float f2 = this.radius * 2.0F;
        int k1 = Mth.floor(this.x - (double) f2 - 1.0D);
        int l1 = Mth.floor(this.x + (double) f2 + 1.0D);
        int i2 = Mth.floor(this.y - (double) f2 - 1.0D);
        int i1 = Mth.floor(this.y + (double) f2 + 1.0D);
        int j2 = Mth.floor(this.z - (double) f2 - 1.0D);
        int j1 = Mth.floor(this.z + (double) f2 + 1.0D);
        List<Entity> list = this.level.getEntities(this.getExploder(), new AABB(k1, i2, j2, l1, i1, j1));
        ForgeEventFactory.onExplosionDetonate(this.level, this, list, f2);
        Vec3 vector3d = new Vec3(this.x, this.y, this.z);

        for (Entity entity : list) {
            if (!entity.ignoreExplosion()) {
                double distSq = entity.distanceToSqr(vector3d);
                double d12 = Mth.sqrt((float) distSq) / f2;
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.x;
                    double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    double d9 = entity.getZ() - this.z;
                    double d13 = Mth.sqrt((float) (d5 * d5 + d7 * d7 + d9 * d9));
                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = getSeenPercent(vector3d, entity);
                        double d10 = (1.0D - d12) * d14;
                        entity.hurt(this.getDamageSource(), (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                        double d11 = d10;
                        boolean isPlayer = entity instanceof Player;
                        Player playerentity = null;

                        if (isPlayer) {
                            playerentity = (Player) entity;
                            if (!playerentity.isSpectator() && (!playerentity.isCreative() || !playerentity.getAbilities().flying)) {
                                this.hitPlayers.put(playerentity, new Vec3(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }

                        if (entity instanceof LivingEntity livingEntity) {

                            if (!isPlayer || (!playerentity.isSpectator() && !playerentity.isCreative())) {
                                bombType.applyStatusEffects(livingEntity,distSq);
                            }

                            d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, d10);
                        }

                        entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11, d7 * d11, d9 * d11));

                    }
                }
            }
        }

        //send knockback packet to players

        if (!level.isClientSide) {
            for (Player player : this.hitPlayers.keySet()) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new ClientBoundSendKnockbackPacket(this.hitPlayers.get(player), player.getId()));
            }
        }

        this.bombType.afterExploded(this, level);

    }
}

