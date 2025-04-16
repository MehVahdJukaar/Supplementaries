package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class BambooSpikesBlockTile extends BlockEntity {

    //put these two in config
    public static final float POTION_MULTIPLIER = 0.1f;
    public static final int MAX_CHARGES = 16;

    @NotNull
    protected PotionContents potion = PotionContents.EMPTY;
    protected int charges = 0;
    protected long lastTicked = 0;

    public BambooSpikesBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BAMBOO_SPIKES_TILE.get(), pos, state);
    }

    public int getColor() {
        if (this.hasPotion())
            return this.potion.getColor();
        return 0xffffff;
    }

    public boolean hasPotion() {
        return this.charges != 0 && this.potion != PotionContents.EMPTY;
    }

    public boolean isOnCooldown(Level world) {
        return world.getGameTime() - this.lastTicked < 20;
    }

    //true if it has run out of charges
    public boolean consumeCharge(Level world) {
        if (CommonConfigs.Functional.ONLY_ALLOW_HARMFUL_INFINITE.get()) {
            for (var e : this.potion.getAllEffects()) {
                if (!e.getEffect().value().isBeneficial()) return false;
            }
        }

        this.lastTicked = world.getGameTime();
        this.charges -= 1;
        this.setChanged();
        if (this.charges <= 0) {
            this.charges = 0;
            this.potion = PotionContents.EMPTY;
            return true;
        }
        return false;
    }

    public boolean tryApplyPotion(PotionContents potion) {
        return tryApplyPotion(potion, MAX_CHARGES);
    }

    public boolean tryApplyPotion(PotionContents newPotion, int charges) {
        if (!this.hasPotion() || this.potion.equals(newPotion) && this.charges != MAX_CHARGES) {
            if (BambooSpikesTippedItem.isPotionValid(newPotion) && charges > 0) {
                this.potion = newPotion;
                this.charges = MAX_CHARGES;
                this.setChanged();
                //needed for buggy white tipped state. aparently not enough
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

                this.charges = Math.clamp(charges, 0, MAX_CHARGES);

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
            for (MobEffectInstance effectInstance : this.potion.getAllEffects()) {
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
        int color = this.getColor();
        BlockPos pos = this.getBlockPos();
        level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color), pos.getX() + 0.5 + (level.random.nextFloat() - 0.5) * 0.75,
                pos.getY() + 0.5 + (level.random.nextFloat() - 0.5) * 0.75,
                pos.getZ() + 0.5 + (level.random.nextFloat() - 0.5) * 0.75, 0, 0, 0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Charges", this.charges);
        tag.putLong("LastTicked", this.lastTicked);

        if (this.hasPotion()) {
            var ops = registries.createSerializationContext(NbtOps.INSTANCE);
            tag.put("Potion", PotionContents.CODEC.encodeStart(ops, this.potion).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.charges = tag.getInt("Charges");
        this.lastTicked = tag.getLong("LastTicked");
        if (tag.contains("Potion")) {
            var ops = registries.createSerializationContext(NbtOps.INSTANCE);
            PotionContents.CODEC.decode(ops, tag.get("Potion")).ifSuccess(
                    p -> this.potion = p.getFirst()
            );
        } else this.potion = PotionContents.EMPTY;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        var potion = componentInput.get(DataComponents.POTION_CONTENTS);
        if (potion != null) {
            this.potion = potion;
            this.charges = Mth.clamp(componentInput.getOrDefault(ModComponents.CHARGES.get(), MAX_CHARGES), 0, MAX_CHARGES);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.hasPotion()) {
            components.set(DataComponents.POTION_CONTENTS, this.potion);
            components.set(ModComponents.CHARGES.get(), Mth.clamp(this.charges, 0, MAX_CHARGES));
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Potion");
        tag.remove("Charges");
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
