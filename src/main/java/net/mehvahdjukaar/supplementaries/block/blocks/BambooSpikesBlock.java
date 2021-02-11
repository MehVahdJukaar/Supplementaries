package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BambooSpikesBlock extends Block {
    protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);
    protected static final VoxelShape SHAPE_UP = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    protected static final VoxelShape SHAPE_DOWN = Block.makeCuboidShape(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final IntegerProperty POISON = BlockProperties.POISON;


    public BambooSpikesBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(WATERLOGGED,false));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        CompoundNBT com = context.getItem().getTag();
        int poison = com!=null?context.getItem().getMaxDamage()-com.getInt("Damage"):0;
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;;
        return this.getDefaultState().with(FACING, context.getFace()).with(WATERLOGGED,flag).with(POISON, MathHelper.clamp(poison,0,15));
    }

    public ItemStack getSpikeItem(BlockState state){
        int poison = state.get(POISON);
        if(poison==0)return new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get());
        else{
            ItemStack stack = new ItemStack(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
            stack.setDamage(stack.getMaxDamage()-poison);
            return stack;
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> list = new ArrayList<>();
        list.add(this.getSpikeItem(state));
        return list;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)){
            default:
            case DOWN:
                return SHAPE_DOWN;
            case UP:
                return SHAPE_UP;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
        }
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if(entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).isCreative())return;
        if(entityIn instanceof LivingEntity) {
            boolean up = state.get(FACING) == Direction.UP;
            double vy = up ? 0.45 : 0.95;
            entityIn.setMotionMultiplier(state, new Vector3d(0.95D, vy, 0.95D));
            if(!worldIn.isRemote) {
                if(up && entityIn instanceof PlayerEntity && entityIn.isSneaking())return;
                float damage = entityIn.getPosY() > (pos.getY() + 0.0625) ? 2 : 1;
                entityIn.attackEntityFrom(DamageSource.GENERIC, damage);
                int poison = state.get(POISON);
                if (poison > 0) {
                    if (entityIn instanceof LivingEntity) {
                        if (!((LivingEntity) entityIn).isPotionActive(Effects.POISON)) {
                            ((LivingEntity) entityIn).addPotionEffect(new EffectInstance(Effects.POISON, 120));
                            worldIn.setBlockState(pos, state.with(POISON, Math.max(poison - 1, 0)), poison==1?2:2|16);
                        }
                    }
                }
            }
        }
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.DANGER_OTHER;
    }

    public static boolean isLingeringPoison(ItemStack stack){
        return stack.getItem() == Items.LINGERING_POTION && PotionUtils.getEffectsFromStack(stack).stream()
                .map(EffectInstance::getPotion).anyMatch(effect -> effect.equals(Effects.POISON));
    }

    public static boolean addPoison(BlockState state, IWorld world, BlockPos pos, ItemStack stack){
        world.playSound(null, pos, SoundEvents.BLOCK_HONEY_BLOCK_FALL, SoundCategory.BLOCKS, 0.5F, 1.5F);
        return world.setBlockState(pos,state.with(POISON,15),3);
    }

    //TODO: separate potions and add tile and potion support
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(handIn);
        if(isLingeringPoison(stack)) {

            //todo: sound here
            if(!player.isCreative())
                player.setHeldItem(handIn, DrinkHelper.fill(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
            if(!worldIn.isRemote){
                addPoison(state,worldIn,pos,stack);
            }


            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING,WATERLOGGED,POISON);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return this.getSpikeItem(state);
    }
}