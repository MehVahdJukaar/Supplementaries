package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;
import java.util.TreeMap;

public class FeatherBlock extends Block {

    protected static final VoxelShape COLLISION_SHAPE = Block.box(0, 0, 0, 16, 11, 16);

    private static final TreeMap<Float, VoxelShape> COLLISIONS = new TreeMap<Float, VoxelShape>(){{
        float y = (float) COLLISION_SHAPE.max(Direction.Axis.Y);

        float i = 0.0015f;
        //adds an extra lower one for lower key access
        put(y - i, VoxelShapes.box(0, 0, 0, 1, y, 1));

        while (y < 1) {
            put(y, VoxelShapes.box(0, 0, 0, 1, y, 1));
            i *= 1.131;
            y += i;
        }
        put(1f, VoxelShapes.block());
        put(0f, VoxelShapes.block());

    }};


    public FeatherBlock(Properties properties) {
        super(properties);
    }


    @Override
    public void fallOn(World world, BlockPos pos, Entity entity, float height) {
        if (!world.isClientSide) {
            if(height > 2) {
                //TODO: sound here
                world.playSound(null, pos, SoundEvents.WOOL_FALL, SoundCategory.BLOCKS, 1F, 0.9F);

            }
        }
        else{
            //world.addParticle(ParticleTypes.HEART, pos.getX(), pos.getY(), pos.getZ(), 0.0D, 0.0D, 0.0D);

        }
    }

    @Override
    public void updateEntityAfterFallOn(IBlockReader reader, Entity entity) { }


    private final VoxelShape COLLISION_CHECK_SHAPE = Block.box(0, 0, 0, 16, 16.1, 16);

    private boolean isColliding(Entity e, BlockPos pos) {
        if (e == null) return false;
        VoxelShape voxelshape = COLLISION_CHECK_SHAPE.move(pos.getX(), pos.getY(), pos.getZ());
        return VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(e.getBoundingBox()), IBooleanFunction.AND);
    }


    public static float randomBetween(Random random, float f, float g) {
        return random.nextFloat() * (g - f) + f;
    }


    @Override
    public void entityInside(BlockState state, World level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide) {
            if (!(entity instanceof LivingEntity) || ((LivingEntity) entity).getFeetBlockState().is(this)) {


                Random random = level.getRandom();
                boolean isMoving = entity.xOld != entity.getX() || entity.zOld != entity.getZ();
                if (isMoving && random.nextBoolean()) {
                    double dy = 0.04;
                    level.addParticle(Registry.FEATHER_PARTICLE.get(), entity.getX(), entity.getY(), entity.getZ(),
                            randomBetween(random, -1.0F, 1.0F) * 0.083333336F, dy, randomBetween(random, -1.0F, 1.0F) * 0.083333336F);
                }
            }
        }
    }

    @Override
    public ActionResultType use(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity p_225533_4_, Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
        world.addParticle(Registry.FEATHER_PARTICLE.get(), pos.getX()+2, pos.getY()+1, pos.getZ(),
                0, 0.05, 0);
        return ActionResultType.sidedSuccess(world.isClientSide);

    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, IBlockReader blockGetter, BlockPos blockPos, ISelectionContext collisionContext) {
        if (collisionContext instanceof EntitySelectionContext) {
            EntitySelectionContext entityCollisionContext = (EntitySelectionContext) collisionContext;
            Entity entity = entityCollisionContext.getEntity();
            if (entity instanceof LivingEntity) {

                float dy = (float) (entity.getY() - blockPos.getY());

                if(dy > 0) {
                    Float key = COLLISIONS.lowerKey(dy);
                    if(key!=null){
                        return COLLISIONS.getOrDefault(key, COLLISION_SHAPE);
                    }
                }
            }
        }

        return VoxelShapes.block();
    }


}
