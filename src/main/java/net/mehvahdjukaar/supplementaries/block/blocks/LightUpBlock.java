package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Random;

public abstract class LightUpBlock extends Block implements IWaterLoggable {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public LightUpBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED,false).with(LIT,true));
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        if (!state.get(BlockStateProperties.WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
            boolean flag = state.get(LIT);
            if (flag) {
                extinguish(state, pos, worldIn);
            }
            worldIn.setBlockState(pos, state.with(WATERLOGGED, true).with(LIT, false), 3);
            worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isReplaceable(BlockState state, Fluid fluid) {
        return this.material.isReplaceable();
    }

    public enum FireSound{
        FLINT_AND_STEEL,
        FIRE_CHANGE,
        FLAMING_ARROW;
        public void playSound(IWorld world, BlockPos pos){
            switch(this){
                case FIRE_CHANGE:
                    world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F + 1.0F);
                    break;
                case FLAMING_ARROW:
                    world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5F, 1.4F);
                    break;
                case FLINT_AND_STEEL:
                    world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                    break;
            }
        }
    }

    public void onChange(BlockState state, IWorld world, BlockPos pos) {}

    public static boolean lightUp(BlockState state, BlockPos pos, IWorld world, FireSound sound){
        if (!state.get(LIT) && !state.get(WATERLOGGED)) {
            if(!world.isRemote()) {
                world.setBlockState(pos, state.with(LIT, true), 11);
                sound.playSound(world, pos);
            }
            return true;
        }
        return false;
    }

    public static void extinguish(BlockState state, BlockPos pos, IWorld world) {
        if (!world.isRemote()) {
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
            world.setBlockState(pos, state.with(BlockStateProperties.LIT, false), 11);
        }
        else{
            Random random = world.getRandom();
            for (int i = 0; i < 10; ++i) {
                world.addParticle(ParticleTypes.SMOKE,pos.getX()+0.25f+random.nextFloat()*0.5f,pos.getY()+0.35f+random.nextFloat()*0.5f,pos.getZ()+0.25f+random.nextFloat()*0.5f,0, 0.005, 0);
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!state.get(LIT) && !state.get(WATERLOGGED) && player.abilities.allowEdit) {
            ItemStack item = player.getHeldItem(handIn);
            if (item.getItem() instanceof FlintAndSteelItem) {
                if(lightUp(state,pos,worldIn,FireSound.FLINT_AND_STEEL)) {
                    this.onChange(state,worldIn,pos);
                    item.damageItem(1, player, (playerIn) -> playerIn.sendBreakAnimation(handIn));
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }
            else if(item.getItem() instanceof FireChargeItem) {
                if(lightUp(state,pos,worldIn,FireSound.FIRE_CHANGE)) {
                    this.onChange(state,worldIn,pos);
                    item.damageItem(1, player, (playerIn) -> playerIn.sendBreakAnimation(handIn));
                    if(!player.isCreative())item.shrink(1);
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }
        }
        return ActionResultType.PASS;
    }


    @SuppressWarnings({"StrongCast", "OverlyStrongTypeCast"})
    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if(entityIn instanceof ProjectileEntity) {
            ProjectileEntity projectile = (ProjectileEntity)entityIn;
            if (projectile.isBurning()) {
                Entity entity = projectile.func_234616_v_();
                if(entity == null || entity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity)){
                    if(lightUp(state, pos, worldIn,FireSound.FLAMING_ARROW))this.onChange(state,worldIn,pos);
                }
            }
            else if (projectile instanceof PotionEntity && PotionUtils.getPotionFromItem(((ProjectileItemEntity) projectile).getItem())==Potions.WATER) {
                Entity entity = projectile.func_234616_v_();
                boolean flag = entity == null || entity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity);
                if (flag && state.get(LIT)) {
                    extinguish(state, pos, worldIn);
                    this.onChange(state,worldIn,pos);
                }
            }
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return stateIn;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        BlockState state = this.getDefaultState();
        return state.with(WATERLOGGED, flag).with(LIT,!flag);
    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT,WATERLOGGED);
    }
}