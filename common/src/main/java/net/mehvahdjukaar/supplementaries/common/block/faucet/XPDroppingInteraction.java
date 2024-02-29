package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

class XPDroppingInteraction implements FaucetTarget.BlState {

    private void dropXP(Level level, BlockPos pos) {
        int i = 3 + level.random.nextInt(5) + level.random.nextInt(5);
        while (i > 0) {
            int xp = ExperienceOrb.getExperienceValue(i);
            i -= xp;
            ExperienceOrb orb = new ExperienceOrb(level, pos.getX() + 0.5, pos.getY() - 0.125f, pos.getZ() + 0.5, xp);
            orb.setDeltaMovement(new Vec3(0, 0, 0));
            level.addFreshEntity(orb);
        }
        float f = (level.random.nextFloat() - 0.5f) / 4f;
        level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, SoftFluidStack fluid) {
        if (state.isAir()) {
            if (fluid.is(BuiltInSoftFluids.XP.get())) {
                this.dropXP(level, pos);
                return 1;
            }
        }
        return null;
    }
}

