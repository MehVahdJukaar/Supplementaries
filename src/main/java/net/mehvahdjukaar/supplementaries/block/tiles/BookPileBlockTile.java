package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class BookPileBlockTile extends ItemDisplayTile {

    public final boolean horizontal;

    public BookPileBlockTile() {
        this(false);
    }

    public BookPileBlockTile(boolean horizontal) {
        super(ModRegistry.BOOK_PILE_TILE.get(), 4);
        this.horizontal = horizontal;
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new StringTextComponent("block.supplementaries.book_pile");
    }
}
