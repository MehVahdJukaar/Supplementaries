package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.PistonLauncherArmBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Arrays;


public class PistonLauncherHeadBlock extends DirectionalBlock {
    protected static final VoxelShape PISTON_EXTENSION_EAST_AABB = Block.makeCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_WEST_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_SOUTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_NORTH_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    protected static final VoxelShape PISTON_EXTENSION_UP_AABB = Block.makeCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_DOWN_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    protected static final VoxelShape UP_ARM_AABB = Block.makeCuboidShape(1.0D, -4.0D, 1.0D, 15.0D, 12.0D, 15.0D);
    protected static final VoxelShape DOWN_ARM_AABB = Block.makeCuboidShape(1.0D, 4.0D, 1.0D, 15.0D, 20.0D, 15.0D);
    protected static final VoxelShape SOUTH_ARM_AABB = Block.makeCuboidShape(1.0D, 1.0D, -4.0D, 15.0D, 15.0D, 12.0D);
    protected static final VoxelShape NORTH_ARM_AABB = Block.makeCuboidShape(1.0D, 1.0D, 4.0D, 15.0D, 15.0D, 20.0D);
    protected static final VoxelShape EAST_ARM_AABB = Block.makeCuboidShape(-4.0D, 1.0D, 1.0D, 12.0D, 15.0D, 15.0D);
    protected static final VoxelShape WEST_ARM_AABB = Block.makeCuboidShape(4.0D, 1.0D, 1.0D, 20.0D, 15.0D, 15.0D);
    protected static final VoxelShape SHORT_UP_ARM_AABB = Block.makeCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
    protected static final VoxelShape SHORT_DOWN_ARM_AABB = Block.makeCuboidShape(1.0D, 4.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    protected static final VoxelShape SHORT_SOUTH_ARM_AABB = Block.makeCuboidShape(1.0D, 1.0D, 0.0D, 15.0D, 15.0D, 12.0D);
    protected static final VoxelShape SHORT_NORTH_ARM_AABB = Block.makeCuboidShape(1.0D, 1.0D, 4.0D, 15.0D, 15.0D, 16.0D);
    protected static final VoxelShape SHORT_EAST_ARM_AABB = Block.makeCuboidShape(0.0D, 1.0D, 1.0D, 12.0D, 15.0D, 15.0D);
    protected static final VoxelShape SHORT_WEST_ARM_AABB = Block.makeCuboidShape(4.0D, 1.0D, 1.0D, 16.0D, 15.0D, 15.0D);
    private static final VoxelShape[] EXTENDED_SHAPES = getShapesForExtension(true);
    private static final VoxelShape[] UNEXTENDED_SHAPES = getShapesForExtension(false);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty SHORT = BlockStateProperties.SHORT; // is not small? (only used for
    // tile entity, leave true
    public PistonLauncherHeadBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(SHORT, false).with(FACING, Direction.NORTH));
    }

    private static VoxelShape[] getShapesForExtension(boolean extended) {
        return Arrays.stream(Direction.values()).map((direction) -> {
            return getShapeForDirection(direction, extended);
        }).toArray((id) -> {
            return new VoxelShape[id];
        });
    }

    private static VoxelShape getShapeForDirection(Direction direction, boolean shortArm) {
        switch(direction) {
            case DOWN:
            default:
                return VoxelShapes.or(PISTON_EXTENSION_DOWN_AABB, shortArm ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
            case UP:
                return VoxelShapes.or(PISTON_EXTENSION_UP_AABB, shortArm ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
            case NORTH:
                return VoxelShapes.or(PISTON_EXTENSION_NORTH_AABB, shortArm ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
            case SOUTH:
                return VoxelShapes.or(PISTON_EXTENSION_SOUTH_AABB, shortArm ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
            case WEST:
                return VoxelShapes.or(PISTON_EXTENSION_WEST_AABB, shortArm ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
            case EAST:
                return VoxelShapes.or(PISTON_EXTENSION_EAST_AABB, shortArm ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return (state.get(SHORT) ? EXTENDED_SHAPES : UNEXTENDED_SHAPES)[state.get(FACING).ordinal()];
    }


    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        BlockState state = worldIn.getBlockState(pos);
        if (entityIn.isSuppressingBounce() || state.get(FACING)!=Direction.UP) {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        } else {
            entityIn.onLivingFall(fallDistance, 0.0F);
            //TODO: add falling block entity support
            if((entityIn instanceof LivingEntity) && !worldIn.isRemote && fallDistance>(float)ServerConfigs.cached.LAUNCHER_HEIGHT){
                worldIn.setBlockState(pos, Registry.PISTON_LAUNCHER_ARM.get().getDefaultState()
                        .with(PistonLauncherArmBlock.EXTENDING, false).with(FACING, state.get(FACING)), 3);
                TileEntity te = worldIn.getTileEntity(pos);
                if(te instanceof PistonLauncherArmBlockTile){
                    PistonLauncherArmBlockTile pistonarm = (PistonLauncherArmBlockTile) te;
                    pistonarm.age = 1;
                    pistonarm.offset = -0.5;
                }
            }
            //this.bounceEntity(entityIn);
        }

    }

    /**
     * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
     * on its own
     */
    /*
    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        if (entityIn.isSuppressingBounce()) {
            super.onLanded(worldIn, entityIn);
        } else {
            this.bounceEntity(entityIn);
        }

    }*/

    private void bounceEntity(Entity entity) {
        Vector3d vector3d = entity.getMotion();
        if (vector3d.y < 0.0D) {
            double d0 = entity instanceof LivingEntity ? 1.0D : 0.8D;
            entity.setMotion(vector3d.x, -vector3d.y * d0, vector3d.z);
        }

    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
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

