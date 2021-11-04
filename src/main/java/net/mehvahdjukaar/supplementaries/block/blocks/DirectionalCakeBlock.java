package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class DirectionalCakeBlock extends CakeBlock implements IWaterLoggable {
    protected static final VoxelShape[] SHAPES_NORTH = new VoxelShape[]{
            Block.box(1, 0, 1, 15, 8, 15),
            Block.box(1, 0, 3, 15, 8, 15),
            Block.box(1, 0, 5, 15, 8, 15),
            Block.box(1, 0, 7, 15, 8, 15),
            Block.box(1, 0, 9, 15, 8, 15),
            Block.box(1, 0, 11, 15, 8, 15),
            Block.box(1, 0, 13, 15, 8, 15)};
    protected static final VoxelShape[] SHAPES_SOUTH = new VoxelShape[]{
            Block.box(1, 0, 1, 15, 8, 15),
            Block.box(1, 0, 1, 15, 8, 13),
            Block.box(1, 0, 1, 15, 8, 11),
            Block.box(1, 0, 1, 15, 8, 9),
            Block.box(1, 0, 1, 15, 8, 7),
            Block.box(1, 0, 1, 15, 8, 5),
            Block.box(1, 0, 1, 15, 8, 3)};
    protected static final VoxelShape[] SHAPES_EAST = new VoxelShape[]{
            Block.box(1, 0, 1, 15, 8, 15),
            Block.box(1, 0, 1, 13, 8, 15),
            Block.box(1, 0, 1, 11, 8, 15),
            Block.box(1, 0, 1, 9, 8, 15),
            Block.box(1, 0, 1, 7, 8, 15),
            Block.box(1, 0, 1, 5, 8, 15),
            Block.box(1, 0, 1, 3, 8, 15)};


    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public DirectionalCakeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0)
                .setValue(FACING, Direction.WEST).setValue(WATERLOGGED, false));
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
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add((new StringTextComponent("You shouldn't have this")).withStyle(TextFormatting.GRAY));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        return this.eatSliceD(worldIn, pos, state, player,
                hit.getDirection().getAxis() != Direction.Axis.Y ? hit.getDirection() : player.getDirection().getOpposite());

    }

    public ActionResultType eatSliceD(IWorld world, BlockPos pos, BlockState state, PlayerEntity player, Direction dir) {
        if (!player.canEat(false)) {
            return ActionResultType.PASS;
        } else {
            player.awardStat(Stats.EAT_CAKE_SLICE);
            player.getFoodData().eat(2, 0.1F);
            if (!world.isClientSide()) {
                this.removeSlice(state, pos, world, dir);
            }
            return ActionResultType.sidedSuccess(world.isClientSide());
        }
    }

    public void removeSlice(BlockState state, BlockPos pos, IWorld world, Direction dir) {
        int i = state.getValue(BITES);
        if (i < 6) {
            if (i == 0 && ServerConfigs.cached.DIRECTIONAL_CAKE) state = state.setValue(FACING, dir);
            world.setBlock(pos, state.setValue(BITES, i + 1), 3);
        } else {
            world.removeBlock(pos, false);
        }
    }


    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(Items.CAKE);
    }

    @Override
    public IFormattableTextComponent getName() {
        return new TranslationTextComponent("minecraft:cake");
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, BITES, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING)) {
            default:
            case WEST:
                return SHAPE_BY_BITE[state.getValue(BITES)];
            case EAST:
                return SHAPES_EAST[state.getValue(BITES)];
            case SOUTH:
                return SHAPES_SOUTH[state.getValue(BITES)];
            case NORTH:
                return SHAPES_NORTH[state.getValue(BITES)];
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (CommonUtil.FESTIVITY.isStValentine()) {
            if (rand.nextFloat() > 0.8) {
                double d0 = (pos.getX() + 0.5 + (rand.nextFloat() - 0.5));
                double d1 = (pos.getY() + 0.25 + (rand.nextFloat() - 0.25));
                double d2 = (pos.getZ() + 0.5 + (rand.nextFloat() - 0.5));
                worldIn.addParticle(ParticleTypes.HEART, d0, d1, d2, 0, 0, 0);
            }
        }
    }
}
