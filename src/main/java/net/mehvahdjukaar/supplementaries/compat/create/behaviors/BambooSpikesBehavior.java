package net.mehvahdjukaar.supplementaries.compat.create.behaviors;


import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class BambooSpikesBehavior extends MovementBehaviour {

    public boolean isSameDir(MovementContext context) {
        return VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(BambooSpikesBlock.FACING));
    }

    @Override
    public boolean renderAsNormalTileEntity() {
        return true;
    }

    /*
    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        World world = context.world;
        BlockState stateVisited = world.getBlockState(pos);

        if (!stateVisited.isRedstoneConductor(world, pos))
            damageEntities(context, pos, world);
    }*/

    @Override
    public void tick(MovementContext context) {
        damageEntities(context);
    }

    public void damageEntities(MovementContext context) {
        Level world = context.world;
        Vec3 pos = context.position;
        DamageSource damageSource = getDamageSource();

        Entities:
        for (Entity entity : world.getEntitiesOfClass(Entity.class,
                new AABB(pos.add(-0.5, -0.5, -0.5), pos.add(0.5, 0.5, 0.5)))) {
            if (entity instanceof ItemEntity) continue;
            if (entity instanceof AbstractContraptionEntity) continue;
            if (entity instanceof Player player && player.isCreative()) continue;
            if (entity instanceof AbstractMinecart)
                for (Entity passenger : entity.getIndirectPassengers())
                    if (passenger instanceof AbstractContraptionEntity
                            && ((AbstractContraptionEntity) passenger).getContraption() == context.contraption)
                        continue Entities;
            //attack entities
            if (entity.isAlive() && entity instanceof LivingEntity) {
                if (!world.isClientSide) {

                    double pow = 5 * Math.pow(context.relativeMotion.length(), 0.4) + 1;
                    float damage = !isSameDir(context) ? 1 :
                            (float) Mth.clamp(pow, 2, 6);
                    entity.hurt(damageSource, damage);
                    this.doTileStuff(context, world, (LivingEntity) entity);
                }


            }
            //throw entities (i forgot why this is here. maybe its from creates saw)
            if (world.isClientSide == (entity instanceof Player)) {
                Vec3 motionBoost = context.motion.add(0, context.motion.length() / 4f, 0);
                int maxBoost = 4;
                if (motionBoost.length() > maxBoost) {
                    motionBoost = motionBoost.subtract(motionBoost.normalize().scale(motionBoost.length() - maxBoost));
                }
                entity.setDeltaMovement(entity.getDeltaMovement().add(motionBoost));
                entity.hurtMarked = true;
            }
        }
    }


    private void doTileStuff(MovementContext context, @Nonnull Level world, LivingEntity le) {
        CompoundTag com = context.tileData;
        int charges = com.getInt("Charges");
        long lastTicked = com.getLong("LastTicked");
        Potion potion = PotionUtils.getPotion(com);
        if (potion != Potions.EMPTY && charges > 0 && !this.isOnCooldown(world, lastTicked)) {
            boolean used = false;
            for (MobEffectInstance effect : potion.getEffects()) {
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
                lastTicked = world.getGameTime();
                charges -= 1;
                if (charges <= 0) {
                    charges = 0;
                    potion = Potions.EMPTY;
                    MovementUtils.changeState(context, context.state.setValue(BambooSpikesBlock.TIPPED, false));
                }
                com.remove("Charges");
                com.remove("LastTicked");
                com.remove("Potion");
                com.putInt("Charges", charges);
                com.putLong("LastTicked", lastTicked);
                com.putString("Potion", Registry.POTION.getKey(potion).toString());
                context.tileData = com;
            }
        }
    }


    public boolean isOnCooldown(Level world, long lastTicked) {
        return world.getGameTime() - lastTicked < 20;
    }

    protected DamageSource getDamageSource() {
        return CommonUtil.SPIKE_DAMAGE;
    }

}