package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.TurnTableBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TurnTableBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public static final BooleanProperty ROTATING = BlockProperties.ROTATING;

    public TurnTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP)
                .setValue(POWER, 0).setValue(INVERTED, false).setValue(ROTATING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, INVERTED, ROTATING);
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
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (this.updatePower(state, world, pos) && world.getBlockState(pos).getValue(POWER) != 0) {
            this.tryRotate(world, pos);
        }
        // if power changed and is powered or facing block changed
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                BlockHitResult hit) {
        Direction face = hit.getDirection();
        Direction mydir = state.getValue(FACING);
        if (face != mydir && face != mydir.getOpposite()) {
            if (!player.abilities.mayBuild) {
                return InteractionResult.PASS;
            } else {
                state = state.cycle(INVERTED);
                float f = state.getValue(INVERTED) ? 0.55F : 0.5F;
                worldIn.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
                worldIn.setBlock(pos, state, 2 | 4);
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public boolean updatePower(BlockState state, Level world, BlockPos pos) {
        int blockpower = world.getBestNeighborSignal(pos);
        int currentpower = state.getValue(POWER);
        // on-off
        if (blockpower != currentpower) {
            if (blockpower != 0) state = state.setValue(ROTATING, true);
            world.setBlock(pos, state.setValue(POWER, blockpower), 2 | 4);
            return true;
            //returns if state changed
        }
        return false;
    }

    private void tryRotate(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TurnTableBlockTile) {
            ((TurnTableBlockTile) te).tryRotate();
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        boolean powerchanged = this.updatePower(state, world, pos);
        // if power changed and is powered or facing block changed
        if (world.getBlockState(pos).getValue(POWER) != 0 && (powerchanged || fromPos.equals(pos.relative(state.getValue(FACING)))))
            this.tryRotate(world, pos);
    }

    private static Vec3 rotateY(Vec3 vec, double deg) {
        if (deg == 0)
            return vec;
        if (vec == Vec3.ZERO)
            return vec;
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        float angle = (float) ((deg / 180f) * Math.PI);
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        return new Vec3(x * c + z * s, y, z * c - x * s);
    }

    public static int getPeriod(BlockState state) {
        return (60 - state.getValue(POWER) * 4) + 5;
    }

    // rotate entities
    @Override
    public void stepOn(Level world, BlockPos pos, Entity e) {
        super.stepOn(world, pos, e);
        if (!ServerConfigs.cached.TURN_TABLE_ROTATE_ENTITIES) return;
        if (!e.isOnGround()) return;
        BlockState state = world.getBlockState(pos);
        if (state.getValue(POWER) != 0 && state.getValue(FACING) == Direction.UP) {
            float period = getPeriod(state) + 1;
            float ANGLE_INCREMENT = 90f / period;

            float increment = state.getValue(INVERTED) ? ANGLE_INCREMENT : -1 * ANGLE_INCREMENT;
            Vec3 origin = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vec3 oldpos = e.position();
            Vec3 oldoffset = oldpos.subtract(origin);
            Vec3 newoffset = rotateY(oldoffset, increment);
            Vec3 posdiff = origin.add(newoffset).subtract(oldpos);

            e.move(MoverType.SHULKER_BOX, posdiff);
            // e.setMotion(e.getMotion().add(adjustedposdiff));
            e.hurtMarked = true;


            //TODO: use setMotion
            if ((e instanceof LivingEntity)) {
                e.setOnGround(false); //remove this?
                float diff = e.getYHeadRot() - increment;
                e.setYBodyRot(diff);
                e.setYHeadRot(diff);
                ((LivingEntity) e).yHeadRotO = ((LivingEntity) e).yHeadRot;
                ((LivingEntity) e).setNoActionTime(20);
                //e.velocityChanged = true;

                if (e instanceof Cat && ((TamableAnimal) e).isOrderedToSit() && !world.isClientSide) {
                    BlockEntity te = world.getBlockEntity(pos);
                    if (te instanceof TurnTableBlockTile) {
                        TurnTableBlockTile table = ((TurnTableBlockTile) te);
                        if (table.cat == 0) {
                            ((TurnTableBlockTile) te).cat = 20 * 20;
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, ModRegistry.TOM_SOUND.get(), SoundSource.BLOCKS, 0.85f, 1);
                        }
                    }
                }

            }
            // e.prevRotationYaw = e.rotationYaw;

            e.yRot -= increment;
            e.yRotO = e.yRot;


            //e.rotateTowards(e.rotationYaw - increment, e.rotationPitch);
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new TurnTableBlockTile();
    }

}