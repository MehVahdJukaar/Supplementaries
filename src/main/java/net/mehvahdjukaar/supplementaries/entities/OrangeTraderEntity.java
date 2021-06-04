package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.inventories.OrangeMerchantContainer;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SendOrangeTraderOffersPacket;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.OptionalInt;

public class OrangeTraderEntity extends AbstractVillagerEntity {
    @Nullable
    private BlockPos wanderTarget;
    private int despawnDelay;

    public OrangeTraderEntity(EntityType<? extends OrangeTraderEntity> type, World world) {
        super(type, world);
        this.forcedLoading = true;
    }
    public OrangeTraderEntity(World world) {
        this(Registry.ORANGE_TRADER.get(), world);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(0, new UseItemGoal<>(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, (p_213733_1_) -> this.level.isNight() && !p_213733_1_.isInvisible()));
        this.goalSelector.addGoal(0, new UseItemGoal<>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, (p_213736_1_) -> this.level.isDay() && p_213736_1_.isInvisible()));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZombieEntity.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, EvokerEntity.class, 12.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, VindicatorEntity.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, VexEntity.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, PillagerEntity.class, 15.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, IllusionerEntity.class, 12.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZoglinEntity.class, 10.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        this.goalSelector.addGoal(1, new LookAtCustomerGoal(this));
        this.goalSelector.addGoal(2, new OrangeTraderEntity.MoveToGoal(this, 2.0D, 0.35D));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 0.35D));
        this.goalSelector.addGoal(9, new LookAtWithoutMovingGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
    }

    @Nullable
    public AgeableEntity getBreedOffspring(ServerWorld world, AgeableEntity entity) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.getOffers().isEmpty()) {
                if (!this.level.isClientSide) {
                    this.setTradingPlayer(player);
                    this.openTradingScreen(player, this.getDisplayName(), 1);
                }

            }
            return ActionResultType.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(player, hand);
        }
    }

    public static final VillagerTrades.ITrade[] ORANGE_TRADER_TRADES =  new VillagerTrades.ITrade[]{
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Registry.COPPER_LANTERN.get(), 1),  16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Registry.ROPE_ITEM.get()), 16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Registry.BOMB_ITEM.get()), 16, 2, 0.05f)};


    @Override
    public void updateTrades() {
        MerchantOffers merchantoffers = this.getOffers();
        this.addOffersFromItemListings(merchantoffers, ORANGE_TRADER_TRADES, 5);
    }

    @Override
    public void openTradingScreen(PlayerEntity player, ITextComponent name, int level) {
        OptionalInt optionalint = player.openMenu(new SimpleNamedContainerProvider((i, p, m) -> new OrangeMerchantContainer(i, p, this), name));
        if (optionalint.isPresent() && player instanceof ServerPlayerEntity) {
            MerchantOffers merchantoffers = this.getOffers();
            if (!merchantoffers.isEmpty()) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new SendOrangeTraderOffersPacket(optionalint.getAsInt(), merchantoffers, level, this.getVillagerXp(), this.showProgressBar(), this.canRestock())
                );
            }
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundNBT p_213281_1_) {
        super.addAdditionalSaveData(p_213281_1_);
        p_213281_1_.putInt("DespawnDelay", this.despawnDelay);
        if (this.wanderTarget != null) {
            p_213281_1_.put("WanderTarget", NBTUtil.writeBlockPos(this.wanderTarget));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundNBT p_70037_1_) {
        super.readAdditionalSaveData(p_70037_1_);
        if (p_70037_1_.contains("DespawnDelay", 99)) {
            this.despawnDelay = p_70037_1_.getInt("DespawnDelay");
        }

        if (p_70037_1_.contains("WanderTarget")) {
            this.wanderTarget = NBTUtil.readBlockPos(p_70037_1_.getCompound("WanderTarget"));
        }

        this.setAge(Math.max(0, this.getAge()));
    }

    @Override
    public boolean removeWhenFarAway(double p_213397_1_) {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer p_213713_1_) {
        if (p_213713_1_.shouldRewardExp()) {
            int i = 3 + this.random.nextInt(4);
            this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }

    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    protected SoundEvent getDrinkingSound(ItemStack p_213351_1_) {
        Item item = p_213351_1_.getItem();
        return item == Items.MILK_BUCKET ? SoundEvents.WANDERING_TRADER_DRINK_MILK : SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean p_213721_1_) {
        return p_213721_1_ ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public void setDespawnDelay(int p_213728_1_) {
        this.despawnDelay = p_213728_1_;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.maybeDespawn();
        }

    }

    private void maybeDespawn() {
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
            this.remove();
        }

    }

    public void setWanderTarget(@Nullable BlockPos p_213726_1_) {
        this.wanderTarget = p_213726_1_;
    }

    @Nullable
    private BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    class MoveToGoal extends Goal {
        final OrangeTraderEntity trader;
        final double stopDistance;
        final double speedModifier;

        MoveToGoal(OrangeTraderEntity p_i50459_2_, double p_i50459_3_, double p_i50459_5_) {
            this.trader = p_i50459_2_;
            this.stopDistance = p_i50459_3_;
            this.speedModifier = p_i50459_5_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public void stop() {
            this.trader.setWanderTarget(null);
            OrangeTraderEntity.this.navigation.stop();
        }

        @Override
        public boolean canUse() {
            BlockPos blockpos = this.trader.getWanderTarget();
            return blockpos != null && this.isTooFarAway(blockpos, this.stopDistance);
        }

        @Override
        public void tick() {
            BlockPos blockpos = this.trader.getWanderTarget();
            if (blockpos != null && OrangeTraderEntity.this.navigation.isDone()) {
                if (this.isTooFarAway(blockpos, 10.0D)) {
                    Vector3d vector3d = (new Vector3d((double)blockpos.getX() - this.trader.getX(), (double)blockpos.getY() - this.trader.getY(), (double)blockpos.getZ() - this.trader.getZ())).normalize();
                    Vector3d vector3d1 = vector3d.scale(10.0D).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    OrangeTraderEntity.this.navigation.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, this.speedModifier);
                } else {
                    OrangeTraderEntity.this.navigation.moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), this.speedModifier);
                }
            }

        }

        private boolean isTooFarAway(BlockPos p_220846_1_, double p_220846_2_) {
            return !p_220846_1_.closerThan(this.trader.position(), p_220846_2_);
        }
    }
}
