package net.mehvahdjukaar.supplementaries.common.entities;//


import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
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

public class CannnonBoatEntity extends Boat implements HasCustomInventoryScreen, ContainerEntity, CannonAccess {
    private CannonBlockTile cannon;

    public CannnonBoatEntity(EntityType<CannnonBoatEntity> entityType, Level level) {
        super(entityType, level);
        this.cannon = new CannonBlockTile(BlockPos.ZERO, ModRegistry.CANNON.get()
                .defaultBlockState().setValue(CannonBlock.FACING, Direction.UP));
        this.cannon.setLevel(level);
    }

    public CannnonBoatEntity(Level level, double x, double y, double z) {
        super(ModEntities.CANNON_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;

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
        var cannonTag = this.cannon.saveWithoutMetadata(this.registryAccess());
        compound.put("Cannon", cannonTag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Cannon")) {
            var cannonTag = compound.getCompound("Cannon");
            this.cannon.loadWithComponents(cannonTag, this.registryAccess());
        }
    }

    @Override
    public void destroy(DamageSource source) {
        this.destroy(this.getDropItem());
        //   this.chestVehicleDestroyed(source, this.level(), this);
    }

    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            //  Containers.dropContents(this.level(), this, this);
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
    public Vec3 getCannonGlobalPosition() {
        Vec3 boatPos = this.position();
        float yaw = 180 - this.getYRot();
        float backOff = 9 / 16f;
        Vec3 vv = new Vec3(0, 1, backOff);
        vv = vv.yRot(Mth.DEG_TO_RAD * yaw);
        return boatPos.add(vv);
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
        return new Restraint(130, 260, -10, 180);
    }

    @Override
    public void playFireEffects() {

    }

    @Override
    public void playIgniteEffects() {

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
}
