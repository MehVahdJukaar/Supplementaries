package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.fluids.ISoftFluidConsumer;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties.Topping;
import net.mehvahdjukaar.supplementaries.common.utils.ModTags;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PancakeBlock extends WaterBlock implements ISoftFluidConsumer {
    protected static final VoxelShape SHAPE_1 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    protected static final VoxelShape SHAPE_2 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    protected static final VoxelShape SHAPE_3 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D);
    protected static final VoxelShape SHAPE_4 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    protected static final VoxelShape SHAPE_5 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 10.0D, 14.0D);
    protected static final VoxelShape SHAPE_6 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    protected static final VoxelShape SHAPE_7 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    protected static final VoxelShape SHAPE_8 = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public static final IntegerProperty PANCAKES = BlockProperties.PANCAKES_1_8;
    public static final EnumProperty<Topping> TOPPING = BlockProperties.TOPPING;

    public PancakeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PANCAKES, 1).setValue(TOPPING, Topping.NONE).setValue(WATERLOGGED, false));
    }

    private Topping getTopping(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof HoneyBottleItem) return BlockProperties.Topping.HONEY;
        if ((ModTags.CHOCOLATE_BARS.getValues().isEmpty() && item == Items.COCOA_BEANS) || stack.is(ModTags.CHOCOLATE_BARS)) {
            return Topping.CHOCOLATE;
        }
        if (item.getRegistryName().toString().equals("autumnity:syrup_bottle")) return Topping.SYRUP;
        //if(item.isIn(ItemTags.getCollection().get(ResourceLocation.tryCreate("forge:sugar"))))return BlockProperties.Topping.CHOCOLATE;
        return Topping.NONE;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        Item item = stack.getItem();
        Topping t = getTopping(stack);
        if (t != Topping.NONE) {
            if (state.getValue(TOPPING) == Topping.NONE) {
                if (!worldIn.isClientSide) {
                    worldIn.setBlock(pos, state.setValue(TOPPING, t), 3);
                    worldIn.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1, 1.2f);
                }
                ItemStack returnItem = t == Topping.CHOCOLATE ? ItemStack.EMPTY : new ItemStack(Items.GLASS_BOTTLE);
                if (!player.isCreative()) Utils.swapItem(player, handIn, returnItem);
                //player.setHeldItem(handIn, DrinkHelper.fill(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
        } else if (item == ModRegistry.PANCAKE_ITEM.get()) {
            return InteractionResult.PASS;
        } else if (player.canEat(false)) {
            //player.addStat(Stats.EAT_CAKE_SLICE);
            player.getFoodData().eat(1, 0.1F);
            if (!worldIn.isClientSide) {


                removeLayer(state, pos, worldIn, player);
                player.playNotifySound(SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1, 1);
                return InteractionResult.CONSUME;
            } else {
                Minecraft.getInstance().particleEngine.destroy(player.blockPosition().above(1), state);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }


    public static void removeLayer(BlockState state, BlockPos pos, Level world, Player player) {
        int i = state.getValue(PANCAKES);
        if (i == 8) {
            BlockPos up = pos.above();
            BlockState upState = world.getBlockState(up);
            if (upState.getBlock() == state.getBlock()) {
                removeLayer(upState, up, world, player);
                return;
            }
        }
        if (i > 1) {
            world.setBlock(pos, state.setValue(PANCAKES, i - 1), 3);
        } else {
            world.removeBlock(pos, false);
        }
        if (state.getValue(TOPPING) != Topping.NONE) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 8 * 20));
        }
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            return blockstate.setValue(PANCAKES, Math.min(8, blockstate.getValue(PANCAKES) + 1));
        }
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    protected boolean isValidGround(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return !state.getCollisionShape(worldIn, pos).getFaceShape(Direction.UP).isEmpty() || state.isFaceSturdy(worldIn, pos, Direction.UP);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return this.isValidGround(worldIn.getBlockState(blockpos), worldIn, blockpos);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (!stateIn.canSurvive(worldIn, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return useContext.getItemInHand().getItem() == this.asItem() && state.getValue(PANCAKES) < 8 || super.canBeReplaced(state, useContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(PANCAKES)) {
            default -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            case 4 -> SHAPE_4;
            case 5 -> SHAPE_5;
            case 6 -> SHAPE_6;
            case 7 -> SHAPE_7;
            case 8 -> SHAPE_8;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PANCAKES, TOPPING, WATERLOGGED);
    }

    @Override
    public boolean tryAcceptingFluid(Level world, BlockState state, BlockPos pos, SoftFluid f, CompoundTag nbt, int amount) {
        Topping topping = Topping.fromFluid(f);
        if (state.getValue(TOPPING) == Topping.NONE && topping != Topping.NONE) {
            world.setBlock(pos, state.setValue(TOPPING, topping), 2);
            world.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1, 1.2f);
            return true;
        }
        return false;
    }
}
