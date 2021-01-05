package net.mehvahdjukaar.supplementaries.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.IWaterLoggable;
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
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class CandelabraBlock extends HorizontalFaceBlock implements IWaterLoggable {
    protected static final VoxelShape SHAPE_FLOOR = Block.makeCuboidShape(5D, 0D, 5D, 11D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_NORTH = Block.makeCuboidShape(5D, 0D, 10D, 11D, 15D, 16D);
    protected static final VoxelShape SHAPE_WALL_SOUTH = Block.makeCuboidShape(5D, 0D, 0D, 11D, 15D, 6D);
    protected static final VoxelShape SHAPE_WALL_WEST = Block.makeCuboidShape(10D, 0D, 5D, 16D, 15D, 11D);
    protected static final VoxelShape SHAPE_WALL_EAST = Block.makeCuboidShape(0D, 0D, 5D, 6D, 15D, 11D);
    protected static final VoxelShape SHAPE_CEILING = Block.makeCuboidShape(5D, 3D, 5D, 11D, 16D, 11D);

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public CandelabraBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
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

    public static void extinguish(IWorld worldIn, BlockPos pos) {
        if (!worldIn.isRemote()) {
            worldIn.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.5F);
        }
        else{
            Random random = worldIn.getRandom();
            for (int i = 0; i < 10; ++i) {
                worldIn.addParticle(ParticleTypes.SMOKE,pos.getX()+0.25f+random.nextFloat()*0.5f,pos.getY()+0.35f+random.nextFloat()*0.5f,pos.getZ()+0.25f+random.nextFloat()*0.5f,0, 0.005, 0);
            }
        }
    }


    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if(!worldIn.isRemote && entityIn instanceof ProjectileEntity) {
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        BlockState state = super.getStateForPlacement(context);
        return state != null ? state.with(WATERLOGGED, flag).with(LIT,!flag) : state;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACE, HORIZONTAL_FACING, LIT, WATERLOGGED);
        //TODO add waterloggable like sconces
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch(state.get(FACE)){
            default:
            case FLOOR:
                return SHAPE_FLOOR;
            case WALL:
                switch (state.get(HORIZONTAL_FACING)){
                    default:
                    case NORTH:
                        return SHAPE_WALL_NORTH;
                    case SOUTH:
                         return SHAPE_WALL_SOUTH;
                    case WEST:
                        return SHAPE_WALL_WEST;
                    case EAST:
                        return SHAPE_WALL_EAST;
                }
            case CEILING:
                return SHAPE_CEILING;
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        if(state.get(FACE)==AttachFace.FLOOR){
            return hasEnoughSolidSide(worldIn, pos.down(), Direction.UP);
        }
        else if(state.get(FACE)==AttachFace.CEILING){
            return hasEnoughSolidSide(worldIn, pos.up(), Direction.DOWN);
        }
        return super.isValidPosition(state,worldIn,pos);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(!stateIn.get(LIT))return;
        Direction dir = stateIn.get(HORIZONTAL_FACING);
        double xm,ym,zm,xl,yl,zl,xr,zr;
        switch(stateIn.get(FACE)){
            default:
            case FLOOR:
                dir=dir.rotateY();
                xm = pos.getX() + 0.5D;
                ym = pos.getY() + 1D;
                zm = pos.getZ() + 0.5D;
                xl = pos.getX() + 0.5D - dir.getXOffset()*0.3125D;
                yl = pos.getY() + 0.9375D;
                zl = pos.getZ() + 0.5D - dir.getZOffset()*0.3125D;
                xr = pos.getX() + 0.5D + dir.getXOffset()*0.3125D;
                zr = pos.getZ() + 0.5D + dir.getZOffset()*0.3125D;
                break;
            case WALL:
                double xoff = -dir.getXOffset()*0.25D;
                double zoff = -dir.getZOffset()*0.25D;
                dir=dir.rotateY();
                xm = pos.getX() + 0.5D + xoff;
                ym = pos.getY() + 1.0625D;
                zm = pos.getZ() + 0.5D + zoff;
                xl = pos.getX() + 0.5D + xoff- dir.getXOffset()*0.3125D;
                yl = pos.getY() + 1D;
                zl = pos.getZ() + 0.5D + zoff - dir.getZOffset()*0.3125D;
                xr = pos.getX() + 0.5D + xoff + dir.getXOffset()*0.3125D;
                zr = pos.getZ() + 0.5D + zoff + dir.getZOffset()*0.3125D;
                break;
            case CEILING:
                dir=dir.rotateY();
                //high
                xm = pos.getX() + 0.5D + dir.getZOffset()*0.3125D;
                zm = pos.getZ() + 0.5D - dir.getXOffset()*0.3125D;
                ym = pos.getY() + 0.875;//0.9375D;
                //2 medium
                xl = pos.getX() + 0.5D + dir.getXOffset()*0.3125D;
                zl = pos.getZ() + 0.5D + dir.getZOffset()*0.3125D;
                xr = pos.getX() + 0.5D - dir.getZOffset()*0.3125D;
                zr = pos.getZ() + 0.5D + dir.getXOffset()*0.3125D;
                yl = pos.getY() + 0.8125;

                double xs = pos.getX() + 0.5D - dir.getXOffset()*0.3125D;
                double zs = pos.getZ() + 0.5D - dir.getZOffset()*0.3125D;
                double ys = pos.getY() + 0.75;
                worldIn.addParticle(ParticleTypes.FLAME, xs, ys, zs, 0, 0, 0);
                break;

        }
        worldIn.addParticle(ParticleTypes.FLAME, xm, ym, zm, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xl, yl, zl, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xr, yl, zr, 0, 0, 0);

    }
}
