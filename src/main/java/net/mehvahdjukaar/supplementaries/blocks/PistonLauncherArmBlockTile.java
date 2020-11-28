package net.mehvahdjukaar.supplementaries.blocks;


import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import java.util.List;
import java.util.Random;

public class PistonLauncherArmBlockTile extends TileEntity implements ITickableTileEntity {
    public int age = 0;
    private double increment = 0;
    public double offset = 0;
    public double prevOffset = 0;
    private int dx = 0;
    private int dy = 0;
    private int dz = 0;
    protected final Random rand = new Random();
    public PistonLauncherArmBlockTile() {
        super(Registry.PISTON_LAUNCHER_ARM_TILE);
        //this.setParameters();
    }
    public PistonLauncherArmBlockTile(boolean extending, Direction dir) {
        this();
        this.setParameters(extending, dir);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 96;
    }

    //TODO: rewrite some of this old code
    public AxisAlignedBB getAdjustedBoundingBox() {
        return new AxisAlignedBB(pos).offset(this.dx * this.offset, this.dy * this.offset, this.dz * this.offset);
    }

    public void tick() {
        if (this.world.isRemote()) {
            if (this.getExtending()) {
                double x = this.pos.getX() + 0.5 + this.dx * this.offset;
                double y = this.pos.getY() + this.dy * this.offset;
                double z = this.pos.getZ() + 0.5 + this.dz * this.offset;
                Random random = this.rand;
                for (int l = 0; l < 2; ++l) {
                    double d0 = (x + random.nextFloat() - 0.5D);
                    double d1 = (y + random.nextFloat() + 0.5D);
                    double d2 = (z + random.nextFloat() - 0.5D);
                    double d3 = (random.nextFloat() - 0.5D) * 0.05D;
                    double d4 = (random.nextFloat() - 0.5D) * 0.05D;
                    double d5 = (random.nextFloat() - 0.5D) * 0.05D;
                    // world.addParticle(ParticleTypes.POOF, d0, d1, d2, d3, d4, d5);
                    this.world.addParticle(ParticleTypes.CLOUD, d0, d1, d2, d3, d4, d5);
                }
            }
        }
        if (this.age > 1) {
            this.prevOffset = this.offset;
            if (!this.world.isRemote()) {
                if (this.getExtending()) {
                    BlockState _bs = Registry.PISTON_LAUNCHER_HEAD.getDefaultState();
                    world.setBlockState(pos, _bs.with(PistonLauncherHeadBlock.FACING, this.getDirection()), 3);
                } else {
                    BlockState _bs = Registry.PISTON_LAUNCHER.getDefaultState();
                    BlockPos _bp = pos.offset(this.getDirection().getOpposite());
                    BlockState oldstate = world.getBlockState(_bp);
                    if (_bs.with(PistonLauncherBlock.FACING, this.getDirection()).with(PistonLauncherBlock.EXTENDED, true) == oldstate) {
                        world.setBlockState(_bp, oldstate.with(PistonLauncherBlock.EXTENDED, false), 3);
                    }
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
            }
        } else {
            this.age = this.age + 1;
            this.prevOffset = this.offset;
            this.offset += this.increment;
            if (this.getExtending()) {
                AxisAlignedBB p_bb = this.getAdjustedBoundingBox();
                List<Entity> list1 = this.world.getEntitiesWithinAABBExcludingEntity(null, p_bb);
                if (!list1.isEmpty()) {
                    for (Entity entity : list1) {
                        if (entity.getPushReaction() != PushReaction.IGNORE) {
                            Vector3d vec3d = entity.getMotion();
                            double d1 = vec3d.x;
                            double d2 = vec3d.y;
                            double d3 = vec3d.z;
                            double speed = ServerConfigs.cached.LAUNCHER_VEL;
                            if (dx != 0) {
                                d1 = this.dx * speed;
                            }
                            if (dy != 0) {
                                d2 = this.dy * speed;
                            }
                            if (dz != 0) {
                                d3 = this.dz * speed;
                            }
                            entity.setMotion(d1, d2, d3);
                            entity.velocityChanged = true;
                            moveCollidedEntity(entity, p_bb);
                        }
                    }
                }
            }
        }
    }

    private void moveCollidedEntity(Entity entity, AxisAlignedBB p_bb) {
        AxisAlignedBB e_bb = entity.getBoundingBox();
        double dx = 0;
        double dy = 0;
        double dz = 0;
        switch (this.getDirection()) {
            default :
                dy = 0;
                break;
            case UP :
                dy = p_bb.maxY - e_bb.minY;
                break;
            case DOWN :
                dy = p_bb.minY - e_bb.maxY;
                break;
            case NORTH :
                dz = p_bb.minZ - e_bb.maxZ;
                break;
            case SOUTH :
                dz = p_bb.maxZ - e_bb.minZ;
                break;
            case WEST :
                dx = p_bb.minX - e_bb.maxX;
                break;
            case EAST :
                dx = p_bb.maxX - e_bb.minX;
                break;
        }
        entity.move(MoverType.PISTON, new Vector3d(dx, dy, dz));
    }

    private void setParameters(boolean extending, Direction dir){
        // boolean extending = this.getExtending();
        //Direction dir = this.getDirection();
        this.age = 0;
        if (extending) {
            this.increment = 0.5;
            this.offset = -1;
            this.prevOffset = -1;
        } else {
            this.increment = -0.5;
            this.offset = 0;
            this.prevOffset = 0;
        }
        Vector3i v = dir.getDirectionVec();
        this.dx = v.getX();
        this.dy = v.getY();
        this.dz = v.getZ();
    }

    public Direction getDirection() {
        return this.getBlockState().get(PistonLauncherArmBlock.FACING);
    }

    public boolean getExtending() {
        return this.getBlockState().get(PistonLauncherArmBlock.EXTENDING);
    }

    public int getAge() {
        return this.age;
    }

    public double getOffset() {
        return this.offset;
    }

    public double getPrevOffset() {
        return this.prevOffset;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.age = compound.getInt("age");
        this.increment = compound.getDouble("increment");
        this.offset = compound.getDouble("offset");
        this.prevOffset = compound.getDouble("prevOffset");
        this.dx = compound.getInt("dx");
        this.dy = compound.getInt("dy");
        this.dz = compound.getInt("dz");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("age", this.age);
        compound.putDouble("increment", this.increment);
        compound.putDouble("offset", this.offset);
        compound.putDouble("prevOffset", this.prevOffset);
        compound.putInt("dx", this.dx);
        compound.putInt("dy", this.dy);
        compound.putInt("dz", this.dz);
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