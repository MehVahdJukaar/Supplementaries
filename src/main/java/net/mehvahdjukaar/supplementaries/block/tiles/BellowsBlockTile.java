package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;

public class BellowsBlockTile extends TileEntity implements ITickableTileEntity {

    public float height = 0;
    public float prevHeight = 0;
    private long offset = 0;

    public BellowsBlockTile() {
        super(Registry.BELLOWS_TILE.get());
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 128;
    }

    public VoxelShape getVoxelShape(Direction direction) {
        if(direction.getAxis() == Direction.Axis.Y){
            return VoxelShapes.create(0,0,-this.height,1,1,1+this.height);
        }
        else{
            return VoxelShapes.create(0,-this.height,0,1,1+this.height,1);
        }
    }

    private AxisAlignedBB getHalfBoundingBox(Direction dir) {
        return new AxisAlignedBB(this.pos)
                .contract(-0.5*dir.getXOffset(),-0.5*dir.getYOffset(),-0.5*dir.getZOffset());
    }

    private void moveCollidedEntities(){
        Direction dir = this.getDirection().getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        for(int j=0; j<2; j++) {
            AxisAlignedBB axisalignedbb = this.getHalfBoundingBox(dir);
            List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.getPushReaction() != PushReaction.IGNORE) {
                        AxisAlignedBB entityBB = entity.getBoundingBox();
                        double dy = 0.0D;
                        double dz = 0.0D;
                        float f  = this.height+0.01f;
                        switch (dir) {
                            case SOUTH:
                                dz = axisalignedbb.maxZ+f - entityBB.minZ;
                                if (dz < 0) continue;
                                break;
                            case NORTH:
                                dz = axisalignedbb.minZ-f - entityBB.maxZ;
                                if (dz > 0) continue;
                                break;
                            default:
                            case UP:
                                dy = axisalignedbb.maxY+f - entityBB.minY;
                                if (dy < 0) continue;
                                break;
                            case DOWN:
                                dy = axisalignedbb.minY-f - entityBB.maxY;
                                if (dy > 0) continue;
                                break;
                        }
                        entity.move(MoverType.SHULKER_BOX, new Vector3d(0, dy, dz));
                    }
                }
            }
            dir = dir.getOpposite();
        }
    }

    private void pushEntities(Direction facing, float period, float range){

        double velocity = ServerConfigs.cached.BELLOWS_BASE_VEL_SCALING/period; // Affects acceleration
        double maxVelocity = ServerConfigs.cached.BELLOWS_MAX_VEL; // Affects max speed

        AxisAlignedBB facingBox = CommonUtil.getDirectionBB(this.pos, facing, (int)range);
        List<Entity> list = this.world.getEntitiesWithinAABB(Entity.class, facingBox);

        for (Entity entity : list) {

            if (!this.inLineOfSight(entity, facing)) continue;
            if (facing == Direction.UP) maxVelocity *= 0.5D;
            AxisAlignedBB entityBB = entity.getBoundingBox();
            double dist;
            double b;
            switch(facing){
                default:
                case SOUTH:
                    b = pos.getZ()+1;
                    if(entityBB.minZ<b)continue;
                    dist = entity.getPosZ() - b;
                    break;
                case NORTH:
                    b = pos.getZ();
                    if(entityBB.maxZ>b)continue;
                    dist = b - entity.getPosZ();
                    break;
                case EAST:
                    b = pos.getX()+1;
                    if(entityBB.minX<b)continue;
                    dist = entity.getPosX() - b;
                    break;
                case WEST:
                    b = pos.getX();
                    if(entityBB.maxX>b)continue;
                    dist = b - entity.getPosX();
                    break;
                case UP:
                    b = pos.getY()+1;
                    if(entityBB.minY<b)continue;
                    dist = entity.getPosY() - b;
                    break;
                case DOWN:
                    b = pos.getY();
                    if(entityBB.maxY>b)continue;
                    dist = b - entity.getPosY();
                    break;
            }
            //dist, vel>0
            velocity *=(range-dist)/range;

            if (Math.abs(entity.getMotion().getCoordinate(facing.getAxis())) < maxVelocity) {
                entity.setMotion(entity.getMotion().add(facing.getXOffset() * velocity, facing.getYOffset() * velocity, facing.getZOffset() * velocity));
                if(ServerConfigs.cached.BELLOWS_FLAG) entity.velocityChanged = true;
            }
        }
    }

    public void tick() {

        int power = this.getBlockState().get(BellowsBlock.POWER);
        this.prevHeight = this.height;

        if(power!=0){
            long time = this.world.getGameTime();
            if(offset==0) offset = time;

            float period = ((float)ServerConfigs.cached.BELLOWS_PERIOD)-(power-1)*((float)ServerConfigs.cached.BELLOWS_POWER_SCALING);
            Direction facing = this.getDirection();

            //slope of animation. for particles and pushing entities
            float arg = (float)Math.PI*2*(((time-offset) / period)%1);
            float sin = MathHelper.sin(arg);
            float cos = MathHelper.cos(arg);
            final float dh = 1 / 16f;//0.09375f;
            this.height = dh * cos - dh;

            //client. particles
            if (this.world.isRemote) {
                if (this.world.rand.nextInt(2) == 0 && this.world.rand.nextFloat() < sin &&
                        !Block.hasEnoughSolidSide(this.world, this.pos.offset(facing), facing.getOpposite())) {
                    this.spawnParticles(this.world, this.pos);
                }
            }
            //server
            else{
                float range = ServerConfigs.cached.BELLOWS_RANGE;
                //push entities (only if pushing air)
                if ( sin> 0) {
                    this.pushEntities(facing,period,range);
                }

                BlockPos frontPos = this.pos.offset(facing);

                //speeds up furnaces
                if(time % 9 - (power/2) == 0) {
                    TileEntity te = world.getTileEntity(frontPos);
                    Block b = world.getBlockState(frontPos).getBlock();
                    ITag<Block> tag = BlockTags.getCollection().get(ModTags.BELLOWS_TICKABLE_TAG);
                    if (te instanceof ITickableTileEntity &&
                             tag!= null && b.isIn(tag)) {
                        ((ITickableTileEntity) te).tick();
                    }
                }

                //refresh fire blocks
                //update more frequently block closed to it
                //fire updates (previous random tick) at a minimum of 30 ticks
                int n = 0;
                for (int a = 0; a <= range; a++) {
                    if (time % (15 * (a + 1)) != 0) {
                        n = a;
                        break;
                    }
                }
                //only first 4 block will ultimately be kept active. this could change with random ticks if unlucky
                for (int i = 0; i < n; i++) {
                    BlockState fb = this.world.getBlockState(frontPos);
                    if (fb.getBlock() instanceof FireBlock) {
                        int age = fb.get(FireBlock.AGE);
                        if (age != 0) {
                            world.setBlockState(frontPos, fb.with(FireBlock.AGE,
                                    MathHelper.clamp(age - 7, 0, 15)), 4);
                        }
                    }
                    frontPos = frontPos.offset(facing);
                }

            }
        }
        //resets counter when powered off
        else{
            this.offset=0;
            if(this.height < 0)
                this.height = Math.min(this.height + 0.01f, 0);
        }
        if(this.height !=0){
            this.moveCollidedEntities();
        }
    }

    public boolean inLineOfSight(Entity entity, Direction facing) {
        int x = facing.getXOffset() * (MathHelper.floor(entity.getPosX()) - this.pos.getX());
        int y = facing.getYOffset() * (MathHelper.floor(entity.getPosY()) - this.pos.getY());
        int z = facing.getZOffset() * (MathHelper.floor(entity.getPosZ()) - this.pos.getZ());
        boolean flag = true;

        for(int i = 1; i < Math.abs(x + y + z); i++) {

            if(Block.hasEnoughSolidSide(this.world, this.pos.offset(facing, i), facing.getOpposite())) {
                flag = false;
            }
        }
        return flag;
    }

    public  void spawnParticles(World world, BlockPos pos) {
        Direction dir = this.getDirection();
        double xo = dir.getXOffset();
        double yo = dir.getYOffset();
        double zo = dir.getZOffset();
        double x = xo*0.5 + pos.getX() + 0.5 + (world.rand.nextFloat() - 0.5)/3d;
        double y = yo*0.5 + pos.getY() + 0.5 + (world.rand.nextFloat() - 0.5)/3d;
        double z = zo*0.5 + pos.getZ() + 0.5 + (world.rand.nextFloat() - 0.5)/3d;

        double vel = 0.125F + world.rand.nextFloat() * 0.2F;

        double velX = xo * vel;
        double velY = yo * vel;
        double velZ = zo * vel;

        world.addParticle(ParticleTypes.SMOKE, x, y, z, velX, velY, velZ);
    }

    public Direction getDirection() {
        return this.getBlockState().get(BellowsBlock.FACING);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.offset = compound.getLong("Offset");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putLong("Offset", this.offset);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }
}