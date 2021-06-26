package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class PancakeBlock extends WaterBlock{
    protected static final VoxelShape SHAPE_1 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    protected static final VoxelShape SHAPE_2 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    protected static final VoxelShape SHAPE_3 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D);
    protected static final VoxelShape SHAPE_4 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    protected static final VoxelShape SHAPE_5 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 10.0D, 14.0D);
    protected static final VoxelShape SHAPE_6 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    protected static final VoxelShape SHAPE_7 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    protected static final VoxelShape SHAPE_8 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public static final IntegerProperty PANCAKES = BlockProperties.PANCAKES_1_8;
    public static final EnumProperty<BlockProperties.Topping> TOPPING = BlockProperties.TOPPING;

    public PancakeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PANCAKES, 1).setValue(TOPPING, BlockProperties.Topping.NONE).setValue(WATERLOGGED,false));
    }

    private BlockProperties.Topping getTopping(Item item){
        if(item instanceof HoneyBottleItem)return BlockProperties.Topping.HONEY;
        //TODO: add tag support here
        if((ModTags.CHOCOLATE_BARS.getValues().isEmpty() && item == Items.COCOA_BEANS) || item.is(ModTags.CHOCOLATE_BARS)) {
            return BlockProperties.Topping.CHOCOLATE;
        }
        if(item.getRegistryName().toString().equals("autumnity:syrup_bottle"))return BlockProperties.Topping.SYRUP;
        //if(item.isIn(ItemTags.getCollection().get(ResourceLocation.tryCreate("forge:sugar"))))return BlockProperties.Topping.CHOCOLATE;
        return BlockProperties.Topping.NONE;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        Item item = stack.getItem();
        BlockProperties.Topping t = getTopping(item);
        if(t!= BlockProperties.Topping.NONE){
            if(state.getValue(TOPPING) == BlockProperties.Topping.NONE) {
                if (!worldIn.isClientSide) {
                    worldIn.setBlock(pos, state.setValue(TOPPING, t), 3);
                    worldIn.playSound(null,pos, SoundEvents.HONEY_BLOCK_PLACE, SoundCategory.BLOCKS,1,1.2f);
                }
                ItemStack returnItem = t==BlockProperties.Topping.CHOCOLATE? ItemStack.EMPTY : new ItemStack(Items.GLASS_BOTTLE);
                if(!player.isCreative())
                    CommonUtil.swapItem(player,handIn,returnItem);
                //player.setHeldItem(handIn, DrinkHelper.fill(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
                return ActionResultType.sidedSuccess(worldIn.isClientSide);
            }
        }
        else if(item == Registry.PANCAKE_ITEM.get()){
            return ActionResultType.PASS;
        }
        else if (player.canEat(false)) {
            //player.addStat(Stats.EAT_CAKE_SLICE);
            player.getFoodData().eat(1, 0.1F);
            if (!worldIn.isClientSide) {


                removeLayer(state,pos,worldIn,player);
                player.playNotifySound(SoundEvents.GENERIC_EAT,SoundCategory.PLAYERS,1,1);
                return ActionResultType.CONSUME;
            }
            else{
                Minecraft.getInstance().particleEngine.destroy(player.blockPosition().above(1), state);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }


    public static void removeLayer(BlockState state, BlockPos pos, World world, PlayerEntity player){
        int i = state.getValue(PANCAKES);
        if(i==8){
            BlockPos up = pos.above();
            BlockState upState = world.getBlockState(up);
            if(upState.getBlock()==state.getBlock()){
                removeLayer(upState,up,world,player);
                return;
            }
        }
        if (i > 1) {
            world.setBlock(pos, state.setValue(PANCAKES, i - 1), 3);
        } else {
            world.removeBlock(pos, false);
        }
        if(state.getValue(TOPPING)!= BlockProperties.Topping.NONE){
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED,8*20));
        }
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            return blockstate.setValue(PANCAKES, Math.min(8, blockstate.getValue(PANCAKES) + 1));
        }
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED,flag);
    }

    protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return !state.getCollisionShape(worldIn, pos).getFaceShape(Direction.UP).isEmpty() || state.isFaceSturdy(worldIn, pos, Direction.UP);
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return this.isValidGround(worldIn.getBlockState(blockpos), worldIn, blockpos);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        if (!stateIn.canSurvive(worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext) {
        return useContext.getItemInHand().getItem() == this.asItem() && state.getValue(PANCAKES) < 8 || super.canBeReplaced(state, useContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch(state.getValue(PANCAKES)) {
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PANCAKES,TOPPING,WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }


}
