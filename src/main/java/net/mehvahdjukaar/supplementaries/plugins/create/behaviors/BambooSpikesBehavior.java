package net.mehvahdjukaar.supplementaries.plugins.create.behaviors;



/*
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;

public class BambooSpikesBehavior extends MovementBehaviour {

    @Override
    public boolean hasSpecialMovementRenderer() {
        return false;
    }

    public boolean isSameDir(MovementContext context) {
        return VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(BambooSpikesBlock.FACING));
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        World world = context.world;
        BlockState stateVisited = world.getBlockState(pos);

        if (!stateVisited.isRedstoneConductor(world, pos))
            damageEntities(context, pos, world);
    }

    public void damageEntities(MovementContext context, BlockPos pos, World world) {
        DamageSource damageSource = getDamageSource();

        Entities: for (Entity entity : world.getEntitiesOfClass(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof ItemEntity) continue;
            if (entity instanceof AbstractContraptionEntity) continue;
            if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())continue;
            if (entity instanceof AbstractMinecartEntity)
                for (Entity passenger : entity.getIndirectPassengers())
                    if (passenger instanceof AbstractContraptionEntity
                            && ((AbstractContraptionEntity) passenger).getContraption() == context.contraption)
                        continue Entities;
            //attack entities
            if (entity.isAlive() && entity instanceof LivingEntity) {
                if(!world.isClientSide) {

                    double pow = 4 * Math.pow(context.relativeMotion.length(), 0.4) + 1;
                    float damage = !isSameDir(context)? 1 :
                            (float) MathHelper.clamp(pow, 2, 6);
                    entity.hurt(damageSource, damage);
                    this.doTileStuff(context, world, (LivingEntity) entity);
                }


            }
            //throw entities
            if (world.isClientSide == (entity instanceof PlayerEntity)) {
                Vector3d motionBoost = context.motion.add(0, context.motion.length() / 4f, 0);
                int maxBoost = 4;
                if (motionBoost.length() > maxBoost) {
                    motionBoost = motionBoost.subtract(motionBoost.normalize().scale(motionBoost.length() - maxBoost));
                }
                entity.setDeltaMovement(entity.getDeltaMovement().add(motionBoost));
                entity.hurtMarked = true;
            }
        }
    }


    private void doTileStuff(MovementContext context, @Nonnull World world, LivingEntity le){
        CompoundNBT com = context.tileData;
        int charges = com.getInt("Charges");
        long lastTicked = com.getLong("LastTicked");
        Potion potion = PotionUtils.getPotion(com);
        if(potion!=Potions.EMPTY && charges >0 && !this.isOnCooldown(world,lastTicked)) {
            boolean used = false;
            for(EffectInstance effect : potion.getEffects()){
                if(!le.canBeAffected(effect))continue;
                if(le.hasEffect(effect.getEffect()))continue;

                if (effect.getEffect().isInstantenous()) {
                    float health = 0.5f;//no idea of what this does. it's either 0.5 or 1
                    effect.getEffect().applyInstantenousEffect(null, null, le, effect.getAmplifier(), health);
                } else {
                    le.addEffect(new EffectInstance(effect.getEffect(),
                            (int) (effect.getDuration() * BambooSpikesBlockTile.POTION_MULTIPLIER),
                            effect.getAmplifier()));
                }
                used=true;
            }
            if(used){
                lastTicked = world.getGameTime();
                charges-=1;
                if(charges<=0){
                    charges=0;
                    potion=Potions.EMPTY;
                    MovementUtils.changeState(context, context.state.setValue(BambooSpikesBlock.TIPPED,false));
                }
                com.remove("Charges");
                com.remove("LastTicked");
                com.remove("Potion");
                com.putInt("Charges",charges);
                com.putLong("LastTicked",lastTicked);
                com.putString("Potion", Registry.POTION.getKey(potion).toString());
                context.tileData = com;
            }
        }
    }


    public boolean isOnCooldown(World world,long lastTicked){
        return world.getGameTime()-lastTicked<20;
    }

    protected DamageSource getDamageSource() {
        return BambooSpikesBlock.SPIKE_DAMAGE;
    }

}*/