package net.mehvahdjukaar.supplementaries.common.entities;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.ProjectileStats;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ThrowableBrickEntity extends ImprovedProjectileEntity {
    public ThrowableBrickEntity(EntityType<? extends ThrowableBrickEntity> type, Level world) {
        super(type, world);
    }

    public ThrowableBrickEntity(LivingEntity throwerIn) {
        super(ModEntities.THROWABLE_BRICK.get(), throwerIn, throwerIn.level());
    }

    public ThrowableBrickEntity(Level worldIn, double x, double y, double z) {
        super(ModEntities.THROWABLE_BRICK.get(), x, y, z, worldIn);
    }

    @Override
    protected Component getTypeName() {
        return this.getItem().getDisplayName();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BRICK;
    }


    private ParticleOptions makeParticle() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(this.getDefaultItem())) : new ItemParticleOption(ParticleTypes.ITEM, itemstack);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particle = this.makeParticle();

            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult rayTraceResult) {
        super.onHitBlock(rayTraceResult);
        Level level = level();
        if (!level.isClientSide) {
            Entity entity = this.getOwner();
            BlockPos pos = rayTraceResult.getBlockPos();

            if (entity instanceof Player player) {
                if (CompatHandler.FLAN && !FlanCompat.canBreak(player, pos)) return;
                if (!Utils.mayBuild(player, pos)) {
                    if (!this.getItem().hasAdventureModeBreakTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK),
                            new BlockInWorld(level, pos, false))) {
                        return;
                    }
                }
            }
            if (!(entity instanceof Mob) || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || PlatHelper.isMobGriefingOn(level, this)) {
                var p = FakePlayerManager.get(BRICK_PLAYER, level);
                p.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_PICKAXE.getDefaultInstance());
                if (level.getBlockState(pos).is(ModTags.BRICK_BREAKABLE_POTS)) {
                    //TODO: why is fake player here? cant just use normalplayer like with cannonballs?
                    level.destroyBlock(pos, true, p);
                } else {
                    breakGlass(pos, 6, p);
                }
            }
        }
    }

    private static final GameProfile BRICK_PLAYER = new GameProfile(UUID.randomUUID(), "Throwable Brick Fake Player");

    private void breakGlass(BlockPos pos, int chance, Player p) {
        int c = chance - 1 - this.random.nextInt(4);
        BlockState state = level().getBlockState(pos);
        if (state.getBlock().getExplosionResistance() > 3) return;
        if (c < 0 || !state.is(ModTags.BRICK_BREAKABLE_GLASS)) return;

        level().destroyBlock(pos, true, p);
        breakGlass(pos.above(), c, p);
        breakGlass(pos.below(), c, p);
        breakGlass(pos.east(), c, p);
        breakGlass(pos.west(), c, p);
        breakGlass(pos.north(), c, p);
        breakGlass(pos.south(), c, p);

    }


    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        int i = 1;
        entity.hurt(this.level().damageSources().thrown(this, this.getOwner()), i);
    }


    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            Vec3 v = result.getLocation();
            this.level().playSound(null, v.x, v.y, v.z, SoundEvents.NETHER_BRICKS_BREAK, SoundSource.NEUTRAL, 0.75F, 1);
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.remove(RemovalReason.DISCARDED);
        }

    }

    @Override
    protected void updateRotation() {
    }

    @Override
    public float getDefaultShootVelocity() {
        return ProjectileStats.BRICKS_SPEED;
    }

    @Override
    protected float getGravity() {
        return ProjectileStats.BRICKS_GRAVITY;
    }
}
