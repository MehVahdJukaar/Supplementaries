package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Random;


public class BambooSpikesBlockTile extends BlockEntity {
    //private List<EffectInstance> effects = new ArrayList<>();
    public Potion potion = Potions.EMPTY;
    public int charges = 0;
    public long lastTicked = 0;
    private final Random rand = new Random();
    //put these two in config
    public static final float POTION_MULTIPLIER = 0.1f;
    public static final int MAX_CHARGES = 16;

    public BambooSpikesBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BAMBOO_SPIKES_TILE.get(), pos, state);
    }

    public int getColor() {
        if (this.hasPotion())
            return PotionUtils.getColor(this.potion);
        return 0xffffff;
    }


    public boolean hasPotion() {
        return this.potion != Potions.EMPTY && this.charges != 0;
    }

    public boolean isOnCooldown(Level world) {
        return world.getGameTime() - this.lastTicked < 20;
    }

    //true if has run out of charges
    public boolean consumeCharge(Level world) {
        if (ServerConfigs.cached.BAMBOO_SPIKES_ALTERNATIVE && !this.potion.getEffects().get(0).getEffect().isBeneficial()) return false;
        this.lastTicked = world.getGameTime();
        this.charges -= 1;
        this.setChanged();
        if (this.charges <= 0) {
            this.charges = 0;
            this.potion = Potions.EMPTY;
            return true;
        }
        return false;
    }

    public void setMissingCharges(int missing) {
        this.charges = Math.max(MAX_CHARGES - missing, 0);
    }

    public boolean tryApplyPotion(Potion newPotion) {

        if (this.charges == 0 || this.potion == Potions.EMPTY || this.potion.equals(newPotion) && this.charges != MAX_CHARGES) {
            if(BambooSpikesTippedItem.areEffectsValid(newPotion.getEffects())) {
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
    public boolean interactWithEntity(LivingEntity le, @Nonnull Level world) {
        if (this.hasPotion() && !this.isOnCooldown(world)) {
            boolean used = false;
            for (MobEffectInstance effect : this.potion.getEffects()) {
                if (!le.canBeAffected(effect)) continue;
                if (le.hasEffect(effect.getEffect())) continue;

                if (effect.getEffect().isInstantenous()) {
                    float health = 0.5f;//no idea of what this does. it's either 0.5 or 1
                    effect.getEffect().applyInstantenousEffect(null, null, le, effect.getAmplifier(), health);
                } else {
                    le.addEffect(new MobEffectInstance(effect.getEffect(),
                            (int) (effect.getDuration() * BambooSpikesBlockTile.POTION_MULTIPLIER),
                            effect.getAmplifier()));
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
        double d0 = (double) (i >> 16 & 255) / 255.0D;
        double d1 = (double) (i >> 8 & 255) / 255.0D;
        double d2 = (double) (i & 255) / 255.0D;
        BlockPos pos = this.getBlockPos();
        //TODO: fix on server side
        level.addParticle(ParticleTypes.ENTITY_EFFECT, pos.getX() + 0.5 + (this.rand.nextFloat() - 0.5) * 0.75, pos.getY() + 0.5 + (this.rand.nextFloat() - 0.5) * 0.75, pos.getZ() + 0.5 + (this.rand.nextFloat() - 0.5) * 0.75, d0, d1, d2);
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

        ResourceLocation resourcelocation = net.minecraft.core.Registry.POTION.getKey(this.potion);
        compound.putString("Potion", resourcelocation.toString());
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.charges = compound.getInt("Charges");
        this.lastTicked = compound.getLong("LastTicked");
        this.potion = PotionUtils.getPotion(compound);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}
