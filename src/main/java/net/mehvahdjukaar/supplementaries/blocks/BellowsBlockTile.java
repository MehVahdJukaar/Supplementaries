package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class BellowsBlockTile extends TileEntity implements ITickableTileEntity {
    private static final int RANGE = 5;

    public float height = 0;
    public float prevHeight = 0;
    public int counter = 0;
    public float speed = 1;
    public BellowsBlockTile() {
        super(Registry.BELLOWS_TILE.get());
    }


    public void tick() {
        boolean powered = this.getBlockState().get(BellowsBlock.POWERED);

        if (this.world != null && this.world.isRemote) {
            this.prevHeight = this.height;
            if(powered){

                if(this.world.rand.nextInt(2) == 0 &&
                        this.world.rand.nextFloat() < MathHelper.sin((float)this.counter / 13f))
                    this.spawnParticles(this.world, this.pos);


                //float i = this.world.getDayTime() ;
                float dh = 0.09375f;
                this.height = dh* MathHelper.cos((float)this.counter / 13f) - dh;
                this.counter++;

            }
            else if(this.height <0){
                this.counter=0;
                this.height =Math.min(this.height+0.01f,0);
            }
        }
        //server
        else if(powered){

            Direction facing = this.getDirection();


            final List<Entity> list = this.world.getEntitiesWithinAABB(Entity.class, this.getDirectionBB());

            for(final Entity entity : list) {

                if(!this.isPathClear(entity, facing))
                    continue;

                double velocity = 0.1; // Affects acceleration
                double threshholdVelocity = 2; // Affects max speed
                velocity *= this.speed;

                if(entity instanceof ItemEntity) {
                    threshholdVelocity *= 1.8D;
                    velocity *= 1.3D;
                }

                if(entity instanceof PlayerEntity) {
                    if(((PlayerEntity)entity).abilities.isFlying)
                        continue;
                }

                if(entity instanceof MinecartEntity)
                    velocity *= 0.5D;

                if((entity instanceof FallingBlockEntity) && facing == Direction.UP)
                    velocity = 0.0D;


                if(facing == Direction.UP) {
                    threshholdVelocity *= 0.5D;
                }
                //TODO: make velocity dependant on distance from block
/*
                if(Math.abs(entity.getMotion().getCoordinate(facing.getAxis())) < threshholdVelocity)
                    entity.setMotion(entity.getMotion().add(facing.getXOffset() * velocity, facing.getYOffset() * velocity, facing.getZOffset() * velocity));
*/
            }
        }
    }

    public boolean isPathClear(final Entity entity, final Direction facing) {
        final int x = facing.getXOffset() * (MathHelper.floor(entity.getPosX()) - this.pos.getX());
        final int y = facing.getYOffset() * (MathHelper.floor(entity.getPosY()) - this.pos.getY());
        final int z = facing.getZOffset() * (MathHelper.floor(entity.getPosZ()) - this.pos.getZ());
        boolean flag = true;

        for(int l2 = 1; l2 < Math.abs(x + y + z); l2++) {

            if(Block.hasEnoughSolidSide(this.world, this.pos.offset(facing, l2), facing.getOpposite())) {
                flag = false;
            }
        }

        return flag;
    }


    public AxisAlignedBB getDirectionBB() {
        final Direction facing = this.getDirection();

        BlockPos endPos = this.pos.offset(facing, MathHelper.floor(RANGE));
        if(facing == Direction.WEST)
            endPos = endPos.add(0, 1, 1);
        else if(facing == Direction.NORTH)
            endPos = endPos.add(1, 1, 0);

        if(facing == Direction.EAST)
            endPos = endPos.add(1, 1, 1);
        else if(facing == Direction.SOUTH)
            endPos = endPos.add(1, 1, 1);

        if(facing == Direction.UP)
            endPos = endPos.add(1, 1, 1);
        else if(facing == Direction.DOWN)
            endPos = endPos.add(1, 0, 1);

        return new AxisAlignedBB(this.pos, endPos);
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
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
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