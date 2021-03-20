package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.TurnTableBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;



public class TurnTableBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public TurnTableBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP).with(POWER, 0).with(INVERTED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, INVERTED);
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
        if(this.updatePower(state, world, pos) && world.getBlockState(pos).get(POWER)!=0){
            this.tryRotate(world, pos);
        }
        // if power changed and is powered or facing block changed
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
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
        }
        return ActionResultType.PASS;
    }

    public boolean updatePower(BlockState state, World world, BlockPos pos) {
        int blockpower = world.getRedstonePowerFromNeighbors(pos);
        int currentpower = state.get(POWER);
        // on-off
        if (blockpower != currentpower) {
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof TurnTableBlockTile) {
                TurnTableBlockTile table = ((TurnTableBlockTile) te);

            }
            world.setBlockState(pos, state.with(POWER, blockpower), 2 | 4);
            return true;
            //returns if state changed
        }
        return false;
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
        if (world.getBlockState(pos).get(POWER)!=0 && (powerchanged || fromPos.equals(pos.offset(state.get(FACING)))))
            this.tryRotate(world, pos);
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

    public static int getPeriod(BlockState state){
        return (60-state.get(POWER)*4)+5;
    }

    // rotate entities
    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity e) {
        super.onEntityWalk(world, pos, e);
        if(!ServerConfigs.cached.TURN_TABLE_ROTATE_ENTITIES)return;
        if(!e.isOnGround())return;
        BlockState state = world.getBlockState(pos);
        if (state.get(POWER)!=0 && state.get(FACING) == Direction.UP) {
            float period = getPeriod(state)+1;
            float ANGLE_INCREMENT = 90f / period;

            float increment = state.get(INVERTED) ? ANGLE_INCREMENT : -1 * ANGLE_INCREMENT;
            Vector3d origin = new Vector3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vector3d oldpos = e.getPositionVec();
            Vector3d oldoffset = oldpos.subtract(origin);
            Vector3d newoffset = rotateY(oldoffset, increment);
            Vector3d posdiff = origin.add(newoffset).subtract(oldpos);

            e.move(MoverType.SHULKER_BOX, posdiff);
            // e.setMotion(e.getMotion().add(adjustedposdiff));
            //e.velocityChanged = true;


            //TODO: use setMotion
            if ((e instanceof LivingEntity)) {
                e.setOnGround(false); //remove this?
                float diff = e.getRotationYawHead() - increment;
                e.setRenderYawOffset(diff);
                e.setRotationYawHead(diff);
                ((LivingEntity) e).prevRotationYawHead=((LivingEntity) e).rotationYawHead;
                ((LivingEntity) e).setIdleTime(20);
                //e.velocityChanged = true;

                if(e instanceof CatEntity &&((TameableEntity) e).isSitting()&&!world.isRemote){
                    TileEntity te = world.getTileEntity(pos);
                    if(te instanceof TurnTableBlockTile) {
                        TurnTableBlockTile table = ((TurnTableBlockTile) te);
                        if(table.cat==0) {
                            ((TurnTableBlockTile) te).cat = 20*20;
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, Registry.TOM_SOUND.get(), SoundCategory.BLOCKS, 0.85f, 1);
                        }
                    }
                }

            }
            // e.prevRotationYaw = e.rotationYaw;

            e.rotationYaw -= increment;
            e.prevRotationYaw = e.rotationYaw;







            //e.rotateTowards(e.rotationYaw - increment, e.rotationPitch);
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

}