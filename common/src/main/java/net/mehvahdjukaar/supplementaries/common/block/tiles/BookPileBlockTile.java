package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.SpriteCoordinateUnExpander;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.BookType;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.EnchantRedesignCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookPileBlockTile extends ItemDisplayTile implements IExtraModelDataProvider {

    public final boolean horizontal;
    private float enchantPower = 0;

    //client only
    public final BooksList booksVisuals = new BooksList();
    public static final ModelDataKey<BooksList> BOOKS_KEY = ModBlockProperties.BOOKS_KEY;

    public BookPileBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, false);
    }

    public BookPileBlockTile(BlockPos pos, BlockState state, boolean horizontal) {
        super(ModRegistry.BOOK_PILE_TILE.get(), pos, state, 4);
        this.horizontal = horizontal;
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(BOOKS_KEY, booksVisuals);
    }

    private static final RandomSource rand = RandomSource.create();

    private void displayRandomColoredBooks(int i) {
        for (int j = 0; j < i; j++) {
            Item it;
            int r = rand.nextInt(10);
            if (r < 2) it = Items.ENCHANTED_BOOK;
            else if (r < 3) it = Items.WRITABLE_BOOK;
            else it = Items.BOOK;
            ArrayList<BookType> col = PlaceableBookManager.getByItem(it.getDefaultInstance());
            booksVisuals.add(new VisualBook(it.getDefaultInstance(), this.worldPosition, j,
                    col, null));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putFloat("EnchantPower", this.enchantPower);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.enchantPower = tag.getFloat("EnchantPower");
        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        int b = (int) this.getItems().stream().filter(i -> !i.isEmpty()).count();
        if (b != this.getBlockState().getValue(BookPileBlock.BOOKS)) {
            if (b == 0) {
                if (this.lootTable == null) {
                    this.level.removeBlock(this.worldPosition, false);
                } else {
                    //loot table mode
                    return;
                }
            } else {
                //shifts books. Assumes at most one has been removed
             //   consolidateBookPile();
              //  this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BookPileBlock.BOOKS, b), 2);
            }
        }
        this.enchantPower = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) continue;
            Item item = itemStack.getItem();
            if (CompatHandler.QUARK && CompatObjects.TOME.get() == item)
                this.enchantPower += (CommonConfigs.Tweaks.BOOK_POWER.get() / 4f) * 2;
            else if (item == Items.ENCHANTED_BOOK)
                this.enchantPower += CommonConfigs.Tweaks.ENCHANTED_BOOK_POWER.get() / 4f;
            else this.enchantPower += CommonConfigs.Tweaks.BOOK_POWER.get() / 4f;
        }
    }

    @Override
    public void updateTileOnInventoryChanged() {
        super.updateTileOnInventoryChanged();
    }

    private void consolidateBookPile() {
        boolean prevEmpty = false;
        for (int i = 0; i < 4; i++) {
            var it = this.getItem(i);
            if (it.isEmpty()) prevEmpty = true;
            else if (prevEmpty) {
                this.getItems().set(i - 1, it);
                this.getItems().set(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void updateClientVisualsOnLoad() {
        this.booksVisuals.clear();
        List<BookType> colors = new ArrayList<>();
        for (var v : ClientConfigs.Tweaks.BOOK_COLORS.get()) {
            BookType byName = PlaceableBookManager.getByName(v);
            if (!colors.contains(byName)) colors.add(byName);
        }
        for (int index = 0; index < 4; index++) {
            ItemStack stack = this.getItem(index);
            if (stack.isEmpty()) break;
            BookType last = index == 0 ? null : this.booksVisuals.get(index - 1).type;
            this.booksVisuals.add(index, new VisualBook(stack, this.worldPosition, index, colors, last));
        }

        if (booksVisuals.isEmpty()) {
            displayRandomColoredBooks(this.getBlockState().getValue(BookPileBlock.BOOKS));
        }
    }

    public float getEnchantPower() {
        return enchantPower;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.supplementaries.book_pile");
    }

    //only client
    public static class VisualBook {
        private final float yAngle;
        private final BookType type;
        private final ItemStack stack;

        public VisualBook(ItemStack bookStack, BlockPos pos, int index, List<BookType> colors, @Nullable BookType lastColor) {
            this.stack = bookStack;
            Random rand = new Random(pos.below(2).asLong());
            for (int j = 0; j < index; j++) rand.nextInt();
            Item item = bookStack.getItem();
            this.yAngle = (float) (rand.nextInt(32) * Math.PI / 16);

            if (item instanceof BookItem) {
                if (lastColor == null) {
                    if(colors.isEmpty()){
                        Supplementaries.error();
                        this.type = PlaceableBookManager.getByName("brown");
                        return;
                    }
                    this.type = colors.get(rand.nextInt(colors.size()));
                } else {
                    List<BookType> c = colors.stream().filter(b -> b.looksGoodNextTo(lastColor)).toList();
                    if(c.isEmpty()) {
                        Supplementaries.error();
                        this.type = lastColor;
                    } else {
                        this.type = c.get(rand.nextInt(c.size()));
                    }
                }
                colors.remove(this.type);
            } else {
                var possibleTypes = PlaceableBookManager.getByItem(bookStack);
                if(possibleTypes.isEmpty()){
                    Supplementaries.error();
                    this.type = PlaceableBookManager.getByName("brown");
                    return;
                }
                this.type = possibleTypes.get(rand.nextInt(possibleTypes.size()));
            }
        }

        @SuppressWarnings("ConstantConditions")
        public VertexConsumer getBuilder(MultiBufferSource buffer) {
            if (this.type.hasGlint() && ClientConfigs.Tweaks.BOOK_GLINT.get()) {
                VertexConsumer foilBuilder = null;
                if (CompatHandler.ENCHANTEDBOOKREDESIGN) {
                    foilBuilder = EnchantRedesignCompat.getBookColoredFoil(this.stack, buffer);
                }
                if (foilBuilder == null) {
                    foilBuilder = new SpriteCoordinateUnExpander(buffer.getBuffer(RenderType.entityGlint()),
                            ModMaterials.BOOK_GLINT_MATERIAL.sprite());
                }
                return foilBuilder;
            }
            return null;// buffer.getBuffer(RenderType.cutout());
        }

        public float getAngle() {
            return yAngle;
        }

        public BookType getType() {
            return type;
        }

    }

    public static final List<String> DEFAULT_COLORS = List.of("brown", "orange", "yellow",
            "red", "green", "lime", "cyan", "blue", "purple");

    public record BooksList(List<VisualBook> books) {
        public BooksList() {
            this(new ArrayList<>());
        }

        public void add(VisualBook visualBook) {
            books.add(visualBook);
        }

        public void add(int i, VisualBook visualBook) {
            books.add(i, visualBook);
        }

        public void clear() {
            books.clear();
        }

        public boolean isEmpty() {
            return books.isEmpty();
        }

        public VisualBook get(int i) {
            return books.get(i);
        }

        public int size() {
            return books.size();
        }
    }


    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canOpen(Player player) {
        return false;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}
