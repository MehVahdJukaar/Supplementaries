package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.moonlight.core.misc.DummyWorld;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.UUID;

public class CannonBlockTile extends OpeneableContainerBlockEntity {

    private static final int TIME_TO_FIRE = 40;
    private static final int FIRE_COOLDOWN = 60;

    private float pitch = 0;
    private float prevPitch = 0;
    private float yaw = 0;
    private float prevYaw = 0;

    // both from 0 to 1
    private float cooldown = 0;
    private float chargeTimer = 0;
    private byte firePower = 1;

    private float projectileDrag = 0;
    private float projectileGravity = 0f;

    public CannonBlockTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.CANNON_TILE.get(), pos, blockState, 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        tag.putFloat("cooldown", this.cooldown);
        tag.putFloat("fire_timer", this.chargeTimer);
        tag.putByte("fire_power", this.firePower);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.cooldown = tag.getFloat("cooldown");
        this.chargeTimer = tag.getFloat("fire_timer");
        this.firePower = tag.getByte("fire_power");
        if (level != null) {
            recalculateProjectileStats();
        }
    }

    @Override
    public void setChanged() {
        if (this.level != null) {
            recalculateProjectileStats();
        }
    }


    public boolean readyToFire() {
        return cooldown == 0 && chargeTimer == 0 && hasFuelAndProjectiles();
    }

    public boolean hasFuelAndProjectiles() {
        return !getProjectile().isEmpty() && !getFuel().isEmpty() &&
                getFuel().getCount() >= firePower;
    }

    public float getCooldown() {
        return cooldown;
    }

   public float getFireTimer() {
        return chargeTimer;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getProjectile() {
        return this.getItem(1).copyWithCount(1);
    }

    public ItemStack getFuel() {
        return this.getItem(0);
    }

    public float getProjectileDrag() {
        return projectileDrag;
    }

    public float getProjectileGravity() {
        return projectileGravity;
    }

    public byte getFirePower() {
        return firePower;
    }

    public float getYaw(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevYaw, this.yaw);
    }

    public float getPitch(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevPitch, this.pitch);
    }

    public void syncAttributes(float yaw, float pitch, byte firePower, boolean fire) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.firePower = firePower;
        if (fire) this.ignite();
    }


    public void setPitch(float v) {
        this.pitch = v;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.supplementaries.cannon");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new CannonContainerMenu(id, player, this);
    }

    @Override
    protected void updateBlockState(BlockState state, boolean b) {
    }

    @Override
    protected void playOpenSound(BlockState state) {
    }

    @Override
    protected void playCloseSound(BlockState state) {
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) return stack.is(Items.GUNPOWDER);
        return true;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem((direction == null) || direction.getAxis().isHorizontal() ? 1 : 0, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{side.getAxis().isHorizontal() ? 1 : 0};
    }

    public void use(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer) {
                //  startControlling(serverPlayer);
            } else CannonController.activateCannonCamera(this);
        } else if (player instanceof ServerPlayer sp) PlatHelper.openCustomMenu(sp, this, worldPosition);

    }

    public void ignite() {
        this.level.playSound(null, worldPosition, ModSounds.GUNPOWDER_IGNITE.get(), SoundSource.BLOCKS, 1.0f,
                1.8f + level.getRandom().nextFloat() * 0.2f);
        // called from server when firing
        this.chargeTimer = 1;
        //update other clients
        this.level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockTile t) {
        t.prevYaw = t.yaw;
        t.prevPitch = t.pitch;

        if (t.cooldown > 0) {
            t.cooldown -= 1f / FIRE_COOLDOWN;
            if (t.cooldown < 0) t.cooldown = 0;
        }
        if (t.chargeTimer > 0) {
            t.chargeTimer -= 1f / TIME_TO_FIRE;
            if (t.chargeTimer <= 0) {
                t.chargeTimer = 0;
                t.fire();
            }
        }
    }

    public void fire() {
        if (level.isClientSide) {
            BlockPos pos = this.worldPosition;
            level.addParticle(ModParticles.CANNON_FIRE_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    this.pitch * Mth.DEG_TO_RAD, -this.yaw * Mth.DEG_TO_RAD, 0);

            PoseStack poseStack = new PoseStack();
            RandomSource ran = level.random;
            poseStack.translate(pos.getX() + 0.5f, pos.getY() + 0.5f + 1 / 16f, pos.getZ() + 0.5f);

            poseStack.mulPose(Axis.YP.rotationDegrees(-this.yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(this.pitch));
            poseStack.translate(0, 0, -1.4);

            this.spawnDustRing(poseStack);

            this.spawnSmokeTrail(poseStack, ran);

        } else {
            this.shootProjectile();
        }
        this.cooldown = 1;
    }

    private boolean shootProjectile() {
        BlockPos pos = worldPosition;

        Vec3 facing = Vec3.directionFromRotation(this.pitch, this.yaw).scale(0.01);

        ItemStack projectile = this.getProjectile();

        Entity proj = getProjectileFromItemHack(level, projectile);

        if (proj instanceof Projectile arrow) {
            arrow.cachedOwner = null;
            arrow.ownerUUID = null;
            CompoundTag c = new CompoundTag();
            arrow.save(c);
            var opt = EntityType.create(c, level); // create new to reset level properly

            if (opt.isPresent()) {
                arrow = (Projectile) opt.get();
                arrow.setPos(pos.getX() + 0.5 - facing.x,
                        pos.getY() + 0.5 - facing.y, pos.getZ() + 0.5 - facing.z);

                float inaccuracy = 0;
                float power = -projectileDrag * getFirePower();
                arrow.shoot(facing.x, facing.y, facing.z, power, inaccuracy);

                level.addFreshEntity(arrow);
                return true;
            }
        }
        return false;
    }


    private void spawnSmokeTrail(PoseStack poseStack, RandomSource ran) {
        int smokeCount = 20;
        for (int i = 0; i < smokeCount; i += 1) {

            poseStack.pushPose();

            Vector4f speed = poseStack.last().pose().transform(new Vector4f(0, 0, -MthUtils.nextWeighted(ran, 0.5f, 1, 0.06f), 0));

            float aperture = 0.5f;
            poseStack.translate(-aperture / 2 + ran.nextFloat() * aperture, -aperture / 2 + ran.nextFloat() * aperture, 0);

            Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1, 1));

            level.addParticle(ParticleTypes.SMOKE,
                    p.x, p.y, p.z,
                    speed.x, speed.y, speed.z);
            poseStack.popPose();
        }
    }

    private void spawnDustRing(PoseStack poseStack) {
        poseStack.pushPose();

        Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1, 1));

        int dustCount = 16;
        for (int i = 0; i < dustCount; i += 1) {

            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(90));

            poseStack.mulPose(Axis.XP.rotationDegrees(380f * i / dustCount));
            Vector4f speed = poseStack.last().pose().transform(new Vector4f(0, 0, 0.05f, 0));
            level.addParticle(ModParticles.BOMB_SMOKE_PARTICLE.get(),
                    p.x, p.y, p.z,
                    speed.x, speed.y, speed.z);
            poseStack.popPose();
        }

        poseStack.popPose();
    }


    private static Entity getProjectileFromItemHack(Level level, ItemStack projectile) {
        Player fakePlayer = FakePlayerManager.get(FAKE_PLAYER, level);
        if (projectile.getItem() instanceof ArrowItem ai) {
            return ai.createArrow(level, projectile, fakePlayer);
        }
        ProjectileTestLevel testLevel = ProjectileTestLevel.getCachedInstance("cannon_test_level", ProjectileTestLevel::new);
        testLevel.setup();
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, projectile.copy());
        projectile.use(testLevel, fakePlayer, InteractionHand.MAIN_HAND);
        var p = testLevel.projectile;
        if (p != null) return p;
        if (projectile.is(Items.FIRE_CHARGE)) return EntityType.SMALL_FIREBALL.create(level);

        return new SlingshotProjectileEntity(level, projectile, ItemStack.EMPTY);

    }

    private static final GameProfile FAKE_PLAYER = new GameProfile(UUID.fromString("11242C44-14d5-1f22-3d27-13D2C45CA355"), "[CANNON_TESTER]");

    private void recalculateProjectileStats() {
        ItemStack projectile = getProjectile();
        if (projectile.isEmpty()) return;

        Entity proj = getProjectileFromItemHack(level, projectile);

        proj.setDeltaMovement(1, 0, 0);
        proj.tick();
        var newMovement = proj.getDeltaMovement();
        this.projectileDrag = (float) newMovement.x;
        this.projectileGravity = (float) -newMovement.y;
    }


    private static class ProjectileTestLevel extends DummyWorld {

        private Entity projectile = null;

        public ProjectileTestLevel() {
            super(false, false);
        }

        public void setup() {
            projectile = null;
        }

        @Override
        public boolean addFreshEntity(Entity entity) {
            this.projectile = entity;
            return true;
        }
    }

}
