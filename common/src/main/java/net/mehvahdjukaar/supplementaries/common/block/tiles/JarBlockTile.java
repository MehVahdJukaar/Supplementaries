package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.common.components.MobContainerView;
import net.mehvahdjukaar.supplementaries.common.components.SoftFluidTankView;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class JarBlockTile extends ItemDisplayTile implements IMobContainerProvider, ISoftFluidTankProvider, IExtraModelDataProvider {
    public static final ModelDataKey<SoftFluid> FLUID = ModBlockProperties.FLUID;
    public static final ModelDataKey<Float> FILL_LEVEL = ModBlockProperties.FILL_LEVEL;

    public final MobContainer mobContainer;
    public final SoftFluidTank fluidHolder;

    public JarBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.JAR_TILE.get(), pos, state, 12);
        int capacity = CommonConfigs.Functional.JAR_CAPACITY.get();
        this.fluidHolder = SoftFluidTank.create(capacity);
        AbstractMobContainerItem item = ((AbstractMobContainerItem) state.getBlock().asItem());
        this.mobContainer = new MobContainer(item.getMobContainerWidth(), item.getMobContainerHeight(), true);
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(FLUID, fluidHolder.getFluidValue())
                .with(FILL_LEVEL, fluidHolder.getHeight(1));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        SoftFluidTankView fluidView = componentInput.get(ModComponents.SOFT_FLUID_CONTENT.get());
        if (fluidView != null) {
            fluidView.apply(this.fluidHolder);
        }
        MobContainerView mobView = componentInput.get(ModComponents.MOB_HOLDER_CONTENT.get());
        if (mobView != null) {
            mobView.apply(this.mobContainer);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!this.fluidHolder.isEmpty()) {
            components.set(ModComponents.SOFT_FLUID_CONTENT.get(), SoftFluidTankView.of(this.fluidHolder));
        }
        if (!this.mobContainer.isEmpty()) {
            components.set(ModComponents.MOB_HOLDER_CONTENT.get(), MobContainerView.of(this.mobContainer));
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        //ugly. removes stuff that's put in components
        tag.remove("fluid");
        tag.remove("MobHolder");
        tag.remove("BucketHolder");
    }

    @Override
    public void updateTileOnInventoryChanged() {
        this.level.updateNeighborsAt(worldPosition, this.getBlockState().getBlock()); //why is this here?
        int light = this.fluidHolder.getFluidValue().getLuminosity();
        if (light != this.getBlockState().getValue(ModBlockProperties.LIGHT_LEVEL_0_15)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(ModBlockProperties.LIGHT_LEVEL_0_15, light), 2);
        }
    }

    @Override
    public void updateClientVisualsOnLoad() {
        super.updateClientVisualsOnLoad();
        requestModelReload();
    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, InteractionHand hand, Level level, BlockPos pos) {

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack displayedStack = this.getDisplayedItem();

        //interact with fluid holder
        if (canInteractWithSoftFluidTank() && this.fluidHolder.interactWithPlayer(player, hand, level, pos)) {
            return true;
        }
        //empty hand: eat food

        // can I insert this item? For cookies and fish buckets
        else if (tryAddingItem(handStack, player, hand)) {
            return true;
        }
        //fish buckets
        else if (this.isEmpty() && this.fluidHolder.isEmpty() && this.mobContainer.interactWithBucket(handStack, level, player.blockPosition(), player, hand)) {
            return true;
        }

        if (!player.isShiftKeyDown()) {
            //from drink
            if (CommonConfigs.Functional.JAR_EAT.get()) {
                if (this.fluidHolder.tryDrinkUpFluid(player, level)) return true;
                //cookies
                if (displayedStack.has(DataComponents.FOOD) && player.canEat(false) && !player.isCreative()) {
                    //eat cookies
                    player.eat(level, displayedStack);
                    return true;
                }
            }
        }
        //extract stuff
        return this.handleExtractItem(player, hand);
    }

    // removes item from te. only 1 increment
    public ItemStack extractItem() {
        for (var j = this.getContainerSize() - 1; j >= 0; j--) {
            ItemStack s = this.getItem(j);
            if (!s.isEmpty()) {
                this.removeItemNoUpdate(j);
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    // removes item from te and gives it to player
    public boolean handleExtractItem(Player player, InteractionHand hand) {
        if (!player.getItemInHand(hand).isEmpty()) return false;
        ItemStack extracted = this.extractItem();
        if (!extracted.isEmpty()) {
            Utils.swapItem(player, hand, extracted);
            return true;
        }
        return false;
    }

    // adds item to te, removes from player
    public boolean tryAddingItem(ItemStack stack, @Nullable Player player, InteractionHand handIn) {
        ItemStack handStack = stack.copy();
        handStack.setCount(1);
        if (this.tryAddingItem(handStack)) {

            if (player != null) {
                ItemStack returnStack = ItemStack.EMPTY;
                Level level = player.level();
                level.playSound(player, this.worldPosition, ModSounds.JAR_COOKIE.get(), SoundSource.BLOCKS,
                        1, 0.9f + level.random.nextFloat() * 0.1f);
                player.awardStat(Stats.ITEM_USED.get(handStack.getItem()));
                // shrink stack and replace bottle /bucket with empty ones
                if (!player.isCreative()) {
                    Utils.swapItem(player, handIn, returnStack);
                }
            }
            return true;
        }
        return false;
    }

    public boolean tryAddingItem(ItemStack itemstack) {
        for (int i = 0; i < this.getItems().size(); i++) {
            if (canPlaceItem(i, itemstack)) {
                this.setItem(i, itemstack);
                return true;
            }
        }
        return false;
    }

    public void clearAllContents() {
        this.fluidHolder.clear();
        this.mobContainer.clear();
        this.setItems(NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY));
    }

    public boolean isPonyJar() {
        //hahaha, funy pony jar meme
        if (this.hasCustomName()) {
            Component c = this.getCustomName();
            return (c != null && c.getString().toLowerCase(Locale.ROOT).contains("cum"));
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fluidHolder.load(tag, registries);
        this.mobContainer.load(tag, registries);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.fluidHolder.save(tag, registries);
        this.mobContainer.save(tag, registries);
    }

    public boolean hasContent() {
        return !(this.isEmpty() && this.mobContainer.isEmpty() && this.fluidHolder.isEmpty());
    }

    public boolean isFull() {
        return this.getItems().stream().noneMatch(ItemStack::isEmpty);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.jar");
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(index, stack);
    }

    //can this item be added?
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (this.getItem(index).getCount() < this.getMaxStackSize() &&
                CommonConfigs.Functional.JAR_COOKIES.get() && this.fluidHolder.isEmpty() && this.mobContainer.isEmpty()) {
            return stack.is(ModTags.COOKIES);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public MobContainer getMobContainer() {
        return this.mobContainer;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ClockBlock.FACING);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, JarBlockTile tile) {
        tile.mobContainer.tick(pLevel, pPos);
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return this.fluidHolder;
    }

    @Override
    public boolean canInteractWithSoftFluidTank() {
        return CommonConfigs.Functional.JAR_LIQUIDS.get() && this.isEmpty() && (this.mobContainer.isEmpty() || isPonyJar());
    }
}