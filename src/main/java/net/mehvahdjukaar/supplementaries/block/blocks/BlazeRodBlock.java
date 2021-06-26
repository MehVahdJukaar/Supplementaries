package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class BlazeRodBlock extends StickBlock{

    public BlazeRodBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(AXIS_Y,true).setValue(AXIS_X,false).setValue(AXIS_Z,false));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        Item item = context.getItemInHand().getItem();
        if(item == Items.BLAZE_ROD || item == this.asItem()){
            BooleanProperty axis = this.axisToProperty.get(context.getClickedFace().getAxis());
            if(!state.getValue(axis))return true;
        }
        return state.getMaterial().isReplaceable() && (context.getItemInHand().isEmpty() || context.getItemInHand().getItem() != this.asItem());
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 0;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(Items.BLAZE_ROD);
    }

    @Override
    public void stepOn(World world, BlockPos pos, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
            if(!(entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()))
                entity.setSecondsOnFire(2);
        }
        super.stepOn(world, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if(random.nextFloat()>0.3)return;
        ArrayList<Integer> list =  new ArrayList<>();
        if(state.getValue(AXIS_Y))list.add(0);
        if(state.getValue(AXIS_X))list.add(1);
        if(state.getValue(AXIS_Z))list.add(2);
        int s = list.size();
        if(s>0){
            IParticleData particle = state.getValue(WATERLOGGED)?ParticleTypes.BUBBLE:ParticleTypes.SMOKE;
            int c = list.get(random.nextInt(s));
            double x,y,z;
            switch (c){
                default:
                case 0:
                    x = (double) pos.getX() + 0.5D-0.125 + random.nextFloat()*0.25;
                    y = (double) pos.getY() + random.nextFloat();
                    z = (double) pos.getZ() + 0.5D-0.125 + random.nextFloat()*0.25;
                    break;
                case 1:
                    y = (double) pos.getY() + 0.5D-0.125 + random.nextFloat()*0.25;
                    x = (double) pos.getX() + random.nextFloat();
                    z = (double) pos.getZ() + 0.5D-0.125 + random.nextFloat()*0.25;
                    break;
                case 2:
                    y = (double) pos.getY() + 0.5D-0.125 + random.nextFloat()*0.25;
                    z = (double) pos.getZ() + random.nextFloat();
                    x = (double) pos.getX() + 0.5D-0.125 + random.nextFloat()*0.25;
                    break;
            }
            world.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
