package net.mehvahdjukaar.supplementaries.common.entities;//


import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.cannon.BoatReferenceFrame;
import net.mehvahdjukaar.supplementaries.common.block.cannon.EntityReferenceFrame;
import net.mehvahdjukaar.supplementaries.common.block.cannon.Restraint;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.CannonContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CannonBoatEntity extends Boat implements HasCustomInventoryScreen, ContainerEntity, IControllableVehicle {

    private static final EntityDataAccessor<WoodType> DATA_WOOD_TYPE =
            SynchedEntityData.defineId(CannonBoatEntity.class, WoodType.ENTITY_SERIALIZER.get());
    private static final EntityDataAccessor<ItemStack> BANNER_ITEM = SynchedEntityData.defineId(
            CannonBoatEntity.class, EntityDataSerializers.ITEM_STACK);

    private final CannonBlockTile cannon;
    private boolean isBamboo;

    public CannonBoatEntity(EntityType<CannonBoatEntity> entityType, Level level) {
        super(entityType, level);
        this.cannon = new CannonBlockTile(BlockPos.ZERO, ModRegistry.CANNON.get()
                .defaultBlockState().setValue(CannonBlock.FACING, Direction.UP));
        this.cannon.setReferenceFrame(new BoatReferenceFrame(this));
        this.cannon.setRestraint(new Restraint(50, 360 - 50, 0, 180));
        this.cannon.setLevel(level);
        this.cannon.setRenderYaw(0);
        this.setWoodType(VanillaWoodTypes.OAK);
    }

    public CannonBoatEntity(Level level, double x, double y, double z, WoodType type) {
        this(ModEntities.CANNON_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setWoodType(type);
    }

    public boolean isBamboo() {
        return isBamboo;
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WOOD_TYPE, VanillaWoodTypes.OAK);
        builder.define(BANNER_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getBannerItem() {
        return entityData.get(BANNER_ITEM);
    }

    public void setBannerItem(ItemStack stack) {
        this.entityData.set(BANNER_ITEM, stack);
    }

    public void setWoodType(WoodType type) {
        this.entityData.set(DATA_WOOD_TYPE, type);
        Type vanillaBoatOrOak = type.toVanillaBoatOrOak();
        this.isBamboo = vanillaBoatOrOak == Type.BAMBOO;
        this.setVariant(vanillaBoatOrOak);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if (dataAccessor == DATA_WOOD_TYPE) {
            this.isBamboo = VanillaWoodTypes.BAMBOO == this.entityData.get(dataAccessor);
        }
    }

    public WoodType getWoodType() {
        return entityData.get(DATA_WOOD_TYPE);
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15F + (0.125f);
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
        WoodType type = WoodTypeRegistry.INSTANCE.get(ResourceLocation.parse(woodTypeId));
        if (type != null) this.setWoodType(type);
        if (compound.contains("Cannon")) {
            var cannonTag = compound.getCompound("Cannon");
            this.cannon.loadWithComponents(cannonTag, this.registryAccess());
        }
    }

    @Override
    public void destroy(DamageSource source) {
        this.destroy(this.getDropItem());
        this.chestVehicleDestroyed(source, this.level(), this);
        ItemStack bannerItem = this.getBannerItem();
        if (!bannerItem.isEmpty()) this.spawnAtLocation(bannerItem);
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
        } else {
            var item = player.getItemInHand(hand);
            boolean hasNoBanner = this.getBannerItem().isEmpty();
            if (hasNoBanner && item.is(ItemTags.BANNERS)) {
                this.setBannerItem(item.copy());
                if (!player.getAbilities().instabuild) item.shrink(1);
                this.playSound(((BlockItem) item.getItem()).getBlock().defaultBlockState().getSoundType().getPlaceSound(), 1.0F, 1.2f);
                return InteractionResult.sidedSuccess(level().isClientSide);
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
        if (player instanceof ServerPlayer sp && cannon.canBeUsedBy(this.blockPosition(), player)) {
            PlatHelper.openCustomMenu(sp, this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public Item getDropItem() {
        return ModRegistry.CANNON_BOAT_ITEMS.get(this.getWoodType());
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
    public SlotAccess getSlot(int slot) {
        return this.getChestVehicleSlot(slot);
    }

    public void setChanged() {
        this.cannon.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isChestVehicleStillValid(player);
    }

   // @Override
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
        return new CannonContainerMenu(id, inv, cannon);
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

    public CannonBlockTile getInternalCannon() {
        return cannon;
    }


    @Override
    protected void clampRotation(Entity entityToUpdate) {
        //  if (entityToUpdate instanceof AbstractIllager) return;
        super.clampRotation(entityToUpdate);
    }

    @Override
    public void onInputUpdate(boolean b, boolean b1, boolean b2, boolean b3, boolean ctrl, boolean jump) {
        if (jump && level().isClientSide) {
            CannonController.startControlling(cannon);
        }
        if (ctrl && cannon.readyToFire()) {
            cannon.syncToServer(true, false);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.cannon.tick();

        Entity controlling = this.getControllingPassenger();
        if (controlling != null) {
            cannon.setCurrentUser(controlling.getUUID());
        }
    }



    public Vec3 getCannonOffset() {
        if (this.isBamboo()) {
            float backOff = 6 / 16f;
            return new Vec3(0, 16 / 16f, backOff);
        } else {
            float backOff = 7 / 16f;
            return new Vec3(0, 12 / 16f, backOff);
        }
    }
}
