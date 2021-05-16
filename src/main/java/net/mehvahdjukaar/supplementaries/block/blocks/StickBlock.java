package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class StickBlock extends WaterBlock{
    protected static final VoxelShape Y_AXIS_AABB = Block.box(7D, 0.0D, 7D, 9D, 16.0D, 9D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(7D, 7D, 0.0D, 9D, 9D, 16.0D);
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 7D, 7D, 16.0D, 9D, 9D);
    protected static final VoxelShape Y_Z_AXIS_AABB = VoxelShapes.or(Block.box(7D, 0.0D, 7D, 9D, 16.0D, 9D),
            Block.box(7D, 7D, 0.0D, 9D, 9D, 16.0D));
    protected static final VoxelShape Y_X_AXIS_AABB = VoxelShapes.or(Block.box(7D, 0.0D, 7D, 9D, 16.0D, 9D),
            Block.box(0.0D, 7D, 7D, 16.0D, 9D, 9D));
    protected static final VoxelShape X_Z_AXIS_AABB = VoxelShapes.or(Block.box(7D, 7D, 0.0D, 9D, 9D, 16.0D),
            Block.box(0.0D, 7D, 7D, 16.0D, 9D, 9D));
    protected static final VoxelShape X_Y_Z_AXIS_AABB = VoxelShapes.or(Block.box(7D, 7D, 0.0D, 9D, 9D, 16.0D),
            Block.box(0.0D, 7D, 7D, 16.0D, 9D, 9D),
            Block.box(7D, 0.0D, 7D, 9D, 16.0D, 9D));

    public static final BooleanProperty AXIS_X = BlockProperties.AXIS_X;
    public static final BooleanProperty AXIS_Y = BlockProperties.AXIS_Y;
    public static final BooleanProperty AXIS_Z = BlockProperties.AXIS_Z;

    protected final Map<Direction.Axis,BooleanProperty> axisToProperty = ImmutableMap.of(Direction.Axis.X,AXIS_X,Direction.Axis.Y,AXIS_Y,Direction.Axis.Z,AXIS_Z);


    public StickBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(AXIS_Y,true).setValue(AXIS_X,false).setValue(AXIS_Z,false));
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add((new StringTextComponent("You shouldn't have this")).withStyle(TextFormatting.GRAY));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS_X,AXIS_Y,AXIS_Z);
    }


    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        boolean x = state.getValue(AXIS_X);
        boolean y = state.getValue(AXIS_Y);
        boolean z = state.getValue(AXIS_Z);
        if(x){
            if(y){
                if(z) return X_Y_Z_AXIS_AABB;
                return Y_X_AXIS_AABB;
            }
            else if(z)return X_Z_AXIS_AABB;
            return X_AXIS_AABB;
        }
        if(z){
            if(y)return Y_Z_AXIS_AABB;
            return Z_AXIS_AABB;
        }
        return Y_AXIS_AABB;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        BooleanProperty axis = axisToProperty.get(context.getClickedFace().getAxis());
        if (blockstate.is(this)) {
            return blockstate.setValue(axis, true);
        } else {
            return super.getStateForPlacement(context).setValue(AXIS_Y,false).setValue(axis, true);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        Item item = context.getItemInHand().getItem();
        if(item == Items.STICK || item == this.asItem()){
            BooleanProperty axis = axisToProperty.get(context.getClickedFace().getAxis());
            if(!state.getValue(axis))return true;
        }
        return super.canBeReplaced(state, context);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(Items.STICK);
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

}
