package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.screens.DoormatScreen;
import net.mehvahdjukaar.supplementaries.client.screens.NoticeBoardScreen;
import net.mehvahdjukaar.supplementaries.common.block.IMapDisplay;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.NoticeBoardContainerMenu;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CCCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public class NoticeBoardBlockTile extends ItemDisplayTile implements Nameable, IMapDisplay, ITextHolderProvider {

    //just used for color
    private final TextHolder textHolder;
    private boolean isWaxed = false;
    private int pageNumber = 0;

    @Nullable
    private UUID playerWhoMayEdit;

    //client stuff
    private String text = null;
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

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.notice_board");
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
                this.pageNumber = 0;
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.50F);
            }
        }
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
            this.cachedPattern = ModMaterials.getFlagMaterialForPatternItem(bannerPatternItem);
        }

        this.needsVisualRefresh = true;
        this.cachedPageLines = Collections.emptyList();
        this.text = null;
        updateText();

        this.isNormalItem = !isPageItem(itemstack.getItem());
    }

    public boolean isNormalItem() {
        return isNormalItem;
    }

    public void updateText() {
        ItemStack itemstack = getDisplayedItem();
        Item item = itemstack.getItem();
        CompoundTag com = itemstack.getTag();
        if ((item instanceof WrittenBookItem && WrittenBookItem.makeSureTagIsValid(com)) ||
                (item instanceof WritableBookItem && WritableBookItem.makeSureTagIsValid(com))) {

            ListTag pages = com.getList("pages", 8).copy();
            if (!pages.isEmpty()) {
                if (this.pageNumber >= pages.size()) {
                    this.pageNumber = this.pageNumber % pages.size();
                }

                this.text = pages.getString(this.pageNumber);
            }

        } else if (CompatHandler.COMPUTERCRAFT) {
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
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.pageNumber = compound.getInt("PageNumber");
        this.textHolder.load(compound, level, worldPosition);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("PageNumber", this.pageNumber);
        this.textHolder.save(tag);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new NoticeBoardContainerMenu(id, player, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty() && (CommonConfigs.Building.NOTICE_BOARDS_UNRESTRICTED.get() || isPageItem(stack.getItem()));
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isPageItem(Item item) {
        return item.builtInRegistryHolder().is(ItemTags.LECTERN_BOOKS) || item instanceof MapItem || item instanceof BannerPatternItem || (CompatHandler.COMPUTERCRAFT && CCCompat.isPrintedBook(item));
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

    public String getText() {
        return text;
    }

    public DyeColor getDyeColor() {
        return textHolder.getColor();
    }

    public boolean isGlowing() {
        return textHolder.hasGlowingText();
    }

    public boolean hasAntiqueInk() {
        return textHolder.hasAntiqueInk();
    }


    public TextUtil.RenderProperties computeRenderProperties(int frontLight, Vector3f normal, BooleanSupplier isNear) {
        return textHolder.computeRenderProperties(frontLight, normal, isNear);
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

    public List<FormattedCharSequence> getRendererLines() {
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
        this.pageNumber++;
        this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                this.level.random.nextFloat() * 0.10F + 1.45F);
        this.setChanged();
    }

    public InteractionResult interact(Player player, InteractionHand handIn, BlockPos pos, BlockState state, BlockHitResult hit) {
        Level level = player.level();

        if (level instanceof ServerLevel sl && textHolder.playerInteract(sl, pos, player, handIn, this).consumesAction()) {
            return InteractionResult.CONSUME;
        } else if (player.isShiftKeyDown() && !this.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack it = this.removeItemNoUpdate(0);
                BlockPos newPos = pos.offset(state.getValue(NoticeBoardBlock.FACING).getNormal());
                ItemEntity drop = new ItemEntity(level, newPos.getX() + 0.5, newPos.getY() + 0.5, newPos.getZ() + 0.5, it);
                drop.setDefaultPickUpDelay();
                level.addFreshEntity(drop);
                this.setChanged();
            }
        }
        //try place or open
        else if (hit.getDirection() != state.getValue(NoticeBoardBlock.FACING) ||
                !super.interact(player, handIn).consumesAction()) {
            if (!CommonConfigs.Building.NOTICE_BOARD_GUI.get()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                this.tryOpeningEditGui((ServerPlayer) player, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlayerWhoMayEdit(@Nullable UUID uuid) {
        validatePlayerWhoMayEdit(level, worldPosition);
        this.playerWhoMayEdit = uuid;
    }

    @Override
    public UUID getPlayerWhoMayEdit() {
        validatePlayerWhoMayEdit(level, worldPosition);
        return playerWhoMayEdit;
    }

    @Override
    public TextHolder getTextHolder(int ind) {
        return textHolder;
    }

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
        //unused we are have a container instead
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