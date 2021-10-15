package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BlazeRodBlock extends StickBlock{

    public BlazeRodBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(AXIS_Y,true).setValue(AXIS_X,false).setValue(AXIS_Z,false));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        Item item = context.getItemInHand().getItem();
        if(item == Items.BLAZE_ROD || item == this.asItem()){
            BooleanProperty axis = this.AXIS2PROPERTY.get(context.getClickedFace().getAxis());
            if(!state.getValue(axis))return true;
        }
        return state.getMaterial().isReplaceable() && (context.getItemInHand().isEmpty() || context.getItemInHand().getItem() != this.asItem());
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 0;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return new ItemStack(Items.BLAZE_ROD);
    }

    @Override
    public void stepOn(Level world, BlockPos pos, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
            if(!(entity instanceof Player && ((Player) entity).isCreative()))
                entity.setSecondsOnFire(2);
        }
        super.stepOn(world, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
        if(random.nextFloat()>0.3)return;
        ArrayList<Integer> list =  new ArrayList<>();
        if(state.getValue(AXIS_Y))list.add(0);
        if(state.getValue(AXIS_X))list.add(1);
        if(state.getValue(AXIS_Z))list.add(2);
        int s = list.size();
        if(s>0){
            ParticleOptions particle = state.getValue(WATERLOGGED)?ParticleTypes.BUBBLE:ParticleTypes.SMOKE;
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

    @Override
    public InteractionResult use(BlockState p_225533_1_, Level p_225533_2_, BlockPos p_225533_3_, Player p_225533_4_, InteractionHand p_225533_5_, BlockHitResult p_225533_6_) {
        return InteractionResult.PASS;
    }
}
