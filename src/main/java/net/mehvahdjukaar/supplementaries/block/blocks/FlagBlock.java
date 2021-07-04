package net.mehvahdjukaar.supplementaries.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.supplementaries.block.tiles.FlagBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;
import java.util.Map;

public class FlagBlock extends WaterBlock {
    protected static final VoxelShape SHAPE = Block.box(4, 0D, 4D, 12.0D, 16.0D, 12.0D);
    private static final Map<DyeColor, Block> BY_COLOR = Maps.newHashMap();
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final DyeColor color;

    public FlagBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        BY_COLOR.put(color, this);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED,false));
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity tileentity = world.getBlockEntity(pos);
        return tileentity instanceof FlagBlockTile ? ((FlagBlockTile)tileentity).getItem(state) : super.getPickBlock(state, target, world, pos, player);

    }

    public DyeColor getColor() {
        return this.color;
    }

    public static Block byColor(DyeColor color) {
        return BY_COLOR.getOrDefault(color, Blocks.WHITE_BANNER);
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FlagBlockTile();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING,WATERLOGGED);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof FlagBlockTile) {
                ((FlagBlockTile)tileentity).setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof FlagBlockTile) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (itemstack.getItem() instanceof FilledMapItem) {
                if(!world.isClientSide) {
                    MapData data = FilledMapItem.getOrCreateSavedData(itemstack, world);
                    if (data instanceof CustomDecorationHolder) {
                        ((CustomDecorationHolder) data).toggleCustomDecoration(world, pos);
                    }
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return ActionResultType.PASS;
    }
}