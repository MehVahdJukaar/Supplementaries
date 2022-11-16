package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PresentBlock extends AbstractPresentBlock {

    public PresentBlock(DyeColor color, Properties properties) {
        super(color, properties);
    }

    @Nullable
    @Override
    public Item changeItemColor(@Nullable DyeColor color) {
        var c = ModRegistry.PRESENTS.get(color);
        if (c != null) return c.get().asItem();
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PresentBlockTile(pPos, pState);
    }

}
