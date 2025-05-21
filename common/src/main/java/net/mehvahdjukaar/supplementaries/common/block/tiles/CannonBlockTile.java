package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOnePlayerInteractable;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IBallisticBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.entities.FallingUrnEntity;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.CannonBallItem;
import net.mehvahdjukaar.supplementaries.common.items.components.CannonballWhitelist;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundControlCannonPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class CannonBlockTile extends OpeneableContainerBlockEntity implements IOnePlayerInteractable {


    //no list = normal behavior. empty list = cant break anything
    //not using a tag as this is meant to be edited with commands immediately without a tag being there
    @Nullable
    private Set<Block> breakWhitelist = null;

    protected float pitch = 0;
    protected float prevPitch = 0;
    protected float yaw = 0;
    protected float prevYaw = 0;

    // both from 0 to config value. in tick
    protected int cooldownTimer = 0;
    protected int fuseTimer = 0;
    protected byte powerLevel = 1;

    private IBallisticBehavior.Data trajectoryData = IBallisticBehavior.LINE;
    private Item trajectoryFor = Items.AIR;

    @Nullable
    private UUID playerWhoIgnitedUUID = null;

    //not saved
    @Nullable
    private UUID controllingPlayer = null;

    private boolean isBig = false;

    public CannonBlockTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.CANNON_TILE.get(), pos, blockState, 2);
    }

    public final CannonAccess selfAccess = CannonAccess.tile(this);


    public void tick(CannonAccess access) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;

        if (this.cooldownTimer > 0) {
            this.cooldownTimer -= 1;
        }
        if (this.fuseTimer > 0) {
            this.fuseTimer -= 1;
            if (this.fuseTimer <= 0) {
                this.fire(access);
            }
        }
    }

    private void fire(CannonAccess access) {
        if (!this.hasFuelAndProjectiles()) return;

        if (this.getLevel() instanceof ServerLevel sl) {
            //level.blockEvent(worldPosition, this.getBlockState().getBlock(), 1, 0);
            if (this.shootProjectile(sl, access)) {
                Player p = this.getPlayerWhoFired();
                if (p == null || !p.isCreative()) {
                    ItemStack fuel = this.getFuel();
                    fuel.shrink(this.powerLevel);
                    this.setFuel(fuel);

                    ItemStack projectile = this.getProjectile();
                    projectile.shrink(1);
                    this.setProjectile(projectile);
                    this.setChanged();
                    access.updateClients();

                    level.gameEvent(p, GameEvent.EXPLODE, access.getCannonPosition());
                }
            }
        } else {
            access.playFireEffects();
        }
        this.cooldownTimer = CommonConfigs.Functional.CANNON_COOLDOWN.get();
    }


    public boolean isBig() {
        return isBig;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        tag.putInt("cooldown", this.cooldownTimer);
        tag.putInt("fuse_timer", this.fuseTimer);
        tag.putByte("fire_power", this.powerLevel);
        if (playerWhoIgnitedUUID != null) tag.putUUID("player_ignited", playerWhoIgnitedUUID);
        if (breakWhitelist != null) {
            saveBreakWhitelist(breakWhitelist, tag, registries);
        }
        if (isBig) {
            tag.putBoolean("big", true);
        }
        tag.put("trajectory", IBallisticBehavior.Data.CODEC.encodeStart(NbtOps.INSTANCE, trajectoryData).getOrThrow());
    }

    public static void saveBreakWhitelist(Set<Block> breakWhitelist, CompoundTag tag, HolderLookup.Provider registries) {
        CannonballWhitelist.CODEC.encodeStart(
                        registries.createSerializationContext(NbtOps.INSTANCE), new CannonballWhitelist(breakWhitelist))
                .ifSuccess(t -> tag.put("break_whitelist", t));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.cooldownTimer = tag.getInt("cooldown");
        this.fuseTimer = tag.getInt("fuse_timer");
        this.powerLevel = tag.getByte("fire_power");
        if (tag.contains("player_ignited")) {
            this.playerWhoIgnitedUUID = tag.getUUID("player_ignited");
        }
        this.breakWhitelist = readBreakWhitelist(tag, registries);
        if (tag.contains("big")) {
            this.isBig = true;
        }
        if(tag.contains("trajectory")){
            this.trajectoryData = IBallisticBehavior.Data.CODEC.parse(NbtOps.INSTANCE, tag.get("trajectory"))
                    .getOrThrow();
        }
    }

    @Nullable
    public static Set<Block> readBreakWhitelist(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("break_whitelist")) {
            return CannonballWhitelist.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),
                            tag.get("break_whitelist")).result()
                    .map(CannonballWhitelist::blocks).orElse(Set.of());
        }
        return null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        //recomputes it
        recomputeTrajectoryData();
    }

    private void computeTrajectoryData() {
        ItemStack proj = this.getProjectile();
        var behavior = CannonBlock.getCannonBehavior(getProjectile().getItem());
        if (behavior instanceof IBallisticBehavior b) {
            this.trajectoryData = b.calculateData(proj, level);
        } else {
            this.trajectoryData = IBallisticBehavior.LINE;
        }
        if (trajectoryData == null) {
            Supplementaries.error();
        }
        trajectoryFor = proj.getItem();
    }

    public boolean readyToFire() {
        return cooldownTimer == 0 && fuseTimer == 0 && hasFuelAndProjectiles();
    }

    public boolean hasFuelAndProjectiles() {
        return !getProjectile().isEmpty() && !getFuel().isEmpty() &&
                getFuel().getCount() >= powerLevel;
    }

    public boolean isFiring() {
        return fuseTimer > 0;
    }

    public float getFiringAnimation(float partialTicks) {
        if (fuseTimer <= 0) return 0;
        return (fuseTimer - partialTicks) / CommonConfigs.Functional.CANNON_FUSE_TIME.get();
    }

    public float getCooldownAnimation(float partialTicks) {
        if (cooldownTimer <= 0) return 0;
        return (cooldownTimer - partialTicks) / CommonConfigs.Functional.CANNON_COOLDOWN.get();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
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

    public IBallisticBehavior.Data getTrajectoryData() {
        return trajectoryData;
    }

    private void recomputeTrajectoryData() {
        if (level.isClientSide) return;
        if (trajectoryFor != getProjectile().getItem()) {
            computeTrajectoryData();
        }
    }

    public byte getPowerLevel() {
        return powerLevel;
    }

    public float getFirePower() {
        return (float) (Math.pow(powerLevel, CommonConfigs.Functional.CANNON_FIRE_POWER.get()));
    }

    public float getYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevYaw, this.yaw);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevPitch, this.pitch);
    }

    public float getPitch() {
        return pitch;
    }

    public void setAttributes(float yaw, float pitch, byte firePower, boolean fire,
                              Player controllingPlayer, CannonAccess access) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.powerLevel = firePower;
        if (fire) this.ignite(controllingPlayer, access);
    }


    public void setRestrainedPitch(float pitch) {
        Restraint r = this.getPitchAndYawRestrains();
        this.pitch = Mth.clamp(Mth.wrapDegrees(pitch), r.minPitch, r.maxPitch);
    }

    public void setRestrainedYaw(float yaw) {
        Restraint r = this.getPitchAndYawRestrains();
        this.yaw = Mth.clamp(Mth.wrapDegrees(yaw), r.minYaw, r.maxYaw);
    }

    // sets both prev and current yaw. Only makes sense to be called from render thread
    public void setRenderYaw(float yaw) {
        setRestrainedYaw(yaw);
        this.prevYaw = this.yaw;
    }

    public void setRenderPitch(float pitch) {
        setRestrainedPitch(pitch);
        this.prevPitch = this.pitch;
    }

    public record Restraint(float minYaw, float maxYaw, float minPitch, float maxPitch) {
    }

    public Restraint getPitchAndYawRestrains() {
        BlockState state = this.getBlockState();
        return switch (state.getValue(CannonBlock.FACING).getOpposite()) {
            case NORTH -> new Restraint(70, 290, -360, 360);
            case SOUTH -> new Restraint(-110, 110, -360, 360);
            case EAST -> new Restraint(-200, 20, -360, 360);
            case WEST -> new Restraint(-20, 200, -360, 360);
            case UP -> new Restraint(-360, 360, -200, 20);
            case DOWN -> new Restraint(-360, 360, -20, 200);
        };
    }

    public void changeFirePower(int scrollDelta) {
        this.powerLevel = (byte) (1 + Math.floorMod(this.powerLevel - 1 + scrollDelta, 4));
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.supplementaries.cannon");
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
        return !stack.is(ModTags.CANNON_BLACKLIST);
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

    @Override
    public boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos, ItemStack stack, Direction face) {
        if (player.isSecondaryUseActive()) {
            //same as super but sends custom packet
            if (!this.isOtherPlayerEditing(pos, player)) {
                // open gui (edit sign with empty hand)
                this.setPlayerWhoMayEdit(player.getUUID());
                NetworkHelper.sendToClientPlayer(player, new ClientBoundControlCannonPacket(TileOrEntityTarget.of(this)));
            }
            return true;
        }
        return IOnePlayerInteractable.super.tryOpeningEditGui(player, pos, stack, face);
    }

    public void ignite(@Nullable Entity entityWhoIgnited, CannonAccess access) {
        //do nothing if its already ignited
        if (this.fuseTimer > 0) return;

        if (this.getProjectile().isEmpty()) return;

        // called from server when firing
        this.fuseTimer = CommonConfigs.Functional.CANNON_FUSE_TIME.get();

        //make this entity agnostic
        //particles
        access.playIgniteEffects();
        this.playerWhoIgnitedUUID = entityWhoIgnited != null ? entityWhoIgnited.getUUID() : null;

        this.setChanged();
        //update other clients
        this.level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }


    protected boolean shootProjectile(ServerLevel serverLevel, CannonAccess access) {
        Vec3 facing = Vec3.directionFromRotation(this.pitch, this.yaw).scale(-1);
        ItemStack projectile = this.getProjectile().copy();

        if (projectile.getItem() instanceof CannonBallItem && breakWhitelist != null) {
            //hack for cannonballs
            projectile.set(ModComponents.CANNONBALL_WHITELIST.get(), new CannonballWhitelist(breakWhitelist));
        }

        IFireItemBehavior behavior = CannonBlock.getCannonBehavior(getProjectile().getItem());

        return behavior.fire(projectile.copy(), serverLevel, access.getCannonPosition(), 0.5f,
                facing, getFirePower(), 0, getPlayerWhoFired());
    }


    @Nullable
    protected Player getPlayerWhoFired() {
        UUID uuid = this.controllingPlayer;
        if (uuid == null && playerWhoIgnitedUUID != null) {
            uuid = playerWhoIgnitedUUID;
        }
        if (uuid == null) return null;
        return level.getPlayerByUUID(uuid);
    }

    @Override
    public void setPlayerWhoMayEdit(@Nullable UUID uuid) {
        this.controllingPlayer = uuid;
    }

    @Override
    public UUID getPlayerWhoMayEdit() {
        return controllingPlayer;
    }


    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv) {
        //thanks mojank
        if (inv.player.isSpectator()) return null;
        return new CannonContainerMenu(id, inv, this.selfAccess);
    }

}
