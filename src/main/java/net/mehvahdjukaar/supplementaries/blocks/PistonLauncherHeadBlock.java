package net.mehvahdjukaar.supplementaries.blocks;


import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;


public class PistonLauncherHeadBlock extends DirectionalBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty SHORT = BlockStateProperties.SHORT; // is not small? (only used for
    // tile entity, leave true
    public PistonLauncherHeadBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(SHORT, false).with(FACING, Direction.NORTH));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            default :
            case NORTH :
                return VoxelShapes.or(VoxelShapes.create(0.9375D, 0.0625D, 1.1875D, 0.0625D, 0.938D, 0.125D),
                        VoxelShapes.create(0D, 0D, 0D, 1D, 1D, 0.125D));
            case SOUTH :
                return VoxelShapes.or(VoxelShapes.create(0.0625D, 0.0625D, -0.1875D, 0.938D, 0.938D, 0.875D),
                        VoxelShapes.create(1D, 0D, 1D, 0D, 1D, 0.875D));
            case EAST :
                return VoxelShapes.or(VoxelShapes.create(-0.1875D, 0.0625D, 0.9375D, 0.875D, 0.938D, 0.0625D),
                        VoxelShapes.create(1D, 0D, 0D, 0.875D, 1D, 1D));
            case WEST :
                return VoxelShapes.or(VoxelShapes.create(1.1875D, 0.0625D, 0.0625D, 0.125D, 0.938D, 0.938D),
                        VoxelShapes.create(0D, 0D, 1D, 0.125D, 1D, 0D));
            case DOWN :
                return VoxelShapes.or(VoxelShapes.create(0.0625D, 1.1875D, 0.0625D, 0.938D, 0.125D, 0.938D),
                        VoxelShapes.create(0D, 0D, 1D, 1D, 0.125D, 0D));
            case UP :
                return VoxelShapes.or(VoxelShapes.create(0.0625D, -0.1875D, 0.9375D, 0.938D, 0.875D, 0.0625D),
                        VoxelShapes.create(0D, 1D, 0D, 1D, 0.875D, 1D));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHORT);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(Registry.PISTON_LAUNCHER.get());
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    // piston code
    /**
     * Called before the Block is set to air in the world. Called regardless of if
     * the player's tool can actually collect this block
     */
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!worldIn.isRemote && player.abilities.isCreativeMode) {
            BlockPos blockpos = pos.offset(state.get(FACING).getOpposite());
            Block block = worldIn.getBlockState(blockpos).getBlock();
            if (block instanceof PistonLauncherBlock) {
                worldIn.removeBlock(blockpos, false);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockState comp = Registry.PISTON_LAUNCHER_ARM.get().getDefaultState().with(PistonLauncherArmBlock.EXTENDING, false).with(FACING, state.get(FACING));
        if ((state.getBlock() != newState.getBlock()) && (newState != comp)) {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
            Direction direction = state.get(FACING).getOpposite();
            pos = pos.offset(direction);
            BlockState blockstate = worldIn.getBlockState(pos);
            if ((blockstate.getBlock() instanceof PistonLauncherBlock) && blockstate.get(BlockStateProperties.EXTENDED)) {
                spawnDrops(blockstate, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor
     * state, returning a new state. For example, fences make their connections to
     * the passed in state if possible, and wet concrete powder immediately returns
     * its solidified counterpart. Note that this method should ideally consider
     * only the specific face passed in.
     */
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        return facing.getOpposite() == stateIn.get(FACING) && !stateIn.isValidPosition(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState()
                : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState bs = worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite()));
        return bs == Registry.PISTON_LAUNCHER.get().getDefaultState().with(BlockStateProperties.EXTENDED, true).with(FACING, state.get(FACING));
        // return bs == PistonLauncherBlock.block || block ==
        // PistonLauncherArmTileBlock.block;
    }

    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.isValidPosition(worldIn, pos)) {
            BlockPos blockpos = pos.offset(state.get(FACING).getOpposite());
            worldIn.getBlockState(blockpos).neighborChanged(worldIn, blockpos, blockIn, fromPos, false);
        }
    }
}

