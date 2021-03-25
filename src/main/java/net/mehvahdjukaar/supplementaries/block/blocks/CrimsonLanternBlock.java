package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.OilLanternBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class CrimsonLanternBlock extends OilLanternBlock {
    public static final VoxelShape SHAPE_DOWN = VoxelShapes.or(Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D), Block.box(6.0D, 8.0D, 6.0D, 10.0D, 9.0D, 10.0D));
    public static final VoxelShape SHAPE_UP = VoxelShapes.or(Block.box(5.0D, 5.0D, 5.0D, 11.0D, 13.0D, 11.0D), Block.box(6.0D, 13.0D, 6.0D, 10.0D, 14.0D, 10.0D));

    public CrimsonLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = super.getStateForPlacement(context);
        if(state!=null)return state.setValue(LIT,!water);
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch(state.getValue(FACE)) {
            default:
            case FLOOR:
                return SHAPE_DOWN;
            case CEILING:
                return SHAPE_UP;
            case WALL:
                return super.getShape(state,world,pos,context);
        }
    }




    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(worldIn.getFluidState(pos).getType() != Fluids.WATER)return super.use(state,worldIn,pos,player,handIn,hit);
        return ActionResultType.PASS;
    }



    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new OilLanternBlockTile(Registry.CRIMSON_LANTERN_TILE.get());
    }
}
