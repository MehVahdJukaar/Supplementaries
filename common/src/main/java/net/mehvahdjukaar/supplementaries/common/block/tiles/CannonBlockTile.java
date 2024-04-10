package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.core.misc.DummyWorld;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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

import java.util.UUID;

public class CannonBlockTile extends OpeneableContainerBlockEntity {

    private static final int TIME_TO_FIRE = 40;
    private static final int FIRE_COOLDOWN = 60;

    private float pitch = 0;
    private float prevPitch = 0;
    private float yaw = 0;
    private float prevYaw = 0;

    // both from 0 to 1
    private float disabledCooldown = 0;
    private float timeUntilFire = 0;
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
        tag.putFloat("cooldown", this.disabledCooldown);
        tag.putFloat("fire_timer", this.timeUntilFire);
        tag.putByte("fire_power", this.firePower);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.disabledCooldown = tag.getFloat("cooldown");
        this.timeUntilFire = tag.getFloat("fire_timer");
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
        return disabledCooldown == 0 && timeUntilFire == 0 && hasFuelAndProjectiles();
    }

    public boolean hasFuelAndProjectiles() {
        return !getProjectile().isEmpty() && !getFuel().isEmpty() &&
                getFuel().getCount() >= firePower;
    }

    public boolean isFiring() {
        return timeUntilFire > 0;
    }

    public float getFiringAnimation(float partialTicks) {
        if(timeUntilFire <= 0) return 0;
        return timeUntilFire - (1f / TIME_TO_FIRE * partialTicks);
    }

    public float getCooldownAnimation(float partialTicks) {
        if(disabledCooldown <= 0) return 0;
        return disabledCooldown - (1f / FIRE_COOLDOWN * partialTicks);
    }

    public float getFireTimer() {
        return timeUntilFire;
    }

    public float getDisabledCooldown() {
        return disabledCooldown;
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


    public void setPitch(float pitch) {
        this.pitch = Mth.wrapDegrees(pitch);
    }

    public void setYaw(float yaw) {
        this.yaw = Mth.wrapDegrees(yaw);
    }

    public void changeFirePower(int scrollDelta) {
        this.firePower = (byte)(1+ Math.floorMod(this.firePower-1 + scrollDelta, 4));
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
        level.playSound(null, worldPosition, ModSounds.GUNPOWDER_IGNITE.get(), SoundSource.BLOCKS, 1.0f,
                1.8f + level.getRandom().nextFloat() * 0.2f);
        // called from server when firing
        this.timeUntilFire = 1;
        //update other clients
        level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 3);
        level.blockEvent(worldPosition, this.getBlockState().getBlock(), 0, 0);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockTile t) {
        t.prevYaw = t.yaw;
        t.prevPitch = t.pitch;

        if (t.disabledCooldown > 0) {
            t.disabledCooldown -= 1f / FIRE_COOLDOWN;
            if (t.disabledCooldown < 0) t.disabledCooldown = 0;
        }
        if (t.timeUntilFire > 0) {
            t.timeUntilFire -= 1f / TIME_TO_FIRE;
            if (t.timeUntilFire <= 0) {
                t.timeUntilFire = 0;
                t.fire();
            }
        }
    }

    private void fire() {
        if (level.isClientSide) {
            //call directly on client
            level.blockEvent(worldPosition, this.getBlockState().getBlock(), 1, 0);
        } else {
            this.shootProjectile();
        }
        this.disabledCooldown = 1;
    }

    private boolean shootProjectile() {
        BlockPos pos = worldPosition;

        Vec3 facing = Vec3.directionFromRotation(this.pitch, this.yaw).scale(0.01);

        ItemStack projectile = this.getProjectile();

        Entity proj = getProjectileFromItemHack(projectile);

        if (proj instanceof Projectile arrow) {
            arrow.cachedOwner = null;
            arrow.ownerUUID = null;

            if (projectile.is(Items.ENDER_PEARL) && level instanceof ServerLevel se) {
                BlockSource source = new BlockSourceImpl(se, worldPosition);
                arrow = PearlMarker.getPearlToDispenseAndPlaceMarker(source);
            } else {
                CompoundTag c = new CompoundTag();
                arrow.save(c);
                var opt = EntityType.create(c, level); // create new to reset level properly

                if (opt.isPresent()) {
                    arrow = (Projectile) opt.get();
                }
            }

            arrow.setPos(pos.getX() + 0.5 - facing.x,
                    pos.getY() + 0.5 - facing.y, pos.getZ() + 0.5 - facing.z);

            float inaccuracy = 0;
            float power = -projectileDrag * getFirePower();
            arrow.shoot(facing.x, facing.y, facing.z, power, inaccuracy);

            level.addFreshEntity(arrow);
            return true;
        }
        return false;
    }


    private Entity getProjectileFromItemHack(ItemStack projectile) {
        if (projectile.is(Items.FIRE_CHARGE)) return EntityType.SMALL_FIREBALL.create(level);


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

        return new SlingshotProjectileEntity(level, projectile, ItemStack.EMPTY);

    }

    private static final GameProfile FAKE_PLAYER = new GameProfile(UUID.fromString("11242C44-14d5-1f22-3d27-13D2C45CA355"), "[CANNON_TESTER]");

    private void recalculateProjectileStats() {
        ItemStack projectile = getProjectile();
        if (projectile.isEmpty()) return;

        Entity proj = getProjectileFromItemHack(projectile);

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
