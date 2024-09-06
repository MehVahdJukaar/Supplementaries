package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GravelBricksBlock extends Block {

    private static final VoxelShape SHAPE_HACK = Block.box(0, 0.01, 0, 16, 16, 16);

    public GravelBricksBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(context instanceof EntityCollisionContext ec && ec.getEntity() instanceof Player){
            return SHAPE_HACK;
        }
        return Shapes.block();
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);
        if (!level.isClientSide && !entity.isSteppingCarefully() && fallDistance > 0.2
        && hasEnergyToBreak(entity)) {
            level.destroyBlock(pos, false, entity);
            if (level.getBlockEntity(pos) instanceof Container tile) {
                Containers.dropContents(level, pos, tile);
            }
        }
    }


    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if(!level.isClientSide) {
          // gg. we cant keep velocity into account. unless we want to have player authoritative over it as its velocity isnt synced all the times
          if (entity.yo <entity.getY() && hasEnergyToBreak(entity)) {
              level.destroyBlock(pos, false, entity);
              if (level.getBlockEntity(pos) instanceof Container tile) {
                  Containers.dropContents(level, pos, tile);
              }
          }
      }
        super.entityInside(state, level, pos, entity);
    }

    private static boolean hasEnergyToBreak(Entity entity) {
       return entity.getBoundingBox().getSize() > 0.5;
    }
}