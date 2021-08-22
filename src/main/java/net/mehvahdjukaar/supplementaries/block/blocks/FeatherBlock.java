package net.mehvahdjukaar.supplementaries.block.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class FeatherBlock extends Block {

    protected static final VoxelShape COLLISION_SHAPE = Block.box(0,0,0,16,12,16);


    public FeatherBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(World world, BlockPos pos, Entity entity, float height) {
        if(world.isClientSide){
            world.addParticle(ParticleTypes.HEART, pos.getX(), pos.getY(), pos.getZ(), 0.0D, 0.0D, 0.0D);

        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        if(context.isAbove(COLLISION_SHAPE, pos,true) &&
                (context instanceof EntitySelectionContext && isColliding(context.getEntity(), pos))) return COLLISION_SHAPE;
        return VoxelShapes.block();
    }

    private final VoxelShape COLLISION_CHECK_SHAPE = Block.box(0,0,0,16,16.1,16);
    private boolean isColliding(Entity e, BlockPos pos) {
        if(e == null) return false;
        VoxelShape voxelshape = COLLISION_CHECK_SHAPE.move(pos.getX(), pos.getY(), pos.getZ());
        return VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(e.getBoundingBox()), IBooleanFunction.AND);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if(world.isClientSide){
            world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, state), pos.getX(), pos.getY(), pos.getZ(), 0.0D, 0.0D, 0.0D);

        }
        Vector3d v = entity.getDeltaMovement();
        if(v.y<0){
            entity.setDeltaMovement(v.x,v.y*0,v.z);
        }
    }


}
