package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.item.BookItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class BookPileBlockTile extends ItemDisplayTile {

    public final boolean horizontal;
    private float enchantPower = 0;

    //TODO: add color

    public BookPileBlockTile() {
        this(false);
    }

    public BookPileBlockTile(boolean horizontal) {
        super(ModRegistry.BOOK_PILE_TILE.get(), 4);
        this.horizontal = horizontal;
    }

    @Override
    public void updateTileOnInventoryChanged() {
        int b = (int) this.getItems().stream().filter(i -> !i.isEmpty()).count();
        if (b != this.getBlockState().getValue(BookPileBlock.BOOKS)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BookPileBlock.BOOKS, b), 2);
        }
        this.enchantPower = 0;
        for (int i = 0; i < 4; i++) {
            Item item = this.getItem(i).getItem();
            if (item instanceof BookItem) this.enchantPower += ServerConfigs.cached.BOOK_POWER/4f;
            else if (CompatHandler.quark && QuarkPlugin.isTome(item)) this.enchantPower += (ServerConfigs.cached.BOOK_POWER/4f) * 2;
            else if (item instanceof EnchantedBookItem) this.enchantPower += ServerConfigs.cached.ENCHANTED_BOOK_POWER/4f;
        }
    }

    public float getEnchantPower() {
        return enchantPower;
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new StringTextComponent("block.supplementaries.book_pile");
    }
}
