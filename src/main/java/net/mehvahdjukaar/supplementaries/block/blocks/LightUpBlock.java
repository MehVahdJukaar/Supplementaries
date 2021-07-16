package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.util.Utils;
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
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Random;

public abstract class LightUpBlock extends Block implements IWaterLoggable {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public LightUpBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(LIT,true));
    }

    @Override
    public boolean placeLiquid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidStateIn.getType() == Fluids.WATER) {

            extinguish(state, pos, worldIn);

            worldIn.setBlock(pos, state.setValue(WATERLOGGED, true).setValue(LIT, false), 3);
            worldIn.getLiquidTicks().scheduleTick(pos, fluidStateIn.getType(), fluidStateIn.getType().getTickDelay(worldIn));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return this.material.isReplaceable();
    }

    public enum FireSound{
        FLINT_AND_STEEL,
        FIRE_CHANGE,
        FLAMING_ARROW;
        public void playSound(IWorld world, BlockPos pos){
            switch(this){
                case FIRE_CHANGE:
                    world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F + 1.0F);
                    break;
                case FLAMING_ARROW:
                    world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5F, 1.4F);
                    break;
                case FLINT_AND_STEEL:
                    world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                    break;
            }
        }
    }

    public void onChange(BlockState state, IWorld world, BlockPos pos) {}

    public static boolean lightUp(BlockState state, BlockPos pos, IWorld world, FireSound sound){
        if (!state.getValue(LIT) && !state.getValue(WATERLOGGED)) {
            if(!world.isClientSide()) {
                world.setBlock(pos, state.setValue(LIT, true), 11);
                sound.playSound(world, pos);
            }
            return true;
        }
        return false;
    }

    public static boolean extinguish(BlockState state, BlockPos pos, IWorld world) {
        if (state.getValue(LIT)) {
            if (!world.isClientSide()) {
                world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
                world.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 11);
            } else {
                Random random = world.getRandom();
                for (int i = 0; i < 10; ++i) {
                    world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25f + random.nextFloat() * 0.5f, pos.getY() + 0.35f + random.nextFloat() * 0.5f, pos.getZ() + 0.25f + random.nextFloat() * 0.5f, 0, 0.005, 0);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!state.getValue(LIT) && !state.getValue(WATERLOGGED) && player.abilities.mayBuild) {
            ItemStack stack = player.getItemInHand(handIn);
            Item item = stack.getItem();
            if (item instanceof FlintAndSteelItem) {
                if(lightUp(state,pos,worldIn,FireSound.FLINT_AND_STEEL)) {
                    this.onChange(state,worldIn,pos);
                    stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
            }
            else if(item instanceof FireChargeItem) {
                if(lightUp(state,pos,worldIn,FireSound.FIRE_CHANGE)) {
                    this.onChange(state,worldIn,pos);
                    stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    if(!player.isCreative())stack.shrink(1);
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
            }
            else if(item instanceof PotionItem && PotionUtils.getPotion(stack)==Potions.WATER){
                if(extinguish(state,pos,worldIn)) {
                    this.onChange(state,worldIn,pos);
                    Utils.swapItem(player,handIn,stack,new ItemStack(Items.GLASS_BOTTLE));
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
            }
        }
        return ActionResultType.PASS;
    }


    @SuppressWarnings({"StrongCast", "OverlyStrongTypeCast"})
    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if(entityIn instanceof ProjectileEntity) {
            ProjectileEntity projectile = (ProjectileEntity)entityIn;
            if (projectile.isOnFire()) {
                Entity entity = projectile.getOwner();
                if(entity == null || entity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity)){
                    if(lightUp(state, pos, worldIn,FireSound.FLAMING_ARROW))this.onChange(state,worldIn,pos);
                }
            }
            else if (projectile instanceof PotionEntity && PotionUtils.getPotion(((ProjectileItemEntity) projectile).getItem())==Potions.WATER) {
                Entity entity = projectile.getOwner();
                boolean flag = entity == null || entity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity);
                if (flag && extinguish(state, pos, worldIn)) {
                    this.onChange(state,worldIn,pos);
                }
            }
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return stateIn;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState();
        return state.setValue(WATERLOGGED, flag).setValue(LIT,!flag);
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT,WATERLOGGED);
    }
}