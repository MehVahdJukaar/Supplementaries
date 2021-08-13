package net.mehvahdjukaar.supplementaries.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.supplementaries.network.BombExplosionKnockbackPacket;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EntityExplosionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/*
public class BombExplosion extends Explosion {

    private final Entity source;
    private final float radius;

    private final World level;
    private final double x;
    private final double y;
    private final double z;
    private final boolean blue;

    private final ExplosionContext damageCalculator;
    private final List<BlockPos> toBlow = Lists.newArrayList();
    private final Map<PlayerEntity, Vector3d> hitPlayers = Maps.newHashMap();
    private final Mode mode;


    public BombExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionContext context, double x, double y, double z, float radius, boolean blue, Mode interaction) {
        super(world, entity, damageSource, context, x, y, z, radius, blue, interaction);
        this.level = world;
        this.source = entity;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blue = blue;
        this.mode = interaction;
        this.damageCalculator = context == null ? this.makeDamageCalculator(entity) : context;
    }

    private static final ExplosionContext EXPLOSION_DAMAGE_CALCULATOR = new ExplosionContext();
    private ExplosionContext makeDamageCalculator(@Nullable Entity p_234894_1_) {
        return p_234894_1_ == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityExplosionContext(p_234894_1_);
    }


    public void doFinalizeExplosion() {

        this.level.playSound(null, this.x, this.y, this.z, Registry.BOMB_SOUND.get(), SoundCategory.NEUTRAL, blue ? 5F : 3f, (1.2F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F));

        ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
        Collections.shuffle(this.toBlow, this.level.random);


        for(BlockPos blockpos : this.toBlow) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (!blockstate.isAir(this.level, blockpos)) {
                BlockPos blockpos1 = blockpos.immutable();
                this.level.getProfiler().push("explosion_blocks");
                if (blockstate.canDropFromExplosion(this.level, blockpos, this) && this.level instanceof ServerWorld) {
                    TileEntity tileentity = blockstate.hasTileEntity() ? this.level.getBlockEntity(blockpos) : null;
                    LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.level)).withRandom(this.level.random).withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(blockpos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withOptionalParameter(LootParameters.BLOCK_ENTITY, tileentity).withOptionalParameter(LootParameters.THIS_ENTITY, this.source);

                    if (this.mode == Mode.DESTROY) {
                        lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.radius);
                    }

                    blockstate.getDrops(lootcontext$builder).forEach((p_229977_2_) -> {
                        addBlockDrops(objectarraylist, p_229977_2_, blockpos1);
                    });
                }

                blockstate.onBlockExploded(this.level, blockpos, this);
                this.level.getProfiler().pop();
            }
        }

        for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
        }

    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> p_229976_0_, ItemStack p_229976_1_, BlockPos p_229976_2_) {
        int i = p_229976_0_.size();

        for(int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = p_229976_0_.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, p_229976_1_)) {
                ItemStack itemstack1 = ItemEntity.merge(itemstack, p_229976_1_, 16);
                p_229976_0_.set(j, Pair.of(itemstack1, pair.getSecond()));
                if (p_229976_1_.isEmpty()) {
                    return;
                }
            }
        }

        p_229976_0_.add(Pair.of(p_229976_1_, p_229976_2_));
    }

    @Override
    public void explode() {

        Set<BlockPos> set = Sets.newHashSet();
        int i = 16;

        if(mode!=Mode.NONE) {
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
        int k1 = MathHelper.floor(this.x - (double)f2 - 1.0D);
        int l1 = MathHelper.floor(this.x + (double)f2 + 1.0D);
        int i2 = MathHelper.floor(this.y - (double)f2 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double)f2 + 1.0D);
        int j2 = MathHelper.floor(this.z - (double)f2 - 1.0D);
        int j1 = MathHelper.floor(this.z + (double)f2 + 1.0D);
        List<Entity> list = this.level.getEntities(this.source, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, list, f2);
        Vector3d vector3d = new Vector3d(this.x, this.y, this.z);

        for (Entity entity : list) {
            if (!entity.ignoreExplosion()) {
                double d12 = MathHelper.sqrt(entity.distanceToSqr(vector3d)) / f2;
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.x;
                    double d7 = (entity instanceof TNTEntity ? entity.getY() : entity.getEyeY()) - this.y;
                    double d9 = entity.getZ() - this.z;
                    double d13 = MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = getSeenPercent(vector3d, entity);
                        double d10 = (1.0D - d12) * d14;
                        entity.hurt(this.getDamageSource(), (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                        double d11 = d10;
                        boolean isPlayer = entity instanceof PlayerEntity;
                        PlayerEntity playerentity = null;

                        if (isPlayer) {
                            playerentity = (PlayerEntity) entity;
                            if (!playerentity.isSpectator() && (!playerentity.isCreative() || !playerentity.abilities.flying)) {
                                this.hitPlayers.put(playerentity, new Vector3d(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }

                        if (entity instanceof LivingEntity) {
                            if(blue) {
                                if (!isPlayer || (!playerentity.isSpectator() && !playerentity.isCreative())){
                                    ((LivingEntity) entity).addEffect(new EffectInstance(Effects.WEAKNESS, 20 * 15));
                                    entity.setSecondsOnFire(6);
                                }
                            }
                            d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, d10);
                        }

                        entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11, d7 * d11, d9 * d11));

                    }
                }
            }
        }

        //send knockback packet to players

        if(!level.isClientSide) {
            for (PlayerEntity player : this.hitPlayers.keySet()) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                        new BombExplosionKnockbackPacket(this.hitPlayers.get(player)));
            }
        }

    }
}
*/
