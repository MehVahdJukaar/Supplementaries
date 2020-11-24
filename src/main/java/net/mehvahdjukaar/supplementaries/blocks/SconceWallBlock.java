package net.mehvahdjukaar.supplementaries.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Random;

public class SconceWallBlock extends WallTorchBlock implements IWaterLoggable {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    //TODO: make map for other blocks
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.makeCuboidShape(6D, 2.0D, 10D, 10D, 13.0D, 16.0D),
            Direction.SOUTH, Block.makeCuboidShape(6D, 2.0D, 0.0D, 10D, 13.0D, 6D),
            Direction.WEST, Block.makeCuboidShape(10D, 2.0D, 6D, 16.0D, 13.0D, 10D),
            Direction.EAST, Block.makeCuboidShape(0.0D, 2.0D, 6D, 6D, 13.0D, 10D)));

    public SconceWallBlock(Properties properties, IParticleData particleData) {
        super(properties, particleData);
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(HORIZONTAL_FACING, Direction.NORTH).with(WATERLOGGED, false).with(LIT, true));
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if(!worldIn.isRemote && entityIn instanceof  ProjectileEntity) {
            ProjectileEntity projectile = (ProjectileEntity)entityIn;
            if (projectile.isBurning()) {
                Entity entity = projectile.func_234616_v_();
                boolean flag = entity == null || entity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity);
                if (flag && !state.get(LIT) && !state.get(WATERLOGGED)) {
                    worldIn.setBlockState(pos, state.with(BlockStateProperties.LIT, true), 11);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5F, 1.4F);
                }
            }
            else if (projectile instanceof PotionEntity && PotionUtils.getPotionFromItem(((PotionEntity)projectile).getItem()).equals(Potions.WATER)) {
                Entity entity = projectile.func_234616_v_();
                boolean flag = entity == null || entity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity);
                if (flag && state.get(LIT)) {
                    worldIn.setBlockState(pos, state.with(BlockStateProperties.LIT, false), 11);
                    extinguish(worldIn, pos);
                }
            }
        }
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        if (!state.get(BlockStateProperties.WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
            boolean flag = state.get(LIT);
            if (flag) {
                extinguish(worldIn, pos);
            }
            worldIn.setBlockState(pos, state.with(WATERLOGGED, true).with(LIT, false), 3);
            worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
            return true;
        } else {
            return false;
        }
    }

    public static void extinguish(IWorld worldIn, BlockPos pos) {
        if (!worldIn.isRemote()) {
            worldIn.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
        }
        else{
            Random random = worldIn.getRandom();
            for (int i = 0; i < 10; ++i) {
                worldIn.addParticle(ParticleTypes.SMOKE,pos.getX()+0.25f+random.nextFloat()*0.5f,pos.getY()+0.5f+random.nextFloat()*0.5f,pos.getZ()+0.25f+random.nextFloat()*0.5f,0, 0.005, 0);
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(!state.get(LIT) && !state.get(WATERLOGGED) && player.abilities.allowEdit) {
            ItemStack item = player.getHeldItem(handIn);
            if (item.getItem() instanceof FlintAndSteelItem) {
                if (!worldIn.isRemote) {
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, worldIn.getRandom().nextFloat() * 0.4F + 0.8F);
                    worldIn.setBlockState(pos, state.with(LIT, true), 3);
                }
                item.damageItem(1, player, (playerIn) -> playerIn.sendBreakAnimation(handIn));
                return ActionResultType.SUCCESS;
            }
            else if(item.getItem() instanceof FireChargeItem) {
                if (!worldIn.isRemote) {
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (worldIn.getRandom().nextFloat() - worldIn.getRandom().nextFloat()) * 0.2F + 1.0F);
                    worldIn.setBlockState(pos, state.with(LIT, true), 3);
                }
                if(!player.isCreative())item.shrink(1);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
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
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(stateIn.get(LIT)){
            Direction direction = stateIn.get(HORIZONTAL_FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.7D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.25D * (double) direction1.getXOffset(), d1 + 0.15D, d2 + 0.25D * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData, d0 + 0.25D * (double) direction1.getXOffset(), d1 + 0.15D, d2 + 0.25D * (double) direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(HORIZONTAL_FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.with(WATERLOGGED, flag).with(LIT,!flag) : state;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIT,HORIZONTAL_FACING,WATERLOGGED);
    }

}
