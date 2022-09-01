package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Skeleton.class)
public abstract class SkeletonMixin extends AbstractSkeleton implements IQuiverEntity {
    //frick it going full mixin here. I could have used caps and spawn events...

    @Unique
    private ItemStack quiver = ItemStack.EMPTY;

    protected SkeletonMixin(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        if (random.nextFloat() < 0.15f * difficulty.getSpecialMultiplier()) {
            this.quiver = QuiverItem.createRandomQuiver(difficulty.getSpecialMultiplier());
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(damageSource, looting, hitByPlayer);
        if(this.quiver != null && hitByPlayer){
            ItemStack itemStack = this.quiver;
            float chance = 0.5f;
            if (Math.max(this.random.nextFloat() - (float)looting * 0.01F, 0.0F) < chance) {
                this.spawnAtLocation(itemStack);
                this.quiver = ItemStack.EMPTY;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (!this.quiver.isEmpty()) {
            compound.put("Quiver", quiver.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if(compound.contains("Quiver")){
            this.quiver = ItemStack.of(compound.getCompound("Quiver"));
        }
    }

    @Override
    public ItemStack getQuiver() {
        return quiver;
    }
}