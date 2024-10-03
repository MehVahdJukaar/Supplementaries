package net.mehvahdjukaar.supplementaries.mixins.neoforge.self;

import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FrameBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FrameBlock.class)
public abstract class SelfFrameMixin extends Block {

    protected SelfFrameMixin(Properties arg) {
        super(arg);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
            return tile.getHeldBlock().getEnchantPowerBonus(world, pos);
        }
        return 0;
    }

}
