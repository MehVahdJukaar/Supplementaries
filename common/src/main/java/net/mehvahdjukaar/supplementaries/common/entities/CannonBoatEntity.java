package net.mehvahdjukaar.supplementaries.common.entities;//


import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundUpdateCannonBoatPacket;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestOpenCannonGuiMessage;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSyncCannonPacket;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CannonBoatEntity extends Boat implements HasCustomInventoryScreen, ContainerEntity, CannonAccess, IControllableVehicle {

    private static final EntityDataAccessor<WoodType> DATA_WOOD_TYPE =
            SynchedEntityData.defineId(
                    CannonBoatEntity.class, WoodType.ENTITY_SERIALIZER.get());

    private final CannonBlockTile cannon;

    public CannonBoatEntity(EntityType<CannonBoatEntity> entityType, Level level) {
        super(entityType, level);
        this.cannon = new CannonBlockTile(BlockPos.ZERO, ModRegistry.CANNON.get()
                .defaultBlockState().setValue(CannonBlock.FACING, Direction.UP));
        this.cannon.setLevel(level);
        this.setWoodType(WoodTypeRegistry.OAK_TYPE);
    }

    public CannonBoatEntity(Level level, CannonBlockTile cannon, double x, double y, double z, WoodType type) {
        this(ModEntities.CANNON_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setWoodType(type);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WOOD_TYPE, WoodTypeRegistry.OAK_TYPE);
    }

    public void setWoodType(WoodType type) {
        this.entityData.set(DATA_WOOD_TYPE, type);
    }

    public WoodType getWoodType() {
        return entityData.get(DATA_WOOD_TYPE);
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15F;
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("WoodType", getWoodType().getId().toString());
        var cannonTag = this.cannon.saveWithoutMetadata(this.registryAccess());
        compound.put("Cannon", cannonTag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        String woodTypeId = compound.getString("WoodType");
        this.setWoodType(WoodTypeRegistry.getValue(woodTypeId));
        if (compound.contains("Cannon")) {
            var cannonTag = compound.getCompound("Cannon");
            this.cannon.loadWithComponents(cannonTag, this.registryAccess());
        }
    }

    @Override
    public void destroy(DamageSource source) {
        this.destroy(this.getDropItem());
        this.chestVehicleDestroyed(source, this.level(), this);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }
        super.remove(reason);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult interactionResult;
        if (!player.isSecondaryUseActive()) {
            interactionResult = super.interact(player, hand);
            if (interactionResult != InteractionResult.PASS) {
                return interactionResult;
            }
        }

        if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            interactionResult = this.interactWithContainerVehicle(player);
            if (interactionResult.consumesAction()) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, player);
                PiglinAi.angerNearbyPiglins(player, true);
            }

            return interactionResult;
        }
    }

    @Override
    public InteractionResult interactWithContainerVehicle(Player player) {
        if (player instanceof ServerPlayer sp) {
            PlatHelper.openCustomMenu(sp, this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public Item getDropItem() {
        Item var10000;


        return ModRegistry.ASH_BRICK_ITEM.get();
    }

    @Override
    public void clearContent() {
        this.cannon.clearContent();
    }

    @Override
    public int getContainerSize() {
        return this.cannon.getContainerSize();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.cannon.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.cannon.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.cannon.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.cannon.setItem(slot, stack);
    }

    @Override
    public SlotAccess getSlot(int slot) {//TODO
        return this.getChestVehicleSlot(slot);
    }

    public void setChanged() {
        this.cannon.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isChestVehicleStillValid(player);
    }

    @Override
    public void openCannonGui(ServerPlayer player) {
        PlatHelper.openCustomMenu(player, this);
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (player instanceof ServerPlayer sp) {
            openCannonGui(sp);
        }
        if (!player.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
        }
    }

    @Nullable
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        //override to create boat menu
        if (inv.player.isSpectator()) return null;
        return new CannonContainerMenu(id, inv, this);
    }

    @Nullable
    public ResourceKey<LootTable> getLootTable() {
        return cannon.getLootTable();
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.cannon.setLootTable(lootTable);
    }

    @Override
    public long getLootTableSeed() {
        return cannon.getLootTableSeed();
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.cannon.setLootTableSeed(lootTableSeed);
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.cannon.getItems();
    }

    @Override
    public void clearItemStacks() {
        this.cannon.clearContent();
    }

    @Override
    public void stopOpen(Player player) {
        this.cannon.stopOpen(player);
    }

    @Override
    public CannonBlockTile getCannon() {
        return cannon;
    }

    @Override
    public void syncToServer(boolean fire, boolean removeOwner) {
        NetworkHelper.sendToServer(new ServerBoundSyncCannonPacket(
                cannon.getYaw(), cannon.getPitch(), cannon.getPowerLevel(),
                fire, removeOwner, TileOrEntityTarget.of(this)));
    }

    @Override
    public void sendOpenGuiRequest() {
        NetworkHelper.sendToServer(new ServerBoundRequestOpenCannonGuiMessage(this));
    }

    @Override
    public Vec3 getCannonGlobalOffset() {
        float backOff = 7 / 16f;
        return new Vec3(0, 12 / 16f, backOff);
    }

    @Override
    public Vec3 getCannonGlobalPosition() {
        float yaw = getCannonGlobalYawOffset();
        Vec3 vv = getCannonGlobalOffset();
        vv = vv.yRot(Mth.DEG_TO_RAD * yaw);
        return this.position().add(vv);
    }

    @Override
    public float getCannonGlobalYawOffset() {
        return 180 - this.getYRot();
    }

    @Override
    public void updateClients() {
        NetworkHelper.sendToAllClientPlayersTrackingEntity(this,
                new ClientBoundUpdateCannonBoatPacket(TileOrEntityTarget.of(this),
                        cannon.saveWithoutMetadata(this.registryAccess())));
    }

    @Override
    public Restraint getPitchAndYawRestrains() {
        return new Restraint(50, 360 - 50, 0, 180);
    }

    @Override
    public void playFireEffects() {

    }

    @Override
    public void playIgniteEffects() {

    }

    @Override
    public void onInputUpdate(boolean b, boolean b1, boolean b2, boolean b3, boolean b4, boolean jump) {
        if(jump && level().isClientSide){
            CannonController.startControlling(this);
        }
    }

    @Override
    public boolean canManeuverFromGUI(Player player) {
        return this.getControllingPassenger() == player;
    }

    @Override
    public void tick() {
        super.tick();
        this.cannon.tick(this);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof Player player) {
            this.cannon.setPlayerWhoMayEdit(player.getUUID());
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.cannon.setPlayerWhoMayEdit(null);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        return super.getPassengerAttachmentPoint(entity, dimensions, partialTick)
                .add(0.125, 0, 0);
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public void ejectPassengers() {
        super.ejectPassengers();
    }
}
