package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.entities.goals.BoatAwareMoveController;
import net.mehvahdjukaar.supplementaries.common.entities.goals.BoatAwarePathNavigation;
import net.mehvahdjukaar.supplementaries.common.entities.goals.ManeuverAndShootCannonGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class PlundererEntity extends AbstractIllager implements InventoryCarrier {
    private static final int INVENTORY_SIZE = 5;
    private static final int SLOT_OFFSET = 300;
    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

    public PlundererEntity(EntityType<? extends PlundererEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new BoatAwareMoveController(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new BoatAwarePathNavigation(this, level);
    }

    @Override
    public float getPathfindingMalus(PathType pathType) {
        return super.getPathfindingMalus(pathType);
    }

    @Override
    protected void registerGoals() {
        //TODO: add dismount boat goal
        // super.registerGoals();
       // this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(4, new ManeuverAndShootCannonGoal(this, 20, 40));

        //     this.goalSelector.addGoal(1, new MoveTowardsTargetGoal(this, 1, 20));
        //  this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0F));
        // this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
        //  this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {

        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
        return projectileWeapon == Items.CROSSBOW;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.writeInventoryToTag(compound, this.registryAccess());
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        return this.isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : IllagerArmPose.CROSSED;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readInventoryFromTag(compound, this.registryAccess());
        this.setCanPickUpLoot(true);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = level.getRandom();
        this.populateDefaultEquipmentSlots(randomSource, difficulty);
        this.populateDefaultEquipmentEnchantments(level, randomSource, difficulty);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    @Override
    protected void enchantSpawnedWeapon(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        super.enchantSpawnedWeapon(level, random, difficulty);
        if (random.nextInt(300) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(Items.GOLDEN_SWORD)) {
                //TODO: proper ench provider
                EnchantmentHelper.enchantItemFromProvider(itemStack, level.registryAccess(),
                        VanillaEnchantmentProviders.RAID_VINDICATOR_POST_WAVE_5, difficulty, random);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.getItem() instanceof BannerItem) {
            super.pickUpItem(itemEntity);
        } else if (this.wantsItem(itemStack)) {
            this.onItemPickup(itemEntity);
            ItemStack itemStack2 = this.inventory.addItem(itemStack);
            if (itemStack2.isEmpty()) {
                itemEntity.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    private boolean wantsItem(ItemStack item) {
        return this.hasActiveRaid() && item.is(Items.WHITE_BANNER);
    }

    @Override
    public SlotAccess getSlot(int slot) {
        int i = slot - SLOT_OFFSET;
        return i >= 0 && i < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, i) : super.getSlot(slot);
    }

    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean unused) {
        Raid raid = this.getCurrentRaid();
        boolean shouldRun = this.random.nextFloat() <= raid.getEnchantOdds();
        if (shouldRun) {
            ItemStack itemStack = new ItemStack(Items.CROSSBOW);
            ResourceKey<EnchantmentProvider> resourceKey;
            if (wave > raid.getNumGroups(Difficulty.NORMAL)) {
                resourceKey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_5;
            } else if (wave > raid.getNumGroups(Difficulty.EASY)) {
                resourceKey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_3;
            } else {
                resourceKey = null;
            }

            if (resourceKey != null) {
                EnchantmentHelper.enchantItemFromProvider(
                        itemStack, level.registryAccess(), resourceKey, level.getCurrentDifficultyAt(this.blockPosition()), this.getRandom()
                );
                this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
            }
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }
}
