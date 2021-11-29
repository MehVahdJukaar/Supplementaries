package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
    public boolean triggerEvent(BlockState pState, Level level, BlockPos pos, int pId, int pParam) {
        if (pId == 0) {

            Random random = level.random;
            for (int i = 0; i < 2; i++) {
                level.addParticle(ModRegistry.SUDS_PARTICLE.get(), pos.getX() + random.nextFloat(), pos.getY() + 1, pos.getZ() + random.nextFloat(),
                        random.nextFloat() * 0.2f, random.nextFloat() * 0.2f, random.nextFloat() * 0.2f);
            }
            return true;
        }
        return super.triggerEvent(pState, level, pos, pId, pParam);
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState state, Entity entity) {
        Random rand = entity.level.random;
        if ((!pLevel.isClientSide || entity instanceof LocalPlayer) && !entity.isSteppingCarefully()) {
            if (rand.nextFloat() < 0.6) {
                var m = entity.getDeltaMovement();
                if (m.x * m.z > 0) {
                    Vec3 v = new Vec3(0, 0, 0.1 + rand.nextFloat() * 0.2F);
                    v = v.yRot((float) (rand.nextFloat() * (Math.PI * 2)));
                    entity.setDeltaMovement(entity.getDeltaMovement().add(v.x, 0.0F, v.z));
                    pLevel.blockEvent(pPos, state.getBlock(), 0, 0);
                }
            }
        }
    }

}
