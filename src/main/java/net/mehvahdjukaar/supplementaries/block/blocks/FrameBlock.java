package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class FrameBlock extends MimicBlock {

    public static final BooleanProperty HAS_BLOCK = BlockProperties.HAS_BLOCK;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;
    public static final VoxelShape OCCLUSION_SHAPE = Block.box(0.01, 0.01, 0.01, 15.99, 15.99, 15.99);
    public static final VoxelShape OCCLUSION_SHAPE_2 = Block.box(-0.01, -0.01, -0.01, 16.01, 16.01, 16.01);

    public final Supplier<Block> daub;

    public FrameBlock(Properties properties, Supplier<Block> daub) {
        super(properties);
        this.daub = daub;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0).setValue(HAS_BLOCK, false));
    }


    private static final VoxelShape INSIDE_Z = box(1.0D, 1.0D, 0.0D, 15.0D, 15.0D, 16.0D);
    private static final VoxelShape INSIDE_X = box(0.0D, 1.0D, 1.0D, 16.0D, 15.0D, 15.0D);
    protected static final VoxelShape SHAPE = VoxelShapes.join(VoxelShapes.block(), VoxelShapes.or(
            INSIDE_Z, INSIDE_X),
            IBooleanFunction.ONLY_FIRST);

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (!state.getValue(HAS_BLOCK)) {
            return (adjacentBlockState.getBlock() instanceof FrameBlock && state.getValue(HAS_BLOCK).equals(adjacentBlockState.getValue(HAS_BLOCK))) || super.skipRendering(state, adjacentBlockState, side);
        }
        return false;
    }


    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIGHT_LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL, HAS_BLOCK);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FrameBlockTile(daub);
    }


    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof FrameBlockTile) {
            return ((FrameBlockTile) te).handleInteraction(player, hand, trace);
        }
        return ActionResultType.PASS;
    }


    /*
    @Override
    public VoxelShape getInteractionShape(BlockState p_199600_1_, IBlockReader p_199600_2_, BlockPos p_199600_3_) {
        return VoxelShapes.block();
    }

    */
    //TODO: fix face disappearing
    //handles dynamic culling
    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader reader, BlockPos pos) {
        if (state.getValue(HAS_BLOCK)) {
            TileEntity te = reader.getBlockEntity(pos);
            if (te instanceof FrameBlockTile && !((IBlockHolder) te).getHeldBlock().isAir()) {
                return VoxelShapes.block();
            }
        }
        return OCCLUSION_SHAPE;
    }

    public static final VoxelShape OCCLUSION_SHAPE_3 = Block.box(1, 1, 1, 15, 15, 15);


    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return VoxelShapes.block();
    }

    /*
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        if(state.getValue(TILE)==1){
            TileEntity te = reader.getBlockEntity(pos);
            if (te instanceof FrameBlockTile && !((IBlockHolder) te).getHeldBlock().isAir()) {
                return VoxelShapes.block();
            }
        }
        return OCCLUSION_SHAPE;
    }*/


    /*
    @Override
    public VoxelShape getBlockSupportShape(BlockState p_230335_1_, IBlockReader p_230335_2_, BlockPos p_230335_3_) {
        return VoxelShapes.block();
    }
    */
    //needed for isnide black edges

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext p_220071_4_) {
        if (state.getValue(HAS_BLOCK)) {
            return VoxelShapes.block();
        }
        return OCCLUSION_SHAPE_2;
    }
    /*
    @Override
    public VoxelShape getVisualShape(BlockState p_230322_1_, IBlockReader p_230322_2_, BlockPos p_230322_3_, ISelectionContext p_230322_4_) {
        return VoxelShapes.empty();
    }*/

    //occlusion shading
    public float getShadeBrightness(BlockState state, IBlockReader reader, BlockPos pos) {
        return state.getValue(HAS_BLOCK) ? 0.2f : 1;
    }

    //let light through
    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return !state.getValue(HAS_BLOCK) || super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof FrameBlockTile) {
            ((IBlockHolder) te).getHeldBlock().getAnalogOutputSignal(world, pos);
        }
        return 0;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof FrameBlockTile) {
            ((IBlockHolder) te).getHeldBlock().getEnchantPowerBonus(world, pos);
        }
        return 0;
    }
}
