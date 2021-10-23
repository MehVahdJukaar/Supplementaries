package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.SpringLauncherArmBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public class SpringLauncherBlock extends Block {
    protected static final VoxelShape PISTON_BASE_EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_WEST_AABB = Block.box(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape PISTON_BASE_NORTH_AABB = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_UP_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_DOWN_AABB = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // is base only?
    public SpringLauncherBlock(Properties properties){
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, false));
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return state.getValue(EXTENDED)?PushReaction.BLOCK:PushReaction.NORMAL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getValue(EXTENDED);
    }

    @Override
    //TODO: add this to other blocks
    public boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(EXTENDED);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (state.getValue(EXTENDED)) {
            switch(state.getValue(FACING)) {
                case DOWN:
                    return PISTON_BASE_DOWN_AABB;
                case UP:
                default:
                    return PISTON_BASE_UP_AABB;
                case NORTH:
                    return PISTON_BASE_NORTH_AABB;
                case SOUTH:
                    return PISTON_BASE_SOUTH_AABB;
                case WEST:
                    return PISTON_BASE_WEST_AABB;
                case EAST:
                    return PISTON_BASE_EAST_AABB;
            }
        } else {
            return Shapes.block();
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.checkForMove(state, worldIn, pos);
    }

    public void checkForMove(BlockState state, Level world, BlockPos pos) {
        if (!world.isClientSide()) {
            boolean flag = this.shouldBeExtended(world, pos, state.getValue(FACING));
            BlockPos _bp = pos.offset(state.getValue(FACING).getNormal());
            if (flag && !state.getValue(EXTENDED)) {
                boolean flag2 = false;
                BlockState targetblock = world.getBlockState(_bp);
                if (targetblock.getPistonPushReaction() == PushReaction.DESTROY || targetblock.isAir()) {
                    BlockEntity tileentity = targetblock.hasTileEntity() ? world.getBlockEntity(_bp) : null;
                    dropResources(targetblock, world, _bp, tileentity);
                    flag2 = true;
                }
                /*
                 * else if (targetblock.getBlock() instanceof FallingBlock &&
                 * world.getBlockState(_bp.add(state.get(FACING).getDirectionVec())).isAir(
                 * world, _bp)){ FallingBlockEntity fallingblockentity = new
                 * FallingBlockEntity(world, (double)_bp.getX() + 0.5D, (double)_bp.getY() ,
                 * (double)_bp.getZ() + 0.5D, world.getBlockState(_bp));
                 *
                 * world.addEntity(fallingblockentity); flag2=true; }
                 */
                if (flag2) {
                    world.setBlock(_bp,
                            ModRegistry.SPRING_LAUNCHER_ARM.get().defaultBlockState().setValue(SpringLauncherArmBlock.EXTENDING, true).setValue(FACING, state.getValue(FACING)),
                            3);
                    world.setBlockAndUpdate(pos, state.setValue(EXTENDED, true));
                    world.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.53F,
                            world.random.nextFloat() * 0.25F + 0.45F);
                }
            } else if (!flag && state.getValue(EXTENDED)) {
                BlockState bs = world.getBlockState(_bp);
                if (bs.getBlock() instanceof SpringLauncherHeadBlock && state.getValue(FACING) == bs.getValue(FACING)) {
                    // world.setBlockState(_bp, Blocks.AIR.getDefaultState(), 3);
                    world.setBlock(_bp,
                            ModRegistry.SPRING_LAUNCHER_ARM.get().defaultBlockState().setValue(SpringLauncherArmBlock.EXTENDING, false).setValue(FACING, state.getValue(FACING)),
                            3);
                    world.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.53F,
                            world.random.nextFloat() * 0.15F + 0.45F);
                } else if (bs.getBlock() instanceof SpringLauncherArmBlock
                        && state.getValue(FACING) == bs.getValue(FACING)) {
                    if (world.getBlockEntity(_bp) instanceof SpringLauncherArmBlockTile) {
                        world.getBlockTicks().scheduleTick(pos, world.getBlockState(pos).getBlock(), 1);
                    }
                }
            }
        }
    }

    // piston code
    private boolean shouldBeExtended(Level worldIn, BlockPos pos, Direction facing) {
        for (Direction direction : Direction.values()) {
            if (direction != facing && worldIn.hasSignal(pos.relative(direction), direction)) {
                return true;
            }
        }
        if (worldIn.hasSignal(pos, Direction.DOWN)) {
            return true;
        } else {
            BlockPos blockpos = pos.above();
            for (Direction direction1 : Direction.values()) {
                if (direction1 != Direction.DOWN && worldIn.hasSignal(blockpos.relative(direction1), direction1)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.checkForMove(state, world, pos);
    }
}