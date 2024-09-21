package net.mehvahdjukaar.supplementaries.common.block.blocks;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlazeRodBlock extends StickBlock {

    public BlazeRodBlock(Properties properties) {
        super(properties, 0);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(AXIS_Y, true).setValue(AXIS_X, false).setValue(AXIS_Z, false));
    }

    //TODO: path find type

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity le && !EnchantmentHelper.hasFrostWalker(le)) {
            if (!(entity instanceof Player p && p.isCreative()))
                entity.setSecondsOnFire(2);
        }
        super.stepOn(world, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (random.nextFloat() > 0.3) return;
        IntList list = new IntArrayList();
        if (state.getValue(AXIS_Y)) list.add(0);
        if (state.getValue(AXIS_X)) list.add(1);
        if (state.getValue(AXIS_Z)) list.add(2);
        int s = list.size();
        if (s > 0) {
            ParticleOptions particle = state.getValue(WATERLOGGED) ? ParticleTypes.BUBBLE : ParticleTypes.SMOKE;
            int c = list.getInt(random.nextInt(s));
            double x, y, z;
            switch (c) {
                case 1 -> {
                    y = pos.getY() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                    x = pos.getX() + random.nextFloat();
                    z = pos.getZ() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                }
                case 2 -> {
                    y = pos.getY() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                    z = pos.getZ() + random.nextFloat();
                    x = pos.getX() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                }
                default -> {
                    x = pos.getX() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                    y = pos.getY() + random.nextFloat();
                    z = pos.getZ() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                }
            }
            world.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
