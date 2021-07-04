package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.GobletBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class GobletBlock extends WaterBlock {
    protected static final VoxelShape SHAPE = Block.box(5,0,5,11,9,11);

    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public GobletBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0).setValue(WATERLOGGED,false));
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof GobletBlockTile) {
            // make te do the work
            GobletBlockTile te = (GobletBlockTile) tileentity;
            if (te.handleInteraction(player, handIn)) {
                if (!worldIn.isClientSide())
                    te.setChanged();
                return ActionResultType.sidedSuccess(worldIn.isClientSide);
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new GobletBlockTile();
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIGHT_LEVEL);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof GobletBlockTile) {
            return ((GobletBlockTile) tileentity).fluidHolder.isEmpty() ? 0 : 1;
        }
        return 0;
    }

}
