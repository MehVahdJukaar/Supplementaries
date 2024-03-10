package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.core.misc.DummyWorld;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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

import java.util.UUID;

public class CannonBlockTile extends OpeneableContainerBlockEntity {

    private float pitch = 0;
    private float prevPitch = 0;
    private float yaw = 0;
    private float prevYaw = 0;
    private float cooldown = 0;
    private float firePower = 1;

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
        tag.putFloat("explosion_timer", this.cooldown);
        tag.putFloat("fire_power", this.firePower);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.cooldown = tag.getFloat("explosion_timer");
        this.firePower = tag.getFloat("fire_power");
        if (level != null) {
            recalculateProjectileStats();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getProjectile() {
        if (true) return Items.ARROW.getDefaultInstance();
        return this.getItem(1);
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

    public float getFirePower() {
        return firePower;
    }

    public float getYaw(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevYaw, this.yaw);
    }

    public float getPitch(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevPitch, this.pitch);
    }

    public void addRotation(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
        this.pitch = Mth.clamp(this.pitch, -80, 0);
    }

    public void setPitch(float angle) {
        this.pitch = angle;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.supplementaries.cannon");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
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
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }


    public void use(Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer) {
                //  startControlling(serverPlayer);
            } else CannonController.activateCannonCamera(worldPosition);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockTile t) {
        t.prevYaw = t.yaw;
        t.prevPitch = t.pitch;


        if ((level.getGameTime() % 60) == 0) {
            if (level.isClientSide) {
                level.addParticle(ModParticles.CANNON_FIRE_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        -t.pitch * Mth.DEG_TO_RAD, -t.yaw * Mth.DEG_TO_RAD, 0);
            } else {
                Vec3 facing = Vec3.directionFromRotation(-t.pitch, t.yaw).scale(0.01);

                ItemStack projectile = t.getProjectile();

                Entity proj = getProjectileFromItemHack(level, projectile);

                if (proj != null) {
                    proj.setPos(pos.getX() + 0.5 - facing.x,
                            pos.getY() + 0.5 - facing.y , pos.getZ() + 0.5 - facing.z);
                    if (proj instanceof Projectile arrow) {
                        arrow.shoot(facing.x, facing.y, facing.z, -0.98f, 0);
                        CompoundTag tag = arrow.saveWithoutId(new CompoundTag());
                        tag.remove("Owner");
                        arrow.load(tag);
                        level.addFreshEntity(arrow);
                    }
                }
            }
        }
    }


    private static Entity getProjectileFromItemHack(Level level, ItemStack projectile) {
        Player fakePlayer = FakePlayerManager.get(FAKE_PLAYER, level);
        if (projectile.getItem() instanceof ArrowItem ai) {
            return ai.createArrow(level, projectile, fakePlayer);
        } else {
            ProjectileTestLevel testLevel = ProjectileTestLevel.getCachedInstance("cannon_test_level", ProjectileTestLevel::new);
            testLevel.setup();
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, projectile.copy());
            projectile.use(testLevel, fakePlayer, InteractionHand.MAIN_HAND);
            return testLevel.projectile;
        }
    }

    private static final GameProfile FAKE_PLAYER = new GameProfile(UUID.fromString("11242C44-14d5-1f22-3d27-13D2C45CA355"), "[CANNON_TESTER]");

    private void recalculateProjectileStats() {
        ItemStack projectile = getProjectile();
        if (projectile.isEmpty()) return;

        Entity proj = getProjectileFromItemHack(level, projectile);
        if (proj != null) {

            proj.setDeltaMovement(1, 0, 0);
            proj.tick();
            var newMovement = proj.getDeltaMovement();
            this.projectileDrag = (float) newMovement.x;
            this.projectileGravity = (float) -newMovement.y;

            if (projectileDrag <= 0 || projectileGravity <= 0) {
                this.projectileDrag = 0.99f;
                this.projectileGravity = 0.03f;
                Supplementaries.error();
            }
        } else {
            //default gravity for non projectile items
            this.projectileGravity = 0.03f;
            this.projectileDrag = 0.99f;
        }

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
