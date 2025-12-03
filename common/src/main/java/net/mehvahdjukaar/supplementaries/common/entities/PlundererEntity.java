package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.entities.controllers.BoatMoveController;
import net.mehvahdjukaar.supplementaries.common.entities.controllers.BoatPathNavigation;
import net.mehvahdjukaar.supplementaries.common.entities.controllers.LookControlWithSpyglass;
import net.mehvahdjukaar.supplementaries.common.entities.goals.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
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
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class PlundererEntity extends AbstractIllager implements InventoryCarrier, ISpyglassMob {
    private static final int INVENTORY_SIZE = 5;
    private static final int SLOT_OFFSET = 300;
    protected static final EntityDataAccessor<Boolean> USING_SPYGLASS =
            SynchedEntityData.defineId(PlundererEntity.class, EntityDataSerializers.BOOLEAN);

    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

    private BoatPathNavigation boatNavigation;
    private PathNavigation defaultNavigation;
    private final BoatMoveController boatController;
    private final MoveControl defaultController;

    private BlockPos lastKnownCannonPos = null;

    public PlundererEntity(EntityType<? extends PlundererEntity> entityType, Level level) {
        super(entityType, level);
        this.boatController = new BoatMoveController(this);
        this.defaultController = moveControl;
        this.lookControl = new LookControlWithSpyglass<>(this);
    }


    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        //don't remove when has target that's a player
        if (this.getTarget() instanceof Player) {
            return false;
        }
        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    public void setLastKnownCannonPos(BlockPos lastKnownCannonPos) {
        this.lastKnownCannonPos = lastKnownCannonPos;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new UseCannonBoatGoal(this, 20, 40,
                16, 20 * 15)); //max 15 sec

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class)
                .setAlertOthers());

        this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0F));
        this.goalSelector.addGoal(2, new BoardBoatGoal(this, 1, 200));

        this.targetSelector.addGoal(2, new PlundererNearestAttackableTargetGoal<>(this, Player.class, true));

        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));


        this.goalSelector.addGoal(3, new AbandonShipGoal(this, 15));

        this.goalSelector.addGoal(3, new UseCannonBlockGoal(this, 1, 20));

        this.goalSelector.addGoal(4, new MeleeAttackGoalWhenInRange(this, 1.0, false));
        this.goalSelector.addGoal(6, new IAmTheCaptainGoal(this));
        //TODO: go to boat,leave boat, switch to captain, soot cannon
        //this.goalSelector.addGoal(1, new MoveTowardsTargetGoal(this, 1, 20));

        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new PlundererLookAtPlayerGoal(this, Player.class, 1.0F));
        this.goalSelector.addGoal(10, new PlundererLookAtPlayerGoal(this, Mob.class));
    }
    //got to do this since the accessors aren't used consistently...

    @Override
    protected void customServerAiStep() {
        if (this.getControlledVehicle() instanceof Boat) {
            this.moveControl = boatController;
            this.navigation = boatNavigation;
        } else {
            this.moveControl = defaultController;
            this.navigation = defaultNavigation;
        }
        super.customServerAiStep();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        this.boatNavigation = new BoatPathNavigation(this, level);
        this.defaultNavigation = super.createNavigation(level);
        return this.defaultNavigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(USING_SPYGLASS, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public boolean isUsingSpyglass() {
        return this.entityData.get(USING_SPYGLASS);
    }

    public void setUsingSpyglass(boolean using) {
        this.entityData.set(USING_SPYGLASS, using);
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isUsingSpyglass()) {
            return IllagerArmPose.NEUTRAL;
        }
        return this.isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : IllagerArmPose.CROSSED;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return super.checkSpawnObstruction(level);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return super.checkSpawnRules(level, reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        RegistryAccess ra = this.registryAccess();
        this.writeInventoryToTag(compound, ra);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        RegistryAccess ra = this.registryAccess();
        this.readInventoryFromTag(compound, ra);
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
                //TODO: confetti villagers
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
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
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


}
