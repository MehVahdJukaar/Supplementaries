package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.BubbleBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BubbleBlockTile extends BlockEntity {

    private int age = 0;
    public float prevScale = 0.1f;
    public float scale = 0.1f;

    public BubbleBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BUBBLE_BLOCK_TILE.get(), pos, state);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BubbleBlockTile tile) {
        if(pLevel.isClientSide) {
            tile.prevScale = tile.scale;
            tile.scale = Math.min(1, tile.scale + 0.001f + (1 - tile.scale) * 0.4f);
        }else {
            tile.age++;
            if (tile.age > 60 * 20 && pLevel.random.nextInt(500) == 0) {
                BubbleBlock.breakBubble((ServerLevel) pLevel, pPos);
            }
        }
    }
}
