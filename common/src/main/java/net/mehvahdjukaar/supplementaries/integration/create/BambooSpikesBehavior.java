package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BambooSpikesBehavior implements MovementBehaviour {

    public boolean isSameDir(MovementContext context) {
        return VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(BambooSpikesBlock.FACING));
    }

    @Override
    public boolean renderAsNormalBlockEntity() {
        return true;
    }


    //@Override
    //public void visitNewPosition(MovementContext context, BlockPos pos) {
    //    World world = context.world;
    //    BlockState stateVisited = world.getBlockState(pos);

    //     if (!stateVisited.isRedstoneConductor(world, pos))
    //        damageEntities(context, pos, world);
    //}

    @Override
    public void tick(MovementContext context) {
        damageEntities(context);
    }

    public void damageEntities(MovementContext context) {
        Level world = context.world;
        Vec3 pos = context.position;
        DamageSource damageSource = BambooSpikesBlock.getDamageSource(world);

        Entities:
        for (Entity entity : world.getEntitiesOfClass(Entity.class,
                new AABB(pos.add(-0.5, -0.5, -0.5), pos.add(0.5, 0.5, 0.5)))) {
            if (entity instanceof ItemEntity) continue;
            if (entity instanceof AbstractContraptionEntity) continue;
            if (entity instanceof Player player && player.isCreative()) continue;
            if (entity instanceof AbstractMinecart)
                for (Entity passenger : entity.getIndirectPassengers())
                    if (CreateCompat. isContraption(context, passenger))
                        continue Entities;
            //attack entities
            if (entity.isAlive() && entity instanceof LivingEntity livingEntity) {
                if (!world.isClientSide) {

                    double pow = 5 * Math.pow(context.relativeMotion.length(), 0.4) + 1;
                    float damage = !isSameDir(context) ? 1 :
                            (float) Mth.clamp(pow, 2, 6);
                    entity.hurt(damageSource, damage);
                    this.doTileStuff(context, world, livingEntity);
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


    private static final BambooSpikesBlockTile DUMMY = new BambooSpikesBlockTile(BlockPos.ZERO, ModRegistry.BAMBOO_SPIKES.get().defaultBlockState());

    private void doTileStuff(MovementContext context, @NotNull Level world, LivingEntity le) {
        CompoundTag com = context.blockEntityData;
        if (com == null) return;
        long lastTicked = com.getLong("LastTicked");
        if (!this.isOnCooldown(world, lastTicked)) {
            DUMMY.loadWithComponents(com, world.registryAccess());
            if (DUMMY.interactWithEntity(le, world)) {
               CreateCompat. changeState(context, context.state.setValue(BambooSpikesBlock.TIPPED, false));
            }
            com = DUMMY.saveWithFullMetadata(world.registryAccess());
            lastTicked = world.getGameTime();
            com.putLong("LastTicked", lastTicked);
            context.blockEntityData = com;
        }
    }


    public boolean isOnCooldown(Level world, long lastTicked) {
        return world.getGameTime() - lastTicked < 20;
    }

}