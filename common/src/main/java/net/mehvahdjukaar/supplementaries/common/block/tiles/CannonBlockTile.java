package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.IOnePlayerInteractable;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IBallisticBehavior;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IFireItemBehavior;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBallEntity;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.CannonBallItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundControlCannonPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSyncCannonPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CannonBlockTile extends OpeneableContainerBlockEntity implements IOnePlayerInteractable {


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
    private UUID controllingPlayer = null;

    public CannonBlockTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.CANNON_TILE.get(), pos, blockState, 2);
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
            saveBreakWhitelist(breakWhitelist, tag);
        }
    }

    public static void saveBreakWhitelist(Set<Block> breakWhitelist, CompoundTag tag) {
        ListTag list = new ListTag();
        for (Block b : breakWhitelist) {
            list.add(StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(b).toString()));
        }
        tag.put("break_whitelist", list);
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
        this.breakWhitelist = readBreakWhitelist(tag);
    }

    @Nullable
    public static Set<Block> readBreakWhitelist(CompoundTag tag) {
        if (tag.contains("break_whitelist")) {
            ListTag list = tag.getList("break_whitelist", 8);
            var breakWhitelist = new HashSet<Block>();
            for (int i = 0; i < list.size(); i++) {
                Block b = BuiltInRegistries.BLOCK.get(new ResourceLocation(list.getString(i)));
                breakWhitelist.add(b);
            }
            return breakWhitelist;
        }
        return null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        //this.trajectoryData = null;
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
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
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
        if (trajectoryFor != getProjectile().getItem()) {
            computeTrajectoryData();
        }
        return trajectoryData;
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

    public void setAttributes(float yaw, float pitch, byte firePower, boolean fire, Player controllingPlayer) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.powerLevel = firePower;
        if (fire) this.ignite(controllingPlayer);
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

    @Override
    public boolean tryOpeningEditGui(ServerPlayer player, BlockPos pos, ItemStack stack) {
        if (player.isSecondaryUseActive()) {
            //same as super but sends custom packet
            if ( !this.isOtherPlayerEditing(player)) {
                // open gui (edit sign with empty hand)
                this.setPlayerWhoMayEdit(player.getUUID());
                NetworkHelper.sendToClientPlayer(player, new ClientBoundControlCannonPacket(this.worldPosition));
            }
            return true;
        }
        return IOnePlayerInteractable.super.tryOpeningEditGui(player, pos, stack);
    }

    public void ignite(@Nullable Entity entityWhoIgnited) {
        if (this.getProjectile().isEmpty()) return;

        // called from server when firing
        this.fuseTimer = CommonConfigs.Functional.CANNON_FUSE_TIME.get();

        //particles
        this.level.blockEvent(worldPosition, this.getBlockState().getBlock(), 0, 0);
        this.playerWhoIgnitedUUID = entityWhoIgnited != null ? entityWhoIgnited.getUUID() : null;

        this.setChanged();
        //update other clients
        this.level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockTile t) {
        t.prevYaw = t.yaw;
        t.prevPitch = t.pitch;

        if (t.cooldownTimer > 0) {
            t.cooldownTimer -= 1;
        }
        if (t.fuseTimer > 0) {
            t.fuseTimer -= 1;
            if (t.fuseTimer <= 0) {
                t.fire();
            }
        }
    }

    private void fire() {
        if (!this.hasFuelAndProjectiles()) return;

        if (level instanceof ServerLevel sl) {
            //level.blockEvent(worldPosition, this.getBlockState().getBlock(), 1, 0);
            if (this.shootProjectile(sl)) {
                Player p = getPlayerWhoFired();
                if (p == null || !p.isCreative()) {
                    ItemStack fuel = this.getFuel();
                    fuel.shrink(this.powerLevel);
                    this.setFuel(fuel);

                    ItemStack projectile = this.getProjectile();
                    projectile.shrink(1);
                    this.setProjectile(projectile);
                    this.setChanged();
                    this.level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), 3);

                    level.gameEvent(p, GameEvent.EXPLODE, worldPosition);
                }
            }
        } else {
            //call directly on client. happens 1 tick faster is this needed?
            level.blockEvent(worldPosition, this.getBlockState().getBlock(), 1, 0);
        }
        this.cooldownTimer = CommonConfigs.Functional.CANNON_COOLDOWN.get();
    }

    private boolean shootProjectile(ServerLevel serverLevel) {
        Vec3 facing = Vec3.directionFromRotation(this.pitch, this.yaw).scale(-1);
        ItemStack projectile = this.getProjectile().copy();

        if (projectile.getItem() instanceof CannonBallItem && breakWhitelist != null) {
            //hack for cannonballs
            saveBreakWhitelist(breakWhitelist, projectile.getOrCreateTag());
        }

        IFireItemBehavior behavior = CannonBlock.getCannonBehavior(getProjectile().getItem());

        return behavior.fire(projectile.copy(), serverLevel, worldPosition, 0.5f,
                facing, getFirePower(), 0, getPlayerWhoFired());
    }


    @Nullable
    private Player getPlayerWhoFired() {
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
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new CannonContainerMenu(id, player, this);
    }


    public static void syncToServer(CannonBlockTile cannon, boolean fire, boolean removeOwner) {
        NetworkHelper.sendToServer(new ServerBoundSyncCannonPacket(
                cannon.getYaw(), cannon.getPitch(), cannon.getPowerLevel(),
                fire, cannon.getBlockPos(), removeOwner));
    }
}
