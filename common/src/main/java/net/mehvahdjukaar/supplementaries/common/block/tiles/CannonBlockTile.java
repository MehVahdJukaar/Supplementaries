package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.math.Axis;
import net.mehvahdjukaar.candlelight.api.VirtualOverride;
import net.mehvahdjukaar.moonlight.api.block.IOneUserInteractable;
import net.mehvahdjukaar.moonlight.api.block.OpenableContainerBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.math.EntityAngles;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.cannon.*;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.FireBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IBallisticBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.CannonBallItem;
import net.mehvahdjukaar.supplementaries.common.items.components.CannonballWhitelist;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundCannonAnimationPacket;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestOpenCannonGuiMessage;
import net.mehvahdjukaar.supplementaries.common.network.SyncCannonPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public class CannonBlockTile extends OpenableContainerBlockTile implements IOneUserInteractable {

    public static final int MAX_POWER_LEVEL = 4;
    private final OrientationRig orientation = new OrientationRig();
    @Nullable
    public Object ccPeripheral = null;
    @Nullable
    private CannonballWhitelist breakWhitelist = null;
    // both from 0 to config value. in tick
    private int cooldownTimer = 0;
    private int fuseTimer = 0;
    private byte powerLevel = 1;

    private BallisticData trajectoryData = BallisticData.LINE;
    private Item trajectoryFor = Items.AIR;

    @Nullable
    private UUID entityWhoIgnitedId = null;

    //not saved
    @Nullable
    private UUID controllingEntity = null;

    //delegate all position and rotation logic to this object which is basically a transform object
    private ReferenceFrame referenceFrame = new WorldReferenceFrame(this);
    private YawPitchRestraint restraint = YawPitchRestraint.UNBOUND;

    public CannonBlockTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.CANNON_TILE.get(), pos, blockState, 2);
        this.setRestraint(new YawPitchRestraint(-360f, 360f, -360, 40));
    }

    public void setRestraint(YawPitchRestraint restraint) {
        //this.restraint = restraint;
        //TODO: this is bugged. Restraints dont work properly. Disabled for now.
    }

    @VisibleForDebug
    @ApiStatus.Internal
    public ReferenceFrame getReferenceFrame() {
        return this.referenceFrame;
    }

    public void setReferenceFrame(ReferenceFrame mount) {
        this.referenceFrame = mount;
    }

    @Override
    public BlockPos getBlockPos() {
        return isInWorld() ? super.getBlockPos() :
                BlockPos.containing(this.referenceFrame.position(1));
    }

    public void tick() {
        this.orientation.tick();

        if (this.cooldownTimer > 0) {
            this.cooldownTimer -= 1;
        }
        if (this.fuseTimer > 0) {
            this.fuseTimer -= 1;
            if (this.fuseTimer <= 0) {
                this.fire();
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return referenceFrame.isStillValid(player);
    }

    private @NotNull Vec3 getCannonRecoil() {
        float power = -this.getFirePower();
        return new Vec3(this.getGlobalFacing(1))
                .multiply(power, 0, power);
    }

    private void fire() {
        if (!this.hasRequiredFuelAndProjectiles()) return;

        if (this.getLevel() instanceof ServerLevel sl) {
            //level.blockEvent(worldPosition, this.getBlockState().getBlock(), 1, 0);
            Entity entityWhoIgnited = this.getEntityWhoIgnited();
            if (this.shootProjectile(sl, entityWhoIgnited)) {
                referenceFrame.applyRecoil(this.getCannonRecoil());

                if (!(entityWhoIgnited instanceof Player pl) || !pl.isCreative()) {
                    ItemStack fuel = this.getFuel();
                    fuel.shrink(this.powerLevel);
                    this.setFuel(fuel);
                    ItemStack projectile = this.getProjectile();
                    projectile.shrink(1);
                    this.setProjectile(projectile);
                    this.setChanged();
                    this.syncToClients(false);

                    level.gameEvent(entityWhoIgnited, GameEvent.EXPLODE, this.getGlobalPosition(1));
                }
                NetworkHelper.sendToAllClientPlayersInRange(sl,
                        BlockPos.containing(this.getGlobalPosition(1)), 128,
                        new ClientBoundCannonAnimationPacket(referenceFrame.makeNetworkTarget(), true));
            }
        }
        this.cooldownTimer = CommonConfigs.Functional.CANNON_COOLDOWN.get();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cooldown", this.cooldownTimer);
        tag.putInt("fuse_timer", this.fuseTimer);
        tag.putByte("fire_power", this.powerLevel);
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        if (entityWhoIgnitedId != null) {
            tag.putUUID("ignited_by", entityWhoIgnitedId);
        }
        if (breakWhitelist != null) {
            tag.put("break_whitelist", CannonballWhitelist.CODEC.encodeStart(ops, breakWhitelist).getOrThrow());
        }
        tag.put("orientation", ExtraCodecs.QUATERNIONF.encodeStart(ops, orientation.getRotation(1)).getOrThrow());
        tag.put("trajectory", BallisticData.CODEC.encodeStart(ops, trajectoryData).getOrThrow());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.cooldownTimer = tag.getInt("cooldown");
        this.fuseTimer = Math.max(this.fuseTimer, tag.getInt("fuse_timer")); //don lose client animation
        this.setFirePower(tag.getByte("fire_power"));
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        if (tag.contains("ignited_by")) {
            this.entityWhoIgnitedId = tag.getUUID("ignited_by");
        }
        if (tag.contains("break_whitelist")) {
            this.breakWhitelist = CannonballWhitelist.CODEC.parse(NbtOps.INSTANCE,
                            tag.get("break_whitelist")).resultOrPartial(Supplementaries.LOGGER::error)
                    .orElse(CannonballWhitelist.EMPTY);
        }
        Quaternionf quat;
        //backwards compat
        if (tag.contains("yaw")) {
            float yaw = 180 + tag.getFloat("yaw");
            float pitch = tag.getFloat("pitch");
            quat = EntityAngles.of(pitch, yaw).toQuaternion();
        } else {
            quat = ExtraCodecs.QUATERNIONF.parse(ops, tag.get("orientation"))
                    .resultOrPartial(Supplementaries.LOGGER::error).orElse(new Quaternionf());
        }
        this.orientation.orient(quat);
        this.snapToWantedRotationInstantly();
        this.trajectoryData = BallisticData.CODEC.parse(NbtOps.INSTANCE, tag.get("trajectory"))
                .resultOrPartial(Supplementaries.LOGGER::error)
                .orElse(BallisticData.LINE);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        //recomputes it
        maybeRefreshTrajectoryData();
    }

    private void computeTrajectoryData() {
        ItemStack proj = this.getProjectile();
        var behavior = FireBehaviorsManager.getCannonBehavior(getProjectile().getItem());
        if (behavior instanceof IBallisticBehavior b) {
            this.trajectoryData = b.calculateBallisticData(proj, level);
        } else {
            this.trajectoryData = BallisticData.LINE;
        }
        if (trajectoryData == null) {
            Supplementaries.error();
        }
        trajectoryFor = proj.getItem();
    }

    public boolean readyToFire() {
        return !isOnCooldown() && fuseTimer == 0 && hasRequiredFuelAndProjectiles();
    }

    public boolean hasRequiredFuelAndProjectiles() {
        return !getProjectile().isEmpty() && !getFuel().isEmpty() &&
                getFuel().getCount() >= powerLevel;
    }

    public boolean hasSomeFuelAndProjectiles() {
        return !getProjectile().isEmpty() && !getFuel().isEmpty();
    }

    public boolean isFiring() {
        return fuseTimer > 0;
    }

    public float getFiringAnimation(float partialTicks) {
        if (fuseTimer <= 0) return 0;
        return (fuseTimer - partialTicks) / CommonConfigs.Functional.CANNON_FUSE_TIME.get();
    }

    public boolean isOnCooldown() {
        return cooldownTimer > 0;
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

    public BallisticData getBallisticData() {
        return trajectoryData;
    }

    private void maybeRefreshTrajectoryData() {
        if (level == null || level.isClientSide) return;
        if (trajectoryFor != getProjectile().getItem()) {
            computeTrajectoryData();
        }
    }

    public byte getPowerLevel() {
        return powerLevel;
    }

    public void setFirePower(byte powerLevel) {
        this.powerLevel = (byte) Math.clamp(powerLevel, 1, MAX_POWER_LEVEL);
    }

    public float getFirePower() {
        return (float) (Math.pow(powerLevel, CommonConfigs.Functional.CANNON_FIRE_POWER.get()));
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

    public void ignite(@Nullable Entity entityWhoIgnited) {
        if (level.isClientSide) return;
        //do nothing if its already ignited
        if (this.fuseTimer > 0) return;

        if (this.getProjectile().isEmpty()) return;

        // called from server when firing
        this.fuseTimer = CommonConfigs.Functional.CANNON_FUSE_TIME.get();

        this.entityWhoIgnitedId = entityWhoIgnited != null ? entityWhoIgnited.getUUID() : null;
        //particles
        if (this.level instanceof ServerLevel serverLevel) {
            NetworkHelper.sendToAllClientPlayersInDefaultRange(serverLevel,
                    BlockPos.containing(this.getGlobalPosition(1)),
                    new ClientBoundCannonAnimationPacket(referenceFrame.makeNetworkTarget(), false));

            this.setChanged();
            //update other clients
            this.syncToClients(true);
        }

    }


    protected boolean shootProjectile(ServerLevel serverLevel, Entity entityWhoFired) {
        Vec3 facing = new Vec3(this.getGlobalFacing(1));
        ItemStack projectile = this.getProjectile().copy();

        if (projectile.getItem() instanceof CannonBallItem && breakWhitelist != null) {
            //hack for cannonballs
            projectile.set(ModComponents.CANNONBALL_WHITELIST.get(), breakWhitelist);
        }

        IFireItemBehavior behavior = FireBehaviorsManager.getCannonBehavior(getProjectile().getItem());

        float firePower = getFirePower();

        return behavior.fire(projectile.copy(), serverLevel, this.getGlobalPosition(1), 0.5f,
                facing, firePower, 0, entityWhoFired);
    }

    @Nullable
    protected Entity getEntityWhoIgnited() {
        UUID uuid = this.controllingEntity;
        if (uuid == null && entityWhoIgnitedId != null) {
            uuid = entityWhoIgnitedId;
        }
        if (uuid == null) return null;
        return level.getPlayerByUUID(uuid);
    }

    @Nullable
    @Override
    public UUID getCurrentUser() {
        return controllingEntity;
    }

    @Override
    public void setCurrentUser(@Nullable UUID uuid) {
        this.controllingEntity = uuid;
    }

    @VirtualOverride("neoforge")
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        //Unused. PlatHelper.openCUstomMenu writes more data ontop of that. only problem is spectator...
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv) {
        //thanks mojank
        if (inv.player.isSpectator()) return null;
        return new CannonContainerMenu(id, inv, this);
    }

    @Override
    public void unpackLootTable(@Nullable Player player) {
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey != null) super.unpackLootTable(player);
        //fix loot table shit. it doesn't even check if stuff can go in a slot. thanks mojank
        if (resourceKey != getLootTable()) {
            //if it has just unpacked
            ItemStack currentAmmo = this.getProjectile();
            ItemStack currentFuel = this.getFuel();
            //consolidate
            if (currentAmmo.is(currentFuel.getItem())) {
                currentFuel.setCount(currentFuel.getCount() + currentAmmo.getCount());
                currentAmmo = ItemStack.EMPTY;
            }
            if (this.canPlaceItem(0, currentFuel) && canPlaceItem(1, currentFuel)) {
            } else if (this.canPlaceItem(0, currentAmmo) && canPlaceItem(1, currentFuel)) {
                //swap
                var temp = currentAmmo;
                currentAmmo = currentFuel;
                currentFuel = temp;
            } else {
                currentFuel = ItemStack.EMPTY;
                currentAmmo = ItemStack.EMPTY;
            }
            this.setFuel(currentFuel);
            this.setProjectile(currentAmmo);
        }
    }


    //new stuff

    public void snapToWantedRotationInstantly() {
        this.orientation.tick();
        this.orientation.tick();
    }

    public void setLocalOrientation(Quaternionf localRot) {
        //remove structure rot
        Quaternionf structureRot = getStructureAdditionalRotation();
        Quaternionf cannonRot = localRot.mul(structureRot.invert(new Quaternionf()));
        //clamp
        this.orientation.orient(this.restraint.clamp(cannonRot));
    }

    public void setWorldOrientation(Quaternionf worldRot) {
        Quaternionf referenceRot = referenceFrame.getRotation(1);
        Quaternionf inverseReferenceRot = referenceRot.invert(new Quaternionf());
        Quaternionf localRot = inverseReferenceRot.mul(worldRot, new Quaternionf());
        setLocalOrientation(localRot);
    }

    public Quaternionf getLocalOrientation(float partialTicks) {
        Quaternionf rot = orientation.getRotation(partialTicks);
        Quaternionf additionalRot = getStructureAdditionalRotation();
        return rot.mul(additionalRot);
    }

    public Quaternionf getWorldOrientation(float partialTicks) {
        Quaternionf localRot = getLocalOrientation(partialTicks);
        Quaternionf referenceRot = referenceFrame.getRotation(partialTicks);
        return localRot.mul(referenceRot);
    }

    public void setTrustedInternalAttributes(Quaternionf wantedRotation, byte firePower, boolean fire, Entity controllingEntity) {
        this.setLocalOrientation(wantedRotation);
        this.setFirePower(firePower);
        if (fire) this.ignite(controllingEntity);
    }

    private Quaternionf getStructureAdditionalRotation() {
        return Axis.YP.rotationDegrees(-this.getBlockState().getValue(CannonBlock.ROTATE_TILE).ordinal() * 90);
    }

    public Vector3f getGlobalFacing(float partialTicks) {
        Quaternionf rot = getWorldOrientation(partialTicks);
        Vector3f forward = new Vector3f(0, 0, 1);
        rot.transform(forward);
        return forward;
    }

    private Vector3f getLocalFacing(float partialTicks) {
        Quaternionf rot = getLocalOrientation(partialTicks);
        Vector3f forward = new Vector3f(0, 0, 1);
        rot.transform(forward);
        return forward;
    }

    public Vec3 getGlobalPosition(float partialTicks) {
        return referenceFrame.position(partialTicks);
    }

    public Vec3 getGlobalVelocity() {
        return referenceFrame.velocity();
    }

    public YawPitchRestraint getOrientationRestraints() {
        BlockState state = this.getBlockState();
        Direction dir = state.getValue(CannonBlock.FACING).getOpposite();
        return restraint.rotated(dir);
    }


    // Network
    public void syncToServer(boolean ignite, boolean removeOwner, Player playerWhoChangedIt) {
        NetworkHelper.sendToServer(new SyncCannonPacket(
                this.orientation.getWantedRotation(), this.getPowerLevel(),
                ignite, removeOwner, referenceFrame.makeNetworkTarget(),
                playerWhoChangedIt.getUUID()));
    }

    public void syncToClients(boolean ignite) {
        if (level instanceof ServerLevel sl) {
            NetworkHelper.sendToAllClientPlayersInDefaultRange(sl,
                    BlockPos.containing(referenceFrame.position(1)), new SyncCannonPacket(
                            this.orientation.getWantedRotation(), this.getPowerLevel(),
                            ignite, false, referenceFrame.makeNetworkTarget(), null));
        }
    }

    public void sendOpenGuiRequest() {
        NetworkHelper.sendToServer(new ServerBoundRequestOpenCannonGuiMessage(referenceFrame.makeNetworkTarget()));
    }

    public boolean shouldRotatePlayerFaceWhenManeuvering() {
        return referenceFrame.shouldRotatePlayerFaceWhenManeuvering();
    }

    public boolean impedePlayerMovementWhenManeuvering() {
        return referenceFrame.impedePlayerMovementWhenManeuvering();
    }

    public boolean canManeuverFromGUI(Player player) {
        return referenceFrame.canManeuverFromGUI(player);
    }

    public boolean isInWorld() {
        return referenceFrame instanceof WorldReferenceFrame;
    }


    public void setRotationToMatchTrajectory(BallisticTrajectory3D trajectory3D, float partialTick) {
        BallisticTrajectory trajectory = trajectory3D.trajectory();
        float followSpeed = 1;
        Quaternionf rot = this.getWorldOrientation(partialTick);
        EntityAngles eulerAngles = EntityAngles.fromQuaternion(rot);

        //we used some flipped coordinates in trajectory calculation. we got to adjust them here.
        float newPitch = Mth.rotLerp(followSpeed, eulerAngles.pitch(),
                trajectory.pitch() * Mth.RAD_TO_DEG);
        // targetYawDeg = Mth.rotLerp(followSpeed, this.getYaw(0), targetYawDeg);
        float newYaw = trajectory3D.yaw() * Mth.RAD_TO_DEG;
        this.setWorldOrientation(EntityAngles.of(newPitch, newYaw).toQuaternion());
    }
}
