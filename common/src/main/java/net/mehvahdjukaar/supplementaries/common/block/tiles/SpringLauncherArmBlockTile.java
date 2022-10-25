package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpringLauncherArmBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpringLauncherBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpringLauncherHeadBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

//TODO: broken on servers
public class SpringLauncherArmBlockTile extends BlockEntity {
    private int age;
    //maybe replace this with boolean?
    private double increment;
    private double offset;
    private double prevOffset;
    private int dx;
    private int dy;
    private int dz;

    public SpringLauncherArmBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SPRING_LAUNCHER_ARM_TILE.get(), pos, state);
        boolean extending = state.getValue(ModBlockProperties.EXTENDING);
        Direction dir = state.getValue(BlockStateProperties.FACING);

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
        Vec3i v = dir.getNormal();
        this.dx = v.getX();
        this.dy = v.getY();
        this.dz = v.getZ();
    }

    public int getAge() {
        return age;
    }

    public double getRenderOffset(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevOffset, this.offset);
    }

    //TODO: rewrite some of this old code
    public AABB getAdjustedBoundingBox() {
        return new AABB(worldPosition).move(this.dx * this.offset, this.dy * this.offset, this.dz * this.offset);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpringLauncherArmBlockTile tile) {
        boolean extending = state.getValue(SpringLauncherArmBlock.EXTENDING);
        if (level.isClientSide && extending) {

            double x = pos.getX() + 0.5 + tile.dx * tile.offset;
            double y = pos.getY() + tile.dy * tile.offset;
            double z = pos.getZ() + 0.5 + tile.dz * tile.offset;
            RandomSource random = level.random;
            for (int l = 0; l < 2; ++l) {
                double d0 = (x + random.nextFloat() - 0.5D);
                double d1 = (y + random.nextFloat() + 0.5D);
                double d2 = (z + random.nextFloat() - 0.5D);
                double d3 = (random.nextFloat() - 0.5D) * 0.05D;
                double d4 = (random.nextFloat() - 0.5D) * 0.05D;
                double d5 = (random.nextFloat() - 0.5D) * 0.05D;

                level.addParticle(ParticleTypes.CLOUD, d0, d1, d2, d3, d4, d5);
                //TODO:add swirl particle
            }
        }
        if (tile.age > 1) {
            tile.prevOffset = tile.offset;
            if (!level.isClientSide) {
                Direction dir = state.getValue(SpringLauncherArmBlock.FACING);
                if (extending) {
                    BlockState state1 = ModRegistry.SPRING_LAUNCHER_HEAD.get().defaultBlockState();
                    level.setBlock(pos, state1.setValue(SpringLauncherHeadBlock.FACING, dir), 3);
                } else {
                    BlockState _bs = ModRegistry.SPRING_LAUNCHER.get().defaultBlockState();
                    BlockPos behindPos = pos.relative(tile.getDirection().getOpposite());
                    BlockState oldState = level.getBlockState(behindPos);
                    if (_bs.setValue(SpringLauncherBlock.FACING, dir).setValue(SpringLauncherBlock.EXTENDED, true) == oldState) {
                        level.setBlock(behindPos, oldState.setValue(SpringLauncherBlock.EXTENDED, false), 3);
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        } else {
            tile.age ++;
            tile.prevOffset = tile.offset;
            tile.offset += tile.increment;
            if (extending) {
                AABB p_bb = tile.getAdjustedBoundingBox();
                List<Entity> list1 = level.getEntities(null, p_bb);
                if (!list1.isEmpty()) {
                    for (Entity entity : list1) {
                        if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                            Vec3 vec3d = entity.getDeltaMovement();
                            double d1 = vec3d.x;
                            double d2 = vec3d.y;
                            double d3 = vec3d.z;
                            double speed = CommonConfigs.Blocks.LAUNCHER_VEL.get();
                            if (tile.dx != 0) {
                                d1 = tile.dx * speed;
                            }
                            if (tile.dy != 0) {
                                d2 = tile.dy * speed;
                            }
                            if (tile.dz != 0) {
                                d3 = tile.dz * speed;
                            }
                            entity.setDeltaMovement(d1, d2, d3);
                            entity.hurtMarked = true;
                            tile.moveCollidedEntity(entity, p_bb);
                        }
                    }
                }
            }
        }
    }

    private void moveCollidedEntity(Entity entity, AABB aabb) {
        AABB boundingBox = entity.getBoundingBox();
        double dx = 0;
        double dy = 0;
        double dz = 0;
        switch (this.getDirection()) {
            default -> dy = 0;
            case UP -> dy = aabb.maxY - boundingBox.minY;
            case DOWN -> dy = aabb.minY - boundingBox.maxY;
            case NORTH -> dz = aabb.minZ - boundingBox.maxZ;
            case SOUTH -> dz = aabb.maxZ - boundingBox.minZ;
            case WEST -> dx = aabb.minX - boundingBox.maxX;
            case EAST -> dx = aabb.maxX - boundingBox.minX;
        }
        entity.move(MoverType.PISTON, new Vec3(dx, dy, dz));
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(SpringLauncherArmBlock.FACING);
    }

    public boolean getExtending() {
        return this.getBlockState().getValue(SpringLauncherArmBlock.EXTENDING);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.age = compound.getInt("Age");
        this.offset = compound.getDouble("Offset");
        this.prevOffset = compound.getDouble("PrevOffset");
        this.increment = compound.getDouble("Increment");
        this.dx = compound.getInt("Dx");
        this.dy = compound.getInt("Dy");
        this.dz = compound.getInt("Dz");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("Age", this.age);
        compound.putDouble("Offset", this.offset);
        compound.putDouble("PrevOffset", this.prevOffset);
        compound.putDouble("Increment", this.increment);
        compound.putInt("Dx", this.dx);
        compound.putInt("Dy", this.dy);
        compound.putInt("Dz", this.dz);
    }
/*
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //TODO: use new system
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
    */
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void retractOnFallOn() {
        this.age = 1;
        this.offset = -0.5;
    }
}