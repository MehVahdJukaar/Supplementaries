package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
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
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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
    public static final BooleanProperty TIPPED = BlockProperties.TIPPED;

    public static DamageSource SPIKE_DAMAGE = (new DamageSource("supplementaries.bamboo_spikes"));

    public BambooSpikesBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(FACING, Direction.NORTH).with(WATERLOGGED,false).with(TIPPED,false));
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

    //this could be improved
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof BambooSpikesBlockTile){
            CompoundNBT com = stack.getTag();
            if(com!=null){
                Potion p = PotionUtils.getPotionFromItem(stack);
                if(p != Potions.EMPTY)((BambooSpikesBlockTile) te).potion = p;
                if(com.contains("Damage"))((BambooSpikesBlockTile) te).setMissingCharges(com.getInt("Damage"));
                //remove in the future
                if(com.contains("BlockEntityTag"))((BambooSpikesBlockTile) te).potion = PotionUtils.getPotionTypeFromNBT(com.getCompound("BlockEntityTag"));
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        CompoundNBT com = context.getItem().getTag();
        int charges = com!=null?context.getItem().getMaxDamage()-com.getInt("Damage"):0;
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;;
        return this.getDefaultState().with(FACING, context.getFace()).with(WATERLOGGED,flag).with(TIPPED, charges!=0);
    }

    public ItemStack getSpikeItem(TileEntity te){
        if(te instanceof BambooSpikesBlockTile) {
            return ((BambooSpikesBlockTile) te).getSpikeItem();
        }
        return new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> list = new ArrayList<>();
        list.add(this.getSpikeItem(builder.get(LootParameters.BLOCK_ENTITY)));
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

    //TODO: fix pathfinding

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if(entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).isCreative())return;
        if(entityIn instanceof LivingEntity && entityIn.isAlive()) {
            boolean up = state.get(FACING) == Direction.UP;
            double vy = up ? 0.45 : 0.95;
            entityIn.setMotionMultiplier(state, new Vector3d(0.95D, vy, 0.95D));
            if(!worldIn.isRemote) {
                if(up && entityIn instanceof PlayerEntity && entityIn.isSneaking())return;
                float damage = entityIn.getPosY() > (pos.getY() + 0.0625) ? 2 : 1;
                entityIn.attackEntityFrom(SPIKE_DAMAGE, damage);
                if(state.get(TIPPED)) {
                    TileEntity te = worldIn.getTileEntity(pos);
                    if (te instanceof BambooSpikesBlockTile) {
                        if(((BambooSpikesBlockTile)te).interactWithEntity(((LivingEntity) entityIn),worldIn)){
                            worldIn.setBlockState(pos,state.with(BambooSpikesBlock.TIPPED,false),3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
        return PathNodeType.BLOCKED;
    }

    public static boolean tryAddingPotion(BlockState state, IWorld world, BlockPos pos, ItemStack stack){
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BambooSpikesBlockTile) {
            if (((BambooSpikesBlockTile) te).tryApplyPotion(stack)) {
                world.playSound(null, pos, SoundEvents.BLOCK_HONEY_BLOCK_FALL, SoundCategory.BLOCKS, 0.5F, 1.5F);
                world.setBlockState(pos,state.with(TIPPED,true),3);
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(handIn);

        if(stack.getItem() instanceof LingeringPotionItem) {
            if(tryAddingPotion(state,worldIn,pos,stack)){
                if (!player.isCreative())
                    player.setHeldItem(handIn, DrinkHelper.fill(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING,WATERLOGGED,TIPPED);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return this.getSpikeItem(world.getTileEntity(pos));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BambooSpikesBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}