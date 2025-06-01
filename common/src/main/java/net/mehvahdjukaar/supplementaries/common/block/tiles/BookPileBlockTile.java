package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.SpriteCoordinateUnExpander;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.BookModelVisuals;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.BookType;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManagerClient;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.EnchantRedesignCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

    private void displayRandomColoredBooks(int i, HolderLookup.Provider provider) {
        for (int j = 0; j < i; j++) {
            Item it;
            int r = rand.nextInt(10);
            if (r < 2) it = Items.ENCHANTED_BOOK;
            else if (r < 3) it = Items.WRITABLE_BOOK;
            else it = Items.BOOK;
            booksVisuals.add(new BookVisualData(it.getDefaultInstance(), this.worldPosition, j,
                    this.horizontal, provider, null));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        //dumb because the component thing always overrides the tile content even when not set
        NonNullList<ItemStack> itemsCopy = null;
        if (componentInput.get(DataComponents.CONTAINER) == null) {
            itemsCopy = this.getItems();
            this.setItems(NonNullList.withSize(4, ItemStack.EMPTY));
        }
        super.applyImplicitComponents(componentInput);
        this.setItems(itemsCopy);
    }

    @Override
    public void onItemRemoved(Player player, ItemStack stack, int slot) {
        super.onItemRemoved(player, stack, slot);
        int actualBookCount = (int) this.getItems().stream().filter(i -> !i.isEmpty()).count();
        if (actualBookCount == 0) {
            this.level.removeBlock(this.worldPosition, false);
        }
    }

    //called on change... too soon
    @Override
    public void updateTileOnInventoryChanged() {
        super.updateTileOnInventoryChanged();

        int actualBookCount = (int) this.getItems().stream().filter(i -> !i.isEmpty()).count();
        if (actualBookCount != this.getBlockState().getValue(BookPileBlock.BOOKS)) {
            if (actualBookCount == 0) {
                //Error?
                return;

            } else {
                //  shifts books. Assumes at most one has been removed
                // auto sets my block state when stuff changes
                //    consolidateBookPile();
                this.level.setBlock(this.worldPosition, this.getBlockState().setValue(BookPileBlock.BOOKS, actualBookCount), 2);
            }
        }
        this.enchantPower = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) continue;
            Item item = itemStack.getItem();
            BookType type = PlaceableBookManager.get(item, this.horizontal, level.registryAccess());
            if (type != null) {
                this.enchantPower += type.enchantPower();
            }
        }
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

        for (int index = 0; index < 4; index++) {
            ItemStack stack = this.getItem(index);
            if (stack.isEmpty()) break;
            var last = index == 0 ? null : this.booksVisuals.get(index - 1).type;
            this.booksVisuals.add(index, new BookVisualData(stack, this.worldPosition, index,
                    this.horizontal, level.registryAccess(), last));
        }

        if (booksVisuals.isEmpty()) {
            displayRandomColoredBooks(this.getBlockState().getValue(BookPileBlock.BOOKS), this.level.registryAccess());
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
    public static class BookVisualData {
        private final float yAngle;
        private final BookModelVisuals type;
        private final ItemStack stack;

        public BookVisualData(ItemStack bookStack, BlockPos pos, int index,
                              boolean isHorizontal,
                              HolderLookup.Provider provider,
                              @Nullable BookModelVisuals lastColor) {
            this.stack = bookStack;
            Random rand = new Random(pos.below(2).asLong());
            for (int j = 0; j < index; j++) rand.nextInt();
            this.yAngle = (float) (rand.nextInt(32) * Math.PI / 16);

            var possibleTypes = PlaceableBookManagerClient
                    .getValidModelsForBookItem(provider, stack, isHorizontal);
            List<BookModelVisuals> looksGood = new ArrayList<>();
            for (var type : possibleTypes) {
                if ((lastColor == null || type.looksGoodNextTo(lastColor)) && type != lastColor) {
                    looksGood.add(type);
                }
            }
            if (looksGood.isEmpty()) {
                looksGood = possibleTypes;
            }
            this.type = looksGood.get(rand.nextInt(looksGood.size()));
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

        public ModelResourceLocation getModel() {
            return this.type.model();
        }

    }

    public record BooksList(List<BookVisualData> books) {
        public BooksList() {
            this(new ArrayList<>());
        }

        private void add(BookVisualData visualBook) {
            books.add(visualBook);
        }

        private void add(int i, BookVisualData visualBook) {
            books.add(i, visualBook);
        }

        private void clear() {
            books.clear();
        }

        public boolean isEmpty() {
            return books.isEmpty();
        }

        public BookVisualData get(int i) {
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
