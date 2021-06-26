package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.CeilingBannerBlockTile;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CeilingBannerBlock extends AbstractBannerBlock {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    private static final VoxelShape SHAPE_X = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_Z = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);

    public CeilingBannerBlock(DyeColor color, AbstractBlock.Properties properties) {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return world.getBlockState(pos.above()).getMaterial().isSolid();
    }

    public BlockState updateShape(BlockState myState, Direction direction, BlockState otherState, IWorld world, BlockPos myPos, BlockPos otherPos) {
        return direction == Direction.UP && !myState.canSurvive(world, myPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(myState, direction, myState, world, myPos, otherPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = this.defaultBlockState();
        IWorldReader iworldreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        blockstate = blockstate.setValue(FACING, context.getHorizontalDirection().getOpposite());
        if (blockstate.canSurvive(iworldreader, blockpos)) {
            return blockstate;
        }
        return null;
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CeilingBannerBlockTile(this.getColor());
    }

    public void setPlacedBy(World p_180633_1_, BlockPos p_180633_2_, BlockState p_180633_3_, @Nullable LivingEntity p_180633_4_, ItemStack p_180633_5_) {
        if (p_180633_5_.hasCustomHoverName()) {
            TileEntity tileentity = p_180633_1_.getBlockEntity(p_180633_2_);
            if (tileentity instanceof CeilingBannerBlockTile) {
                ((CeilingBannerBlockTile)tileentity).setCustomName(p_180633_5_.getHoverName());
            }
        }

    }

    public ItemStack getCloneItemStack(IBlockReader p_185473_1_, BlockPos p_185473_2_, BlockState p_185473_3_) {
        TileEntity tileentity = p_185473_1_.getBlockEntity(p_185473_2_);
        return tileentity instanceof CeilingBannerBlockTile ? ((CeilingBannerBlockTile)tileentity).getItem(p_185473_3_) : super.getCloneItemStack(p_185473_1_, p_185473_2_, p_185473_3_);
    }

    @Override
    public ActionResultType use(BlockState p_225533_1_, World p_225533_2_, BlockPos p_225533_3_, PlayerEntity p_225533_4_, Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
        return super.use(p_225533_1_, p_225533_2_, p_225533_3_, p_225533_4_, p_225533_5_, p_225533_6_);
        //TODO: add markers for maps
        //TODO: fix banner rendering
    }

    @Override
    public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add((new StringTextComponent("You shouldn't have this")).withStyle(TextFormatting.GRAY));
    }
}
