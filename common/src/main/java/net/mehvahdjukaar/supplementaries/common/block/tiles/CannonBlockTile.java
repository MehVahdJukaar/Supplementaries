package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IBallistic;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
    private byte powerLevel = 1;

    @Nullable
    private IBallistic.Data trajectoryData;

    @Nullable
    private UUID playerWhoIgnitedUUID = null;

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
        tag.putByte("fire_power", this.powerLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.disabledCooldown = tag.getFloat("cooldown");
        this.timeUntilFire = tag.getFloat("fire_timer");
        this.powerLevel = tag.getByte("fire_power");
        this.trajectoryData = null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.trajectoryData = null;
    }

    private void computeTrajectoryData() {
        ItemStack proj = this.getProjectile();
        var behavior = CannonBlock.getCannonBehavior(getProjectile().getItem());
        if (behavior instanceof IBallistic b) {
            this.trajectoryData = b.calculateData(proj, level);
        } else {
            this.trajectoryData = IBallistic.LINE;
        }
    }

    public boolean readyToFire() {
        return disabledCooldown == 0 && timeUntilFire == 0 && hasFuelAndProjectiles();
    }

    public boolean hasFuelAndProjectiles() {
        return !getProjectile().isEmpty() && !getFuel().isEmpty() &&
                getFuel().getCount() >= powerLevel;
    }

    public boolean isFiring() {
        return timeUntilFire > 0;
    }

    public float getFiringAnimation(float partialTicks) {
        if (timeUntilFire <= 0) return 0;
        return timeUntilFire - (1f / TIME_TO_FIRE * partialTicks);
    }

    public float getCooldownAnimation(float partialTicks) {
        if (disabledCooldown <= 0) return 0;
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
        return this.getItem(1);
    }

    public void setProjectile(ItemStack stack) {
        this.setItem(1, stack);
    }

    public ItemStack getFuel() {
        return this.getItem(0);
    }

    public void setFuel(ItemStack stack) {
        this.setItem(0, stack);
    }

    public IBallistic.Data getTrajectoryData() {
        if (trajectoryData == null) computeTrajectoryData();
        return trajectoryData;
    }

    public byte getPowerLevel() {
        return powerLevel;
    }

    public float getFirePower(){
        return (float) (powerLevel * CommonConfigs.Functional.CANNON_FIRE_POWER.get());
    }

    public float getYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevYaw, this.yaw);
    }

    public float getPitch(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevPitch, this.pitch);
    }

    public void syncAttributes(float yaw, float pitch, byte firePower, boolean fire, Player controllingPlayer) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.powerLevel = firePower;
        if (fire) this.ignite(controllingPlayer);
    }


    public void setPitch(float pitch) {
        this.pitch = Mth.wrapDegrees(pitch);
    }

    public void setYaw(float yaw) {
        this.yaw = Mth.wrapDegrees(yaw);
    }

    public void changeFirePower(int scrollDelta) {
        this.powerLevel = (byte) (1 + Math.floorMod(this.powerLevel - 1 + scrollDelta, 4));
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
            } else CannonController.startControlling(this);
        } else if (player instanceof ServerPlayer sp) PlatHelper.openCustomMenu(sp, this, worldPosition);

    }

    public void ignite(@Nullable Player controllingPlayer) {
        if (this.getProjectile().isEmpty()) return;

        this.level.playSound(null, worldPosition, ModSounds.GUNPOWDER_IGNITE.get(), SoundSource.BLOCKS, 1.0f,
                1.8f + level.getRandom().nextFloat() * 0.2f);
        // called from server when firing
        this.timeUntilFire = 1;
        //update other clients
        this.level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 3);
        this.level.blockEvent(worldPosition, this.getBlockState().getBlock(), 0, 0);
        this.playerWhoIgnitedUUID = controllingPlayer != null ? controllingPlayer.getUUID() : null;
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
        if (this.getProjectile().isEmpty()) return;

        if (level.isClientSide) {
            //call directly on client
            level.blockEvent(worldPosition, this.getBlockState().getBlock(), 1, 0);
        } else {
            if (this.shootProjectile()) {
                Player p = getControllingPlayer();
                if (p == null || !p.isCreative()) {
                    ItemStack fuel = this.getFuel();
                    fuel.shrink(this.powerLevel);
                    this.setFuel(fuel);

                    ItemStack projectile = this.getProjectile();
                    projectile.shrink(1);
                    this.setProjectile(projectile);
                    this.setChanged();
                    this.level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 3);
                }
            }

        }
        this.disabledCooldown = 1;
    }

    private boolean shootProjectile() {
        Vec3 facing = Vec3.directionFromRotation(this.pitch, this.yaw).scale(-1);
        ItemStack projectile = this.getProjectile();

        IFireItemBehavior behavior = CannonBlock.getCannonBehavior(getProjectile().getItem());

        return behavior.fire(projectile.copy(), (ServerLevel) level, worldPosition, 0.5f,
                facing, getFirePower(), getTrajectoryData().drag(), 0, getControllingPlayer());
    }


    @Nullable
    private Player getControllingPlayer() {
        if (this.playerWhoIgnitedUUID == null) return null;
        return level.getPlayerByUUID(this.playerWhoIgnitedUUID);
    }


}
