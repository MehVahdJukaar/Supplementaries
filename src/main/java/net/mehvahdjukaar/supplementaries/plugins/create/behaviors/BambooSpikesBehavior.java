package net.mehvahdjukaar.supplementaries.plugins.create.behaviors;


import com.simibubi.create.content.contraptions.components.actors.SawMovementBehaviour;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.security.auth.login.AccountLockedException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class BambooSpikesBehavior extends MovementBehaviour {

    @Override
    public boolean hasSpecialMovementRenderer() {
        return false;
    }

    public boolean isSameDir(MovementContext context) {
        return VecHelper.isVecPointingTowards(context.relativeMotion, context.state.get(BambooSpikesBlock.FACING));
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        World world = context.world;
        BlockState stateVisited = world.getBlockState(pos);

        if (!stateVisited.isNormalCube(world, pos))
            damageEntities(context, pos, world);
    }

    public void damageEntities(MovementContext context, BlockPos pos, World world) {
        DamageSource damageSource = getDamageSource();

        Entities: for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof ItemEntity) continue;
            if (entity instanceof AbstractContraptionEntity) continue;
            if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())continue;
            if (entity instanceof AbstractMinecartEntity)
                for (Entity passenger : entity.getRecursivePassengers())
                    if (passenger instanceof AbstractContraptionEntity
                            && ((AbstractContraptionEntity) passenger).getContraption() == context.contraption)
                        continue Entities;
            //attack entities
            if (entity.isAlive() && entity instanceof LivingEntity) {
                if(!world.isRemote) {

                    double pow = 4 * Math.pow(context.relativeMotion.length(), 0.4) + 1;
                    float damage = !isSameDir(context)? 1 :
                            (float) MathHelper.clamp(pow, 2, 6);
                    entity.attackEntityFrom(damageSource, damage);
                    this.doTileStuff(context, world, (LivingEntity) entity);
                }


            }
            //throw entities
            if (world.isRemote == (entity instanceof PlayerEntity)) {
                Vector3d motionBoost = context.motion.add(0, context.motion.length() / 4f, 0);
                int maxBoost = 4;
                if (motionBoost.length() > maxBoost) {
                    motionBoost = motionBoost.subtract(motionBoost.normalize().scale(motionBoost.length() - maxBoost));
                }
                entity.setMotion(entity.getMotion().add(motionBoost));
                entity.velocityChanged = true;
            }
        }
    }


    private void doTileStuff(MovementContext context, @Nonnull World world, LivingEntity le){
        CompoundNBT com = context.tileData;
        int charges = com.getInt("Charges");
        long lastTicked = com.getLong("LastTicked");
        Potion potion = PotionUtils.getPotionTypeFromNBT(com);
        if(potion!=Potions.EMPTY && charges >0 && !this.isOnCooldown(world,lastTicked)) {
            boolean used = false;
            for(EffectInstance effect : potion.getEffects()){
                if(!le.isPotionApplicable(effect))continue;
                if(le.isPotionActive(effect.getPotion()))continue;

                if (effect.getPotion().isInstant()) {
                    float health = 0.5f;//no idea of what this does. it's either 0.5 or 1
                    effect.getPotion().affectEntity(null, null, le, effect.getAmplifier(), health);
                } else {
                    le.addPotionEffect(new EffectInstance(effect.getPotion(),
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
                    MovementUtils.changeState(context, context.state.with(BambooSpikesBlock.TIPPED,false));
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

}