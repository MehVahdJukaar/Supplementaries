package net.mehvahdjukaar.supplementaries.integration.quark;

import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
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
            if(entity instanceof Player player && player.isCreative())return;
            if(entity instanceof LivingEntity livingEntity && entity.isAlive()) {
                AABB entityBB = entity.getBoundingBox();
                if (pistonBB.intersects(entityBB)) {
                    //apply potions using quark moving tiles
                    if (CompatHandler.quark) {
                        //get tile
                        if (QuarkPistonPlugin.getMovingTile(pos, world) instanceof BambooSpikesBlockTile tile) {
                            //apply effects
                            if(tile.interactWithEntity(livingEntity,world)){
                                //change blockstate if empty
                                if(movingTile instanceof IBlockHolder te) {
                                    //remove last charge and set new blockState
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
