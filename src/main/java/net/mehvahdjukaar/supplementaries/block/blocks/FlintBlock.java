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
        if (entity instanceof LivingEntity && ((LivingEntity) entity).getItemBySlot(EquipmentSlotType.FEET).isEmpty()) {
            entity.hurt(DamageSource.GENERIC, 0.5F);
        }

        super.stepOn(world, pos, entity);
    }

    //TODO: figure out piston fire interaction

    public void onMagnetMoved(World world, BlockPos blockPos, Direction direction, BlockState blockState, TileEntity tileEntity) {
        int a = 1;
    }

}
