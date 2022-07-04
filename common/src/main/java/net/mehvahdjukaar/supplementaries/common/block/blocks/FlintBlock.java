package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FlintBlock extends Block {
    public FlintBlock(Properties properties) {
        super(properties);
    }


    //TODO: figure out piston fire interaction

    public void onMagnetMoved(Level world, BlockPos blockPos, Direction direction, BlockState blockState, BlockEntity tileEntity) {
        int a = 1;
    }

/*
    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {

            double d0 = Math.abs(entity.getX() - entity.xOld);
            double d1 = Math.abs(entity.getZ() - entity.zOld);
            if (d0 >= (double) 0.003F || d1 >= (double) 0.003F) {
                if (world.random.nextInt(4) == 0 && !entity.isSteppingCarefully()) {
                    if (entity instanceof LivingEntity le && le.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                        entity.hurt(DamageSource.GENERIC, 1.0F);
                    }
                }
            }
        }
        super.stepOn(world, pos, state, entity);
    }

 */
}