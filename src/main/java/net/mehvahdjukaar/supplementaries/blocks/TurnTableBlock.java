package net.mehvahdjukaar.supplementaries.blocks;


import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;



public class TurnTableBlock  extends Block {

    //TODO:figure out why these two don't match up

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public TurnTableBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP).with(POWERED, false).with(INVERTED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, INVERTED);
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
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        boolean powerchanged = this.updatePower(state, world, pos);
        // if power changed and is powered or facing block changed
        if (world.getBlockState(pos).get(POWERED) && powerchanged)
            this.tryRotate(world, pos);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        Direction face = hit.getFace();
        Direction mydir = state.get(FACING);
        if (face != mydir && face != mydir.getOpposite()) {
            if (!player.abilities.allowEdit) {
                return ActionResultType.PASS;
            } else {
                state = state.func_235896_a_(INVERTED);
                float f = state.get(INVERTED) ? 0.55F : 0.5F;
                worldIn.playSound(player, pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3F, f);
                worldIn.setBlockState(pos, state, 2 | 4);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    public boolean updatePower(BlockState state, World world, BlockPos pos) {
        boolean ispowered = world.getRedstonePowerFromNeighbors(pos) > 0;
        boolean haspower = state.get(POWERED);
        // on-off
        if (ispowered != haspower) {
            world.setBlockState(pos, state.with(POWERED, ispowered), 2 | 4);
            return true;
            //returns if state changed
        }
        return false;
        /*
         *
         * //do rotate if(nextrot == 0){ world.setBlockState(pos,
         * state.with(NEXT_ROTATION, PERIOD), 2|4|16); this.doRotateBlock(pos, state,
         * world); //mcserv.getPlayerList().sendMessage(new
         * StringTextComponent("2--"+nextrot));
         *
         * } //keep rotating else if(haspower&&nextrot==PERIOD || nextrot!=PERIOD){
         * world.setBlockState(pos, state.with(NEXT_ROTATION, nextrot-1), 2|4|16);
         * world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), (int) 1);
         * //mcserv.getPlayerList().sendMessage(new StringTextComponent("1--"+nextrot));
         * }
         */
    }

    private void tryRotate(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TurnTableBlockTile) {
            ((TurnTableBlockTile) te).tryRotate();
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        boolean powerchanged = this.updatePower(state, world, pos);
        // if power changed and is powered or facing block changed
        if (world.getBlockState(pos).get(POWERED) && (powerchanged || fromPos.equals(pos.offset(state.get(FACING)))))
            this.tryRotate(world, pos);
        // TODO:optimize this
    }

    private static Vector3d rotateY(Vector3d vec, double deg) {
        if (deg == 0)
            return vec;
        if (vec == Vector3d.ZERO)
            return vec;
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        float angle = (float) ((deg / 180f) * Math.PI);
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        return new Vector3d(x * c + z * s, y, z * c - x * s);
    }

    // rotate entities
    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity e) {
        super.onEntityWalk(world, pos, e);
        BlockState state = world.getBlockState(pos);
        if (state.get(POWERED) && state.get(FACING) == Direction.UP) {
            float ANGLE_INCREMENT = 90f / (float)(ServerConfigs.cached.TURN_TABLE_PERIOD-1);

            float increment = state.get(INVERTED) ? ANGLE_INCREMENT : -1 * ANGLE_INCREMENT;
            Vector3d origin = new Vector3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vector3d oldpos = e.getPositionVec();
            Vector3d oldoffset = oldpos.subtract(origin);
            Vector3d newoffset = rotateY(oldoffset, increment);
            Vector3d posdiff = origin.add(newoffset).subtract(oldpos);

            e.move(MoverType.SELF, posdiff);
            // e.setMotion(e.getMotion().add(adjustedposdiff));
            e.velocityChanged = true;
            if ((e instanceof LivingEntity)) {
                float diff = e.getRotationYawHead() - increment;
                ((LivingEntity) e).setIdleTime(20);
                e.setRenderYawOffset(diff);
                e.setRotationYawHead(diff);
                //e.setOnGround(false); //remove this?
                e.velocityChanged = true;
            }
            // e.prevRotationYaw = e.rotationYaw;
            e.rotationYaw -= increment;
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TurnTableBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}