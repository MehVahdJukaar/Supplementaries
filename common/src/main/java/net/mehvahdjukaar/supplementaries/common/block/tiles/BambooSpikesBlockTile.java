package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BambooSpikesBlockTile extends BlockEntity {
    //private List<EffectInstance> effects = new ArrayList<>();
    @Nullable
    public Holder<Potion> potion = null;
    public int charges = 0;
    public long lastTicked = 0;
    //put these two in config
    public static final float POTION_MULTIPLIER = 0.1f;
    public static final int MAX_CHARGES = 16;

    public BambooSpikesBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BAMBOO_SPIKES_TILE.get(), pos, state);
    }

    public int getColor() {
        if (this.hasPotion())
            return PotionContents.getColor(this.potion);
        return 0xffffff;
    }

    public boolean hasPotion() {
        return this.potion != null && this.charges != 0;
    }

    public boolean isOnCooldown(Level world) {
        return world.getGameTime() - this.lastTicked < 20;
    }

    //true if it has run out of charges
    public boolean consumeCharge(Level world) {
        if (CommonConfigs.Functional.ONLY_ALLOW_HARMFUL.get() && this.potion.value().getEffects().stream().anyMatch(
                e -> e.getEffect().value().isBeneficial()
        )) return false;
        this.lastTicked = world.getGameTime();
        this.charges -= 1;
        this.setChanged();
        if (this.charges <= 0) {
            this.charges = 0;
            this.potion = null;
            return true;
        }
        return false;
    }

    public void setMissingCharges(int missing) {
        this.charges = Math.max(MAX_CHARGES - missing, 0);
    }

    public boolean tryApplyPotion(Holder<Potion> newPotion) {

        if (this.charges == 0 || this.potion == null || this.potion.equals(newPotion) && this.charges != MAX_CHARGES) {
            if (BambooSpikesTippedItem.isPotionValid(newPotion)) {
                this.potion = newPotion;
                this.charges = MAX_CHARGES;
                this.setChanged();
                //needed for buggy white tipped state. aparently not enough
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                return true;
            }
        }
        return false;
    }

    //returns true if BlockState needs to be changed
    //will be called from moving block so it needs a world
    public boolean interactWithEntity(LivingEntity le, @NotNull Level world) {
        if (this.hasPotion() && !this.isOnCooldown(world)) {
            boolean used = false;
            for (MobEffectInstance effectInstance : this.potion.value().getEffects()) {
                if (!le.canBeAffected(effectInstance)) continue;
                if (le.hasEffect(effectInstance.getEffect())) continue;

                MobEffect effect = effectInstance.getEffect().value();
                if (effect.isInstantenous()) {
                    float health = 0.5f;//no idea of what this does. it's either 0.5 or 1
                    effect.applyInstantenousEffect(null, null, le, effectInstance.getAmplifier(), health);
                } else {
                    le.addEffect(new MobEffectInstance(effectInstance.getEffect(),
                            (int) (effectInstance.getDuration() * BambooSpikesBlockTile.POTION_MULTIPLIER),
                            effectInstance.getAmplifier()));
                }
                used = true;
            }
            if (used) {
                this.makeParticle(world);
                return this.consumeCharge(world);
            }
        }
        return false;
    }

    public void makeParticle(Level level) {
        int i = this.getColor();
        double d0 = (i >> 16 & 255) / 255.0D;
        double d1 = (i >> 8 & 255) / 255.0D;
        double d2 = (i & 255) / 255.0D;
        BlockPos pos = this.getBlockPos();
        level.addParticle(ParticleTypes.ENTITY_EFFECT, pos.getX() + 0.5 + (level.random.nextFloat() - 0.5) * 0.75,
                pos.getY() + 0.5 + (level.random.nextFloat() - 0.5) * 0.75,
                pos.getZ() + 0.5 + (level.random.nextFloat() - 0.5) * 0.75, d0, d1, d2);
    }

    public ItemStack getSpikeItem() {
        if (this.hasPotion()) {
            ItemStack stack = BambooSpikesTippedItem.makeSpikeItem(this.potion);
            stack.setDamageValue(stack.getMaxDamage() - this.charges);
            return stack;
        }
        return new ItemStack(ModRegistry.BAMBOO_SPIKES_ITEM.get());
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("Charges", this.charges);
        compound.putLong("LastTicked", this.lastTicked);

        ResourceLocation resourcelocation = BuiltInRegistries.POTION.getKey(this.potion);
        compound.putString("Potion", resourcelocation.toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.charges = tag.getInt("Charges");
        this.lastTicked = tag.getLong("LastTicked");
        this.potion = PotionUtils.getPotion(compound);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
