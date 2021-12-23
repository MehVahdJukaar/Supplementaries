package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.BubbleBlock;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        if (pLevel.isClientSide) {
            tile.prevScale = tile.scale;
            tile.scale = Math.min(1, tile.scale + 0.001f + (1 - tile.scale) * ClientConfigs.cached.BUBBLE_BLOCK_GROW_SPEED);
        } else {
            int lifetime = ServerConfigs.cached.BUBBLE_LIFETIME;
            if (lifetime == 10000) return;
            tile.age++;
            if ((tile.age + 5) % 20 == 0) {
                for (Direction d : Direction.values()) {
                    if (tile.level.getBlockState(tile.getBlockPos().relative(d)).is(ModRegistry.SOAP_BLOCK.get())) {
                        tile.age = 0;
                        return;
                    }
                }
            }
            if (tile.age > lifetime && pLevel.random.nextInt(500) == 0) {
                BubbleBlock.breakBubble((ServerLevel) pLevel, pPos);
            }
        }
    }
}
