package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class SoapBlock extends Block {
    public SoapBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public void animateTick(BlockState pState, Level level, BlockPos pos, Random random) {

    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_53683_) {
        return PushReaction.PUSH_ONLY;
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level level, BlockPos pos, int pId, int pParam) {
        if (pId == 0) {
            Random r = level.random;
            for (int i = 0; i < 2; i++) {
                level.addParticle(ModRegistry.SUDS_PARTICLE.get(), pos.getX() + r.nextFloat(), pos.getY() + 1.1, pos.getZ() + r.nextFloat(),
                        (0.5 - r.nextFloat()) * 0.13f, (r.nextFloat()) * 0.1f, (0.5 - r.nextFloat()) * 0.13f);
            }
            return true;
        }
        return super.triggerEvent(pState, level, pos, pId, pParam);
    }


    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        Random rand = entity.level.random;
        if ((!level.isClientSide || entity instanceof LocalPlayer) && !entity.isSteppingCarefully()) {
            //chance
            if (rand.nextFloat() < 0.14) {
                Vec3 m = entity.getDeltaMovement();
                m.subtract(0, m.y, 0);
                if (m.lengthSqr() > 0.0008) {
                    //intensity
                    m = m.normalize().scale(0.10 + rand.nextFloat() * 0.13F);
                    float min = 22;
                    float max = 95;
                    float angle = 20 + rand.nextFloat() * (max - min);
                    angle *= rand.nextBoolean() ? -1 : 1;
                    m = m.yRot((float) (angle * Math.PI / 180f));
                    entity.setDeltaMovement(entity.getDeltaMovement().add(m.x, 0.0F, m.z));
                    level.blockEvent(pos, state.getBlock(), 0, 0);
                }
            }
        }
        super.stepOn(level, pos, state, entity);
    }

/*
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        //TODO: add this functionality & soap
        if (!level.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {

            double d0 = Math.abs(entity.getX() - entity.xOld);
            double d1 = Math.abs(entity.getZ() - entity.zOld);
            if (d0 >= (double) 0.003F || d1 >= (double) 0.003F) {
                Random rand = entity.level.random;
                if (rand.nextFloat()  < 0.15 && !entity.isSteppingCarefully()) {
                    Vec3 m = entity.getDeltaMovement();
                    m = m.normalize().scale(0.12 + rand.nextFloat() * 0.15F);
                    float min = 22;
                    float max = 95;
                    float angle = 20 + rand.nextFloat()*(max-min);
                    angle *= rand.nextBoolean() ? -1 :1;
                    m = m.yRot((float) (angle*Math.PI/180f));
                    entity.setDeltaMovement(entity.getDeltaMovement().add(m.x, 0.0F, m.z));
                    level.blockEvent(pos, state.getBlock(), 0, 0);

                }
            }
        }
        super.stepOn(level, pos, state, entity);
    }
*/
}
