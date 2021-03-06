package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FlaxBlock extends CropsBlock {
    private static final int DOUBLE_AGE = 4; //age at which it grows in block above
    private static final VoxelShape FULL_BOTTOM = Block.makeCuboidShape(1, 0, 1, 15, 16, 15);
    private static final VoxelShape[] SHAPES_BOTTOM = new VoxelShape[]{
            Block.makeCuboidShape(4, 0, 4, 12, 6, 12),
            Block.makeCuboidShape(3, 0, 3, 13, 10, 13),
            Block.makeCuboidShape(3, 0, 3, 13, 13, 13),
            Block.makeCuboidShape(3, 0, 3, 13, 16, 13),
            Block.makeCuboidShape(2, 0, 2, 14, 16, 14),
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM};
    private static final VoxelShape[] SHAPES_TOP = new VoxelShape[]{
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            Block.makeCuboidShape(2, 0, 2, 14, 3, 14),
            Block.makeCuboidShape(1, 0, 1, 15, 7, 15),
            Block.makeCuboidShape(1, 0, 1, 15, 11, 15),
            Block.makeCuboidShape(1, 0, 1, 15, 16, 15),};

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public FlaxBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(this.getAgeProperty(), 0)
                .with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if(state.get(HALF)==DoubleBlockHalf.LOWER){
            return SHAPES_BOTTOM[state.get(AGE)];
        }
        return SHAPES_TOP[state.get(AGE)];
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(!ClientConfigs.cached.TOOLTIP_HINTS)return;
        //tooltip.add(new TranslationTextComponent("message.supplementaries.flax").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));
    }

    @Override
    public AbstractBlock.OffsetType getOffsetType() {
        return OffsetType.NONE;
    }

    //double plant code
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = stateIn.get(HALF);

        if (facing.getAxis() != Direction.Axis.Y || (half == DoubleBlockHalf.LOWER != (facing == Direction.UP) || !this.isDouble(stateIn)) || (facingState.isIn(this) && facingState.get(HALF) != half )) {
            return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !stateIn.isValidPosition(worldIn, currentPos)
                    ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        } else {
            return Blocks.AIR.getDefaultState();
        }
    }

    public boolean isDouble(BlockState state){
        return this.getAge(state)>=DOUBLE_AGE;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return super.isValidPosition(state, worldIn, pos);
        } else {
            if(!this.isDouble(state))return false;
            BlockState blockstate = worldIn.getBlockState(pos.down());
            if (state.getBlock() != this) return super.isValidPosition(state, worldIn, pos); //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
            return blockstate.isIn(this) && blockstate.get(HALF) == DoubleBlockHalf.LOWER && this.getAge(state) == this.getAge(blockstate);
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!worldIn.isRemote) {
            if (player.isCreative()) {
                removeBottomHalf(worldIn, pos, state, player);
            } else {
                spawnDrops(state, worldIn, pos, null, player, player.getHeldItemMainhand());
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, Blocks.AIR.getDefaultState(), te, stack);
    }

    protected static void removeBottomHalf(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        DoubleBlockHalf doubleblockhalf = state.get(HALF);
        if (doubleblockhalf == DoubleBlockHalf.UPPER) {
            BlockPos blockpos = pos.down();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.getBlock() == state.getBlock() && blockstate.get(HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 35);
                world.playEvent(player, 2001, blockpos, Block.getStateId(blockstate));
            }
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(HALF);
    }

    public void placeAt(IWorld worldIn, BlockPos pos, int flags) {
        worldIn.setBlockState(pos, this.getDefaultState().with(HALF, DoubleBlockHalf.LOWER), flags);
        worldIn.setBlockState(pos.up(), this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER), flags);
    }

    // Tick function
    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (!worldIn.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (state.get(HALF)==DoubleBlockHalf.UPPER)return; //only bottom one handles ticking
        if (worldIn.getLightSubtracted(pos, 0) >= 9) {
            int age = this.getAge(state);
            if (this.canGrow(worldIn,pos,state,worldIn.isRemote)) {
                float f = getGrowthChance(this, worldIn, pos);
                if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, random.nextInt((int) (25.0F / f) + 1) == 0)) {
                    if (age +1 >= DOUBLE_AGE) {
                        worldIn.setBlockState(pos.up(), this.withAge(age + 1).with(HALF, DoubleBlockHalf.UPPER), 3);
                    }
                    worldIn.setBlockState(pos, this.withAge(age + 1), 2);
                    ForgeHooks.onCropsGrowPost(worldIn, pos, state);
                }
            }
        }
    }

    public boolean canGrowUp(IBlockReader worldIn, BlockPos downPos){
        BlockState state = worldIn.getBlockState(downPos.up());
        return state.getBlock() instanceof FlaxBlock || state.getMaterial().isReplaceable();
    }


    //for bonemeal
    @Override
    public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return !this.isMaxAge(state) && (this.canGrowUp(worldIn,pos)||this.getAge(state)<DOUBLE_AGE-1);
    }

    //here I'm assuming canGrow has already been called
    @Override
    public void grow(World worldIn, BlockPos pos, BlockState state) {
        int newAge = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
        newAge = Math.min(newAge, this.getMaxAge());
        if (newAge >= DOUBLE_AGE) {
            if(!this.canGrowUp(worldIn,pos))return;
            worldIn.setBlockState(pos.up(), withAge(newAge).with(HALF, DoubleBlockHalf.UPPER), 3);
        }
        worldIn.setBlockState(pos, withAge(newAge), 2);
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(this.asItem());
    }
}
