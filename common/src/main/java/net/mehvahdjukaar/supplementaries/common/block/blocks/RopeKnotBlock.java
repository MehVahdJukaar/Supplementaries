package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.PostType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.RopeKnotBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RopeKnotBlock extends AbstractRopeKnotBlock implements IRopeConnection {

    public static final VoxelShape SIDE_SHAPE = Block.box(6, 9, 0, 10, 13, 10);

    public RopeKnotBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RopeKnotBlockTile(pPos, pState);
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        BlockState newState = state.setValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(facing), this.shouldConnectToFace(state, facingState, facingPos, facing, world));
        if (world.getBlockEntity(currentPos) instanceof RopeKnotBlockTile tile) {
            BlockState oldHeld = tile.getHeldBlock();

            RopeKnotBlockTile otherTile = null;
            if (facingState.is(ModRegistry.ROPE_KNOT.get())) {
                if (world.getBlockEntity(facingPos) instanceof RopeKnotBlockTile te2) {
                    otherTile = te2;
                    facingState = otherTile.getHeldBlock();
                }
            }

            BlockState newHeld = null;

            if (newHeld == null) {
                newHeld = oldHeld.updateShape(facing, facingState, world, currentPos, facingPos);
            }

            //manually refreshTextures facing states
            if (!(facingState.getBlock() instanceof IRopeConnection)) {
                BlockState newFacing = facingState.updateShape(facing.getOpposite(), newHeld, world, facingPos, currentPos);

                if (newFacing != facingState) {
                    if (otherTile != null) {
                        otherTile.setHeldBlock(newFacing);
                        otherTile.setChanged();
                    } else {
                        world.setBlock(facingPos, newFacing, 2);
                    }
                }
            }

            PostType type = PostType.get(newHeld);

            if (newHeld != oldHeld) {
                tile.setHeldBlock(newHeld);
                tile.setChanged();
            }
            if (newState != state) {
                tile.recalculateShapes(newState);
            }
            if (type != null) {
                newState = newState.setValue(POST_TYPE, type);
            }
        }

        return newState;
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof ShearsItem) {
            if (!world.isClientSide) {
                if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
                    popResource(world, pos, new ItemStack(ModRegistry.ROPE.get()));
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.PLAYERS, 0.8F, 1.3F);
                    world.setBlock(pos, tile.getHeldBlock(), 3);
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }


    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            BlockState mimic = tile.getHeldBlock();
            return mimic.getBlock().getCloneItemStack(level, pos, state);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public boolean canSideAcceptConnection(BlockState state, Direction direction) {
        if (state.getValue(AbstractRopeKnotBlock.AXIS) == Direction.Axis.Y) {
            return direction.getAxis() != Direction.Axis.Y;
        } else {
            return direction.getAxis() == Direction.Axis.Y;
        }
    }

    @Override
    public VoxelShape getSideShape() {
        return SIDE_SHAPE;
    }
}
