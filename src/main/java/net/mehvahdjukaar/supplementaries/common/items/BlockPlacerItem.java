package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

//hacky registered item that handles placing placeable stuff
public class BlockPlacerItem extends BlockItem {

    public static final List<Pair<Block, Supplier<? extends Item>>> PLACEABLE_ITEMS = new ArrayList<>();

    public static BlockPlacerItem getInstance() {
        return ModRegistry.BLOCK_PLACER.get();
    }

    private FoodProperties mimicFood;
    private Block mimicBlock;
    private SoundType overrideSound;

    public BlockPlacerItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    public static void registerPlaceableItem(Block block, Supplier<? extends Item> item) {
        PLACEABLE_ITEMS.add(Pair.of(block, item));
    }

    @Override
    public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
        super.registerBlocks(pBlockToItemMap, pItem);
        for (var v : PLACEABLE_ITEMS) {
            Block b = v.getFirst();
            Item i = v.getSecond().get();
            if (i != null && i != Items.AIR && b != null && b != Blocks.AIR) {
                ((IPlaceableItem)i).makePlaceable(b);
                pBlockToItemMap.put(b, i);
            }
        }
    }

    @Nullable
    public BlockState mimicGetPlacementState(BlockPlaceContext pContext, Block toPlace) {
        this.mimicBlock = toPlace;
        var r = getPlacementState(pContext);
        this.mimicBlock = null;
        return r;
    }

    public InteractionResult mimicUseOn(UseOnContext pContext, Block toPlace, FoodProperties foodProperties) {
        this.mimicFood = foodProperties;
        this.mimicBlock = toPlace;
        var r = super.useOn(pContext);
        this.mimicFood = null;
        this.mimicBlock = null;
        return r;
    }

    public InteractionResult mimicPlace(BlockPlaceContext pContext, Block toPlace, @Nullable SoundType overrideSound) {
        this.overrideSound = overrideSound;
        this.mimicBlock = toPlace;
        var r = super.place(pContext);
        this.overrideSound = null;
        this.mimicBlock = null;
        return r;
    }

    @Override
    public Block getBlock() {
        if (this.mimicBlock != null) return mimicBlock;
        return super.getBlock();
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties() {
        return mimicFood;
    }

    @Override
    public boolean isEdible() {
        return mimicFood != null;
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState pState) {
        if (this.overrideSound != null) return this.overrideSound.getPlaceSound();
        return super.getPlaceSound(pState);
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        if (this.overrideSound != null) return this.overrideSound.getPlaceSound();
        return super.getPlaceSound(state, world, pos, entity);
    }


}
