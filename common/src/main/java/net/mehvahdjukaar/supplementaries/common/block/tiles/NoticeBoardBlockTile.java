package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.IMapDisplay;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.NoticeBoardContainerMenu;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CCCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.ExposureCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NoticeBoardBlockTile extends ItemDisplayTile implements Nameable, IMapDisplay, ITextHolderProvider {

    //just used for color
    private final TextHolder textHolder;
    private boolean isWaxed = false;
    private int pageIndex = 0;
    private int maxPageIndex = 0;

    @Nullable
    private UUID playerWhoMayEdit;

    //client stuff
    private Filterable<String> text = null;
    private float fontScale = 1;
    private List<FormattedCharSequence> cachedPageLines = Collections.emptyList();
    //used to tell renderer when it has to slit new line(have to do it there cause i need fontrenderer function)
    private boolean needsVisualRefresh = true;
    private Material cachedPattern = null;

    private boolean isNormalItem = false;

    public NoticeBoardBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.NOTICE_BOARD_TILE.get(), pos, state);
        this.textHolder = new TextHolder(1, 90);
    }

    //refreshTextures blockState and plays sound. server side
    @Override
    public void updateTileOnInventoryChanged() {

        boolean shouldHaveBook = !this.getDisplayedItem().isEmpty();

        BlockState state = this.getBlockState();
        if (state.getValue(BlockStateProperties.HAS_BOOK) != shouldHaveBook) {
            this.level.setBlock(this.worldPosition, state.setValue(BlockStateProperties.HAS_BOOK, shouldHaveBook), 2);
            if (shouldHaveBook) {
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.85F);
            } else {
                this.pageIndex = 0;
                this.maxPageIndex = 0;
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.50F);
            }
        }
    }

    public float getPageProgress() {
        return maxPageIndex <= 0 ? 0 : (int) (((float) pageIndex / (float) maxPageIndex));
    }

    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public ItemStack getMapStack() {
        return this.getDisplayedItem();
    }

    @Override
    public void updateClientVisualsOnLoad() {

        ItemStack itemstack = getDisplayedItem();
        Item item = itemstack.getItem();
        this.cachedPattern = null;
        if (item instanceof BannerPatternItem bannerPatternItem) {
            this.cachedPattern = ModMaterials.getFlagMaterialForPatternItem(level, bannerPatternItem);
        }

        this.needsVisualRefresh = true;
        this.cachedPageLines = Collections.emptyList();
        this.text = null;
        updateText();

        this.isNormalItem = !canPlaceInNoticeBoard(itemstack.getItem());
    }

    public boolean isNormalItem() {
        return isNormalItem;
    }

    public void updateText() {
        this.text = null;
        ItemStack itemstack = getDisplayedItem();
        WrittenBookContent written = itemstack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (written != null) {
            var pages = written.pages();
            if (!pages.isEmpty()) {
                if (this.pageIndex >= pages.size()) {
                    this.pageIndex = this.pageIndex % pages.size();
                }
                this.maxPageIndex = pages.size() - 1;

                this.text = pages.get(this.pageIndex).map(Component::getString);
            }
            return;
        }
        WritableBookContent writable = itemstack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (writable != null) {
            var pages = writable.pages();
            if (!pages.isEmpty()) {
                if (this.pageIndex >= pages.size()) {
                    this.pageIndex = this.pageIndex % pages.size();
                }
                this.maxPageIndex = pages.size() - 1;
                this.text = pages.get(this.pageIndex);
            }
            return;
        }

        if (CompatHandler.EXPOSURE) {
            if (ExposureCompat.isPictureItem(itemstack.getItem())) {
                this.maxPageIndex = ExposureCompat.getMaxPictureCount(itemstack);
            }
        }

        //TODO: add back
        /*
        if (CompatHandler.COMPUTERCRAFT) {
            if (CCCompat.isPrintedBook(item)) {

                if (com != null) {
                    int pages = CCCompat.getPages(itemstack);

                    if (this.pageNumber >= pages) {
                        this.pageNumber = this.pageNumber % pages;
                    }
                    String[] text = CCCompat.getText(itemstack);
                    StringBuilder combined = new StringBuilder();
                    for (int i = 0; i < 21; i++) {
                        int ind = this.pageNumber * 21 + i;
                        if (ind < text.length) {
                            combined.append(text[ind]);
                            combined.append(" ");
                        }
                    }
                    this.text = combined.toString();
                }
            }
        }*/
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.pageIndex = tag.getInt("PageNumber");
        this.textHolder.load(tag, registries, worldPosition);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("PageNumber", this.pageIndex);
        this.textHolder.save(tag, registries);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new NoticeBoardContainerMenu(id, player, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty() && (CommonConfigs.Building.NOTICE_BOARDS_UNRESTRICTED.get() || canPlaceInNoticeBoard(stack.getItem()));
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean canPlaceInNoticeBoard(Item item) {
        return item.builtInRegistryHolder().is(ItemTags.LECTERN_BOOKS) ||
                item instanceof MapItem || item instanceof BannerPatternItem ||
                (CompatHandler.EXPOSURE && ExposureCompat.isPictureItem(item)) ||
                (CompatHandler.COMPUTERCRAFT && CCCompat.isPrintedBook(item));
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public boolean shouldSkipTileRenderer() {
        return getBlockState().getValue(NoticeBoardBlock.CULLED) || !getBlockState().getValue(NoticeBoardBlock.HAS_BOOK);
    }

    public Material getCachedPattern() {
        return cachedPattern;
    }

    @Nullable
    public Filterable<String> getText() {
        return text;
    }

    public DyeColor getDyeColor() {
        return textHolder.getColor();
    }

    public boolean isGlowing() {
        return textHolder.hasGlowingText();
    }

    public boolean hasAntiqueInk() {
        return textHolder.supplementaries$isAntique();
    }

    public float getFontScale() {
        return this.fontScale;
    }

    public void setFontScale(float s) {
        this.fontScale = s;
    }

    public void setCachedPageLines(List<FormattedCharSequence> l) {
        this.cachedPageLines = l;
    }

    public List<FormattedCharSequence> getCachedLines() {
        return this.cachedPageLines;
    }

    public boolean needsVisualUpdate() {
        if (this.needsVisualRefresh) {
            this.needsVisualRefresh = false;
            return true;
        }
        return false;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    public void turnPage() {
        this.pageIndex++;
        this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                this.level.random.nextFloat() * 0.10F + 1.45F);
        this.setChanged();
    }

    public ItemInteractionResult interact(Player player, InteractionHand handIn, BlockHitResult hit, ItemStack stack) {
        Level level = player.level();

        BlockState state = this.getBlockState();
        if (player.isShiftKeyDown() && !this.isEmpty() && player.getItemInHand(handIn).isEmpty()) {
            ItemStack it = this.removeItemNoUpdate(0);
            BlockPos newPos = worldPosition.offset(state.getValue(NoticeBoardBlock.FACING).getNormal());
            ItemEntity drop = new ItemEntity(level, newPos.getX() + 0.5, newPos.getY() + 0.5, newPos.getZ() + 0.5, it);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
            this.setChanged();
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        //try place or open
        Direction face = hit.getDirection();
        if (face == state.getValue(NoticeBoardBlock.FACING)) {
            ItemInteractionResult res = super.interactWithPlayerItem(player, handIn, stack);
            if (res.consumesAction()) {
                return res;
            }
        }
        ItemInteractionResult r = this.textHolderInteract(this, 0, player, handIn, stack, face, hit.getLocation());
        if (r != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) return r;


        if (!CommonConfigs.Building.NOTICE_BOARD_GUI.get()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (player instanceof ServerPlayer sp) {
            this.tryOpeningTextEditGui(this, sp, player.getItemInHand(handIn), face, hit.getLocation());
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setCurrentUser(@Nullable UUID uuid) {
        this.playerWhoMayEdit = uuid;
    }

    @Override
    public UUID getCurrentUser() {
        return playerWhoMayEdit;
    }

    @Override
    public TextHolder getTextHolder(int ind) {
        return textHolder;
    }

    @Override
    public boolean isWaxed() {
        return isWaxed;
    }

    @Override
    public void setWaxed(boolean b) {
        this.isWaxed = b;
    }
}