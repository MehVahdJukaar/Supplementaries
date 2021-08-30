package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlintBlock extends Block {
    public FlintBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(World world, BlockPos pos, Entity entity) {


        if (!world.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
            if(world.random.nextInt(3) == 0) {
                double d0 = Math.abs(entity.getX() - entity.xOld);
                double d1 = Math.abs(entity.getZ() - entity.zOld);
                if (d0 >= (double) 0.003F || d1 >= (double) 0.003F) {
                    if(entity instanceof LivingEntity && ((LivingEntity) entity).getItemBySlot(EquipmentSlotType.FEET).isEmpty()) {
                        entity.hurt(DamageSource.GENERIC, 1.0F);
                    }
                }
            }
        }

        super.stepOn(world, pos, entity);
    }

    //TODO: figure out piston fire interaction

    public void onMagnetMoved(World world, BlockPos blockPos, Direction direction, BlockState blockState, TileEntity tileEntity) {
        int a = 1;
    }

}
