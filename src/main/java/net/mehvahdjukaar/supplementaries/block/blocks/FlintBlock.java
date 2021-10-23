package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class FlintBlock extends Block {
    public FlintBlock(Properties properties) {
        super(properties);
    }

    /*
    @Override
    public void stepOn(Level world, BlockPos pos, Entity entity) {

        //TODO: add this functionality

        if (!world.isClientSide && (entity.getDeltaMovement().length() > 0.003F)) {
            if(world.random.nextInt(2) == 0) {
                //double d0 = Math.abs(entity.getX() - entity.xOld);
                //double d1 = Math.abs(entity.getZ() - entity.zOld);
                //if (d0 >= (double) 0.003F || d1 >= (double) 0.003F) {
                    if(entity instanceof LivingEntity && ((LivingEntity) entity).getItemBySlot(EquipmentSlotType.FEET).isEmpty()) {
                        entity.hurt(DamageSource.GENERIC, 1.0F);
                    }
                //}
            }
        }

        super.stepOn(world, pos, entity);
    }
*/
    //TODO: figure out piston fire interaction

    public void onMagnetMoved(Level world, BlockPos blockPos, Direction direction, BlockState blockState, BlockEntity tileEntity) {
        int a = 1;
    }

}
