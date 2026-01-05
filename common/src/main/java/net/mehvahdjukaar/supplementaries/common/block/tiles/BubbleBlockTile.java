package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.BubbleBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BubbleBlockTile extends BlockEntity {

    private int age = 0;
    private float prevScale = 0.1f;
    private float scale = 0.1f;

    public BubbleBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BUBBLE_BLOCK_TILE.get(), pos, state);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, BubbleBlockTile tile) {
        if (pLevel.isClientSide) {
            tile.prevScale = tile.scale;
            tile.scale = (float) Math.min(1, tile.scale + 0.001f + (1 - tile.scale) * ClientConfigs.Blocks.BUBBLE_BLOCK_GROW_SPEED.get());
        } else {
            int lifetime = CommonConfigs.Tools.BUBBLE_LIFETIME.get();
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
                BubbleBlock.breakBubble(pLevel, pPos, pState);
            }
        }
    }

    public float getScale(float partialTicks) {
        return Mth.lerp(partialTicks, prevScale, scale);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("age", age);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.age = pTag.getInt("age");
    }
}
