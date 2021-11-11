package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.blocks.PistonLauncherArmBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.SpringLauncherBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.SpringLauncherHeadBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
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
    //maybe replace this with boolean?
    private double increment = 0;
    public double offset = 0;
    public double prevOffset = 0;
    private int dx = 0;
    private int dy = 0;
    private int dz = 0;
    protected final Random rand = new Random();
    public PistonLauncherArmBlockTile() {
        super(ModRegistry.PISTON_LAUNCHER_ARM_TILE.get());
        //this.setParameters();
    }

    //TODO rewrite this old code
    public PistonLauncherArmBlockTile(boolean extending, Direction dir) {
        this();
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
        Vector3i v = dir.getNormal();
        this.dx = v.getX();
        this.dy = v.getY();
        this.dz = v.getZ();
    }

    @Override
    public double getViewDistance() {
        return 96;
    }

    //TODO: rewrite some of this old code
    public AxisAlignedBB getAdjustedBoundingBox() {
        return new AxisAlignedBB(worldPosition).move(this.dx * this.offset, this.dy * this.offset, this.dz * this.offset);
    }

    public void tick() {
        if (this.level.isClientSide()) {
            if (this.getExtending()) {
                double x = this.worldPosition.getX() + 0.5 + this.dx * this.offset;
                double y = this.worldPosition.getY() + this.dy * this.offset;
                double z = this.worldPosition.getZ() + 0.5 + this.dz * this.offset;
                Random random = this.rand;
                for (int l = 0; l < 2; ++l) {
                    double d0 = (x + random.nextFloat() - 0.5D);
                    double d1 = (y + random.nextFloat() + 0.5D);
                    double d2 = (z + random.nextFloat() - 0.5D);
                    double d3 = (random.nextFloat() - 0.5D) * 0.05D;
                    double d4 = (random.nextFloat() - 0.5D) * 0.05D;
                    double d5 = (random.nextFloat() - 0.5D) * 0.05D;
                    // world.addParticle(ParticleTypes.POOF, d0, d1, d2, d3, d4, d5);
                    this.level.addParticle(ParticleTypes.CLOUD, d0, d1, d2, d3, d4, d5);
                    //TODO:add swirl particle
                    //this.world.addParticle(ParticleTypes.ENTITY_EFFECT, d0, d1, d2, d3, d4, d5);
                }
            }
        }
        if (this.age > 1) {
            this.prevOffset = this.offset;
            if (!this.level.isClientSide()) {
                if (this.getExtending()) {
                    BlockState _bs = ModRegistry.SPRING_LAUNCHER_HEAD.get().defaultBlockState();
                    level.setBlock(worldPosition, _bs.setValue(SpringLauncherHeadBlock.FACING, this.getDirection()), 3);
                } else {
                    BlockState _bs = ModRegistry.SPRING_LAUNCHER.get().defaultBlockState();
                    BlockPos _bp = worldPosition.relative(this.getDirection().getOpposite());
                    BlockState oldState = level.getBlockState(_bp);
                    if (_bs.setValue(SpringLauncherBlock.FACING, this.getDirection()).setValue(SpringLauncherBlock.EXTENDED, true) == oldState) {
                        level.setBlock(_bp, oldState.setValue(SpringLauncherBlock.EXTENDED, false), 3);
                    }
                    level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        } else {
            this.age = this.age + 1;
            this.prevOffset = this.offset;
            this.offset += this.increment;
            if (this.getExtending()) {
                AxisAlignedBB p_bb = this.getAdjustedBoundingBox();
                List<Entity> list1 = this.level.getEntities(null, p_bb);
                if (!list1.isEmpty()) {
                    for (Entity entity : list1) {
                        if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                            Vector3d vec3d = entity.getDeltaMovement();
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
                            entity.setDeltaMovement(d1, d2, d3);
                            entity.hurtMarked = true;
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

    public Direction getDirection() {
        return this.getBlockState().getValue(PistonLauncherArmBlock.FACING);
    }

    public boolean getExtending() {
        return this.getBlockState().getValue(PistonLauncherArmBlock.EXTENDING);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.age = compound.getInt("Age");
        this.offset = compound.getDouble("Offset");
        this.prevOffset = compound.getDouble("PrevOffset");
        this.increment = compound.getDouble("Increment");
        this.dx = compound.getInt("Dx");
        this.dy = compound.getInt("Dy");
        this.dz = compound.getInt("Dz");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("Age", this.age);
        compound.putDouble("Offset", this.offset);
        compound.putDouble("PrevOffset", this.prevOffset);
        compound.putDouble("Increment", this.increment);
        compound.putInt("Dx", this.dx);
        compound.putInt("Dy", this.dy);
        compound.putInt("Dz", this.dz);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }
}