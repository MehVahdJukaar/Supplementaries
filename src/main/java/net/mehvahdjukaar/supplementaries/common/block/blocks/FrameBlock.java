package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FrameBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FrameBlock extends MimicBlock implements EntityBlock {

    public static final BooleanProperty HAS_BLOCK = BlockProperties.HAS_BLOCK;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;
    public static final VoxelShape OCCLUSION_SHAPE = Block.box(0.01, 0.01, 0.01, 15.99, 15.99, 15.99);
    public static final VoxelShape OCCLUSION_SHAPE_2 = Block.box(-0.01, -0.01, -0.01, 16.01, 16.01, 16.01);

    public final Supplier<Block> daub;

    public FrameBlock(Properties properties, Supplier<Block> daub) {
        super(properties.lightLevel(state->state.getValue(LIGHT_LEVEL)));
        this.daub = daub;
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0).setValue(HAS_BLOCK, false));
    }


    private static final VoxelShape INSIDE_Z = box(1.0D, 1.0D, 0.0D, 15.0D, 15.0D, 16.0D);
    private static final VoxelShape INSIDE_X = box(0.0D, 1.0D, 1.0D, 16.0D, 15.0D, 15.0D);
    protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(
                    INSIDE_Z, INSIDE_X),
            BooleanOp.ONLY_FIRST);

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (!state.getValue(HAS_BLOCK)) {
            return (adjacentBlockState.getBlock() instanceof FrameBlock && state.getValue(HAS_BLOCK).equals(adjacentBlockState.getValue(HAS_BLOCK))) || super.skipRendering(state, adjacentBlockState, side);
        }
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FrameBlockTile(pPos, pState, daub);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL, HAS_BLOCK);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
            return tile.handleInteraction(player, hand, trace);
        }
        return InteractionResult.PASS;
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
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        if (state.getValue(HAS_BLOCK)) {
            if (reader.getBlockEntity(pos) instanceof FrameBlockTile tile && !tile.getHeldBlock().isAir()) {
                return Shapes.block();
            }
        }
        return OCCLUSION_SHAPE;
    }

    public static final VoxelShape OCCLUSION_SHAPE_3 = Block.box(1, 1, 1, 15, 15, 15);


    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return Shapes.block();
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext p_220071_4_) {
        if (state.getValue(HAS_BLOCK)) {
            return Shapes.block();
        }
        return OCCLUSION_SHAPE_2;
    }
    /*
    @Override
    public VoxelShape getVisualShape(BlockState p_230322_1_, IBlockReader p_230322_2_, BlockPos p_230322_3_, ISelectionContext p_230322_4_) {
        return VoxelShapes.empty();
    }*/

    //occlusion shading
    public float getShadeBrightness(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getValue(HAS_BLOCK) ? 0.2f : 1;
    }

    //let light through
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return !state.getValue(HAS_BLOCK) || super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
            tile.getHeldBlock().getAnalogOutputSignal(world, pos);
        }
        return 0;
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
            tile.getHeldBlock().getEnchantPowerBonus(world, pos);
        }
        return 0;
    }
}
