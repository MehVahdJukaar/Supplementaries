package net.mehvahdjukaar.supplementaries.compat.quark;

import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BambooSpikesPistonMovement {
    //called by mixin code
    public static void tick(Level world, BlockPos pos, AABB pistonBB, boolean sameDir, BlockEntity movingTile){
        List<Entity> list = world.getEntities(null, pistonBB);
        for(Entity entity : list){
            if(entity instanceof Player && ((Player) entity).isCreative())return;
            if(entity instanceof LivingEntity && entity.isAlive()) {
                AABB entityBB = entity.getBoundingBox();
                if (pistonBB.intersects(entityBB)) {
                    //apply potions using quark moving tiles
                    if (CompatHandler.quark && world!=null) {
                        //get tile
                        BlockEntity tile = QuarkPistonPlugin.getMovingTile(pos, world);
                        if (tile instanceof BambooSpikesBlockTile) {
                            //apply effects
                            if(((BambooSpikesBlockTile) tile).interactWithEntity(((LivingEntity) entity),world)){
                                if(movingTile instanceof IBlockHolder) {
                                    //remove last charge and set new blockState
                                    IBlockHolder te = ((IBlockHolder) movingTile);
                                    BlockState state = te.getHeldBlock();
                                    if(state.getBlock() instanceof BambooSpikesBlock) {
                                        te.setHeldBlock(state.setValue(BambooSpikesBlock.TIPPED,false));
                                    }
                                }
                            }
                            //update tile entity in its list
                            QuarkPistonPlugin.updateMovingTIle(pos,world,tile);
                        }
                    }

                    entity.hurt(CommonUtil.SPIKE_DAMAGE, sameDir ? 3 : 1);

                }
            }
        }

    }

}
