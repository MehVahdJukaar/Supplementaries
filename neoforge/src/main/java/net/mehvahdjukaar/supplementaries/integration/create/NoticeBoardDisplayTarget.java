package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.CreateLang;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class NoticeBoardDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        BlockEntity te = context.getTargetBlockEntity();
        if (!(te instanceof NoticeBoardBlockTile tile)) return;

        ItemStack book = tile.getDisplayedItem();
        if (book.isEmpty()) return;

        if (book.is(Items.WRITABLE_BOOK)) {
            book = signBook(book);
        }
        if (!book.is(Items.WRITTEN_BOOK)) return;

        WrittenBookContent content = book.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);
        List<Filterable<Component>> pages = new ArrayList<>(content.pages());
        boolean changed = false;

        for (int i = 0; i - line < text.size() && i < 50; ++i) {
            if (pages.size() <= i) {
                pages.add(Filterable.passThrough(i < line ? Component.empty() : text.get(i - line)));
            } else if (i >= line) {
                if (i - line == 0) {
                    reserve(i, tile, context);
                }
                if (i - line > 0 && this.isReserved(i - line, tile, context)) {
                    break;
                }
                pages.set(i, Filterable.passThrough(text.get(i - line)));
            }
            changed = true;
        }

        if (!changed) return;

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content.withReplacedPages(pages));
        tile.setDisplayedItem(book);
        tile.setChanged();
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(50, 256, this);
    }

    @Override
    public Component getLineOptionText(int line) {
        return CreateLang.translateDirect("display_target.page", line + 1);
    }

    @Override
    public boolean requiresComponentSanitization() {
        return true;
    }

    private static ItemStack signBook(ItemStack book) {
        ItemStack written = new ItemStack(Items.WRITTEN_BOOK);
        WritableBookContent contents = book.getOrDefault(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY);
        List<Filterable<Component>> pages = contents.pages().stream()
                .map(p -> p.<Component>map(Component::literal))
                .toList();
        written.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(Filterable.passThrough("Printed Book"), "Data Gatherer", 0, pages, true));
        return written;
    }
}
