package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOneUserInteractable;
import net.mehvahdjukaar.moonlight.api.block.OpenableContainerBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IBallisticBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.CannonBallItem;
import net.mehvahdjukaar.supplementaries.common.items.components.CannonballWhitelist;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundCannonAnimationPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class CannonBlockTile extends OpenableContainerBlockTile implements IOneUserInteractable {

    public static final int MAX_POWER_LEVEL = 4;
    public Object ccHack = null;

    //no list = normal behavior. empty list = cant break anything
    //not using a tag as this is meant to be edited with commands immediately without a tag being there
    @Nullable
    private Set<Block> breakWhitelist = null;

    private float pitch = 0;
    private float prevPitch = 0;
    private float yaw = 0;
    private float prevYaw = 0;

    // both from 0 to config value. in tick
    private int cooldownTimer = 0;
    private int fuseTimer = 0;
    private byte powerLevel = 1;

    private IBallisticBehavior.Data trajectoryData = IBallisticBehavior.LINE;
    private Item trajectoryFor = Items.AIR;

    @Nullable
    private UUID playerWhoIgnitedUUID = null;

    //not saved
    @Nullable
    private UUID controllingEntity = null;

    public CannonBlockTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.CANNON_TILE.get(), pos, blockState, 2);
    }

    public CannonBlockTile(BlockPos pos, BlockState blockState, float initialYaw) {
        this(pos, blockState);
        this.yaw = initialYaw;
        this.prevYaw = initialYaw;
    }

    public final CannonAccess selfAccess = CannonAccess.block(this);

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
        if (!this.hasRequiredFuelAndProjectiles()) return;

        if (this.getLevel() instanceof ServerLevel sl) {
            //level.blockEvent(worldPosition, this.getBlockState().getBlock(), 1, 0);
            if (this.shootProjectile(sl, access)) {
                access.applyRecoil();

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

                    level.gameEvent(p, GameEvent.EXPLODE, access.getCannonGlobalPosition(1));
                }
                NetworkHelper.sendToAllClientPlayersInRange(sl,
                        BlockPos.containing(access.getCannonGlobalPosition(1)), 128,
                        new ClientBoundCannonAnimationPacket(access.makeNetworkTarget(), true));
            }
        } else {
            // access.playFiringEffects();
        }
        this.cooldownTimer = CommonConfigs.Functional.CANNON_COOLDOWN.get();
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
        this.fuseTimer = Math.max(this.fuseTimer, tag.getInt("fuse_timer")); //don lose client animation
        this.setPowerLevel(tag.getByte("fire_power"));
        if (tag.contains("player_ignited")) {
            this.playerWhoIgnitedUUID = tag.getUUID("player_ignited");
        }
        this.breakWhitelist = readBreakWhitelist(tag, registries);
        if (tag.contains("trajectory")) {
            this.trajectoryData = IBallisticBehavior.Data.CODEC.parse(NbtOps.INSTANCE, tag.get("trajectory"))
                    .getOrThrow();
        }
        // fixRotation(this.level);
    }

    private void fixRotation(Level level) {
        //structure block rotation decoding
        BlockState state = this.getBlockState();
        Rotation rot = state.getValue(ModBlockProperties.ROTATE_TILE);
        if (rot != Rotation.NONE && level != null && !level.isClientSide) {
            this.setYaw(this.selfAccess, this.yaw + (rot.ordinal() * 90));
            level.setBlockAndUpdate(worldPosition, state.setValue(ModBlockProperties.ROTATE_TILE, Rotation.NONE));
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

    public IBallisticBehavior.Data getTrajectoryData() {
        return trajectoryData;
    }

    private void recomputeTrajectoryData() {
        if (level == null || level.isClientSide) return;
        if (trajectoryFor != getProjectile().getItem()) {
            computeTrajectoryData();
        }
    }

    public byte getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(byte powerLevel) {
        this.powerLevel = (byte) Math.clamp(powerLevel, 1, MAX_POWER_LEVEL);
    }

    public float getFirePower() {
        return (float) (Math.pow(powerLevel, CommonConfigs.Functional.CANNON_FIRE_POWER.get()));
    }

    private float getStructureYaw() {
        return this.getBlockState().getValue(CannonBlock.ROTATE_TILE).ordinal() * 90;
    }

    public float getYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevYaw, this.yaw) + getStructureYaw();
    }

    public float getYaw() {
        return yaw + getStructureYaw();
    }

    public float getPitch(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevPitch, this.pitch);
    }

    public float getPitch() {
        return pitch;
    }

    public void setAttributes(float yaw, float pitch, byte firePower, boolean fire,
                              Player controllingPlayer, CannonAccess access) {
        this.setYaw(access, yaw);
        this.setPitch(access, pitch);
        this.setPowerLevel(firePower);
        if (fire) this.ignite(controllingPlayer, access);
    }

    public void setPitch(CannonAccess access, float relativePitch) {
        var r = access.getPitchAndYawRestrains();
        this.pitch = MthUtils.clampDegrees(relativePitch, r.minPitch(), r.maxPitch());
    }

    public void setYaw(CannonAccess access, float relativeYaw) {
        var r = access.getPitchAndYawRestrains();
        this.yaw = MthUtils.clampDegrees(relativeYaw, r.minYaw(), r.maxYaw()) - getStructureYaw();
    }

    // sets both prev and current yaw. Only makes sense to be called from render thread
    public void setRenderYaw(CannonAccess access, float relativeYaw) {
        setYaw(access, relativeYaw);
        this.prevYaw = this.yaw;
    }

    public void setRenderPitch(CannonAccess access, float pitch) {
        setPitch(access, pitch);
        this.prevPitch = this.pitch;
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

    public void ignite(@Nullable Entity entityWhoIgnited, CannonAccess access) {
        //do nothing if its already ignited
        if (this.fuseTimer > 0) return;

        if (this.getProjectile().isEmpty()) return;

        // called from server when firing
        this.fuseTimer = CommonConfigs.Functional.CANNON_FUSE_TIME.get();

        //particles
        if (this.level instanceof ServerLevel serverLevel) {
            NetworkHelper.sendToAllClientPlayersInDefaultRange(serverLevel,
                    BlockPos.containing(access.getCannonGlobalPosition(1)),
                    new ClientBoundCannonAnimationPacket(access.makeNetworkTarget(), false));
        }
        this.playerWhoIgnitedUUID = entityWhoIgnited != null ? entityWhoIgnited.getUUID() : null;

        this.setChanged();
        //update other clients
        access.updateClients();
    }


    protected boolean shootProjectile(ServerLevel serverLevel, CannonAccess access) {
        Vec3 facing = access.getCannonGlobalFacing(1).scale(-1);
        ItemStack projectile = this.getProjectile().copy();

        if (projectile.getItem() instanceof CannonBallItem && breakWhitelist != null) {
            //hack for cannonballs
            projectile.set(ModComponents.CANNONBALL_WHITELIST.get(), new CannonballWhitelist(breakWhitelist));
        }

        IFireItemBehavior behavior = CannonBlock.getCannonBehavior(getProjectile().getItem());

        float firePower = getFirePower();

        return behavior.fire(projectile.copy(), serverLevel, access.getCannonGlobalPosition(1), 0.5f,
                facing, firePower, 0, getPlayerWhoFired());
    }

    @Nullable
    protected Player getPlayerWhoFired() {
        UUID uuid = this.controllingEntity;
        if (uuid == null && playerWhoIgnitedUUID != null) {
            uuid = playerWhoIgnitedUUID;
        }
        if (uuid == null) return null;
        return level.getPlayerByUUID(uuid);
    }

    @Override
    public void setCurrentUser(@Nullable UUID uuid) {
        this.controllingEntity = uuid;
    }

    @Nullable
    @Override
    public UUID getCurrentUser() {
        return controllingEntity;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv) {
        //thanks mojank
        if (inv.player.isSpectator()) return null;
        return new CannonContainerMenu(id, inv, this.selfAccess);
    }

    @Override
    public void unpackLootTable(@Nullable Player player) {
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        super.unpackLootTable(player);
        //fix loot table shit. it doesnt even check if stuff can go in a slot. thanks mojank
        if (resourceKey != getLootTable()) {
            //if has just unpacked
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
}
