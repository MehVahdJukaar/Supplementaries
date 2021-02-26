package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
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
import net.minecraftforge.fml.ModList;

public class PancakeBlock extends Block implements IWaterLoggable{
    protected static final VoxelShape SHAPE_1 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    protected static final VoxelShape SHAPE_2 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    protected static final VoxelShape SHAPE_3 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D);
    protected static final VoxelShape SHAPE_4 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    protected static final VoxelShape SHAPE_5 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 10.0D, 14.0D);
    protected static final VoxelShape SHAPE_6 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    protected static final VoxelShape SHAPE_7 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    protected static final VoxelShape SHAPE_8 = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty PANCAKES = BlockProperties.PANCAKES_1_8;
    public static final EnumProperty<BlockProperties.Topping> TOPPING = BlockProperties.TOPPING;

    public PancakeBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(PANCAKES, 1).with(TOPPING, BlockProperties.Topping.NONE).with(WATERLOGGED,false));
    }
    //TODO: add waterloggable block base class
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }


    private BlockProperties.Topping getTopping(Item item){
        if(item instanceof HoneyBottleItem)return BlockProperties.Topping.HONEY;
        if(ModList.get().isLoaded("create")) {
            if (item.getRegistryName().toString().equals("create:bar_of_chocolate")) return BlockProperties.Topping.CHOCOLATE;
        }
        else if(item == Items.COCOA_BEANS)return BlockProperties.Topping.CHOCOLATE;
        if(item.getRegistryName().toString().equals("autumnity:syrup_bottle"))return BlockProperties.Topping.SYRUP;
        //if(item.isIn(ItemTags.getCollection().get(ResourceLocation.tryCreate("forge:sugar"))))return BlockProperties.Topping.CHOCOLATE;
        return BlockProperties.Topping.NONE;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(handIn);
        Item item = stack.getItem();
        BlockProperties.Topping t = getTopping(item);
        if(t!= BlockProperties.Topping.NONE){
            if(state.get(TOPPING) == BlockProperties.Topping.NONE) {
                if (!worldIn.isRemote) {
                    worldIn.setBlockState(pos, state.with(TOPPING, t), 3);
                    worldIn.playSound(null,pos, SoundEvents.BLOCK_HONEY_BLOCK_PLACE, SoundCategory.BLOCKS,1,1.2f);
                }
                ItemStack returnItem = t==BlockProperties.Topping.CHOCOLATE? ItemStack.EMPTY : new ItemStack(Items.GLASS_BOTTLE);
                CommonUtil.swapItem(player,handIn,stack,returnItem);
                //player.setHeldItem(handIn, DrinkHelper.fill(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
        }
        else if(item == Registry.PANCAKE_ITEM.get()){
            return ActionResultType.PASS;
        }
        else if (player.canEat(false)) {
            //player.addStat(Stats.EAT_CAKE_SLICE);
            player.getFoodStats().addStats(1, 0.1F);
            if (!worldIn.isRemote) {


                removeLayer(state,pos,worldIn,player);
                player.playSound(SoundEvents.ENTITY_GENERIC_EAT,SoundCategory.PLAYERS,1,1);
                return ActionResultType.CONSUME;
            }
            else{
                Minecraft.getInstance().particles.addBlockDestroyEffects(player.getPosition().up(1), state);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }


    public static void removeLayer(BlockState state, BlockPos pos, World world, PlayerEntity player){
        int i = state.get(PANCAKES);
        if(i==8){
            BlockPos up = pos.up();
            BlockState upState = world.getBlockState(up);
            if(upState.getBlock()==state.getBlock()){
                removeLayer(upState,up,world,player);
                return;
            }
        }
        if (i > 1) {
            world.setBlockState(pos, state.with(PANCAKES, i - 1), 3);
        } else {
            world.removeBlock(pos, false);
        }
        if(state.get(TOPPING)!= BlockProperties.Topping.NONE){
            player.addPotionEffect(new EffectInstance(Effects.SPEED,8*20));
        }
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = context.getWorld().getBlockState(context.getPos());
        if (blockstate.isIn(this)) {
            return blockstate.with(PANCAKES, Math.min(8, blockstate.get(PANCAKES) + 1));
        }
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(WATERLOGGED,flag);
    }

    protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return !state.getCollisionShape(worldIn, pos).project(Direction.UP).isEmpty() || state.isSolidSide(worldIn, pos, Direction.UP);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.down();
        return this.isValidGround(worldIn.getBlockState(blockpos), worldIn, blockpos);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        if (!stateIn.isValidPosition(worldIn, currentPos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        return useContext.getItem().getItem() == this.asItem() && state.get(PANCAKES) < 8 || super.isReplaceable(state, useContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch(state.get(PANCAKES)) {
            case 1:
            default:
                return SHAPE_1;
            case 2:
                return SHAPE_2;
            case 3:
                return SHAPE_3;
            case 4:
                return SHAPE_4;
            case 5:
                return SHAPE_5;
            case 6:
                return SHAPE_6;
            case 7:
                return SHAPE_7;
            case 8:
                return SHAPE_8;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PANCAKES,TOPPING,WATERLOGGED);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }


}
