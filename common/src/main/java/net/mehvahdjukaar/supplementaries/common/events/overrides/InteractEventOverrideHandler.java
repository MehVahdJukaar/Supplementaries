package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.api.IExtendedItem;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.items.additional_placements.SimplePlacement;
import net.mehvahdjukaar.supplementaries.common.items.additional_placements.WallLanternPlacement;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class InteractEventOverrideHandler {

    //equivalent to Item.useOnBlock to the item itself (called before that though)
    //high priority
    private static final Map<Item, ItemUseOnBlockOverride> ITEM_USE_ON_BLOCK_HP = new IdentityHashMap<>();
    private static final Map<Item, ItemUseOnBlockOverride> ITEM_USE_ON_BLOCK = new IdentityHashMap<>();
    //equivalent to Item.use
    private static final Map<Item, ItemUseOverride> ITEM_USE = new IdentityHashMap<>();
    //equivalent to Block.use
    private static final Map<Block, BlockUseOverride> BLOCK_USE = new IdentityHashMap<>();

    public static boolean hasBlockPlacementAssociated(Item item) {
        var v = ITEM_USE_ON_BLOCK.getOrDefault(item,ITEM_USE_ON_BLOCK_HP.get(item));
        return v != null && v.placesBlock();
    }

    public static void registerOverrides() {

        List<ItemUseOnBlockOverride> itemUseOnBlockHP = new ArrayList<>();
        List<ItemUseOnBlockOverride> itemUseOnBlock = new ArrayList<>();
        List<ItemUseOverride> itemUse = new ArrayList<>();
        List<BlockUseOverride> blockUse = new ArrayList<>();

        blockUse.add(new DirectionalCakeConversionBehavior());
        blockUse.add(new BellChainBehavior());
        blockUse.add(new FDStickBehavior());

        itemUse.add(new ThrowableBricksBehavior());
        itemUse.add(new ClockItemBehavior());
        itemUse.add(new CompassItemBehavior());

        itemUseOnBlockHP.add(new AntiqueInkBehavior());
        itemUseOnBlockHP.add(new SoapBehavior());
        itemUseOnBlockHP.add(new WrenchBehavior());
        itemUseOnBlockHP.add(new SkullCandlesBehavior());

        //maybe move in mixin system (can't for cakes as block interaction has priority)
        itemUseOnBlock.add(new SkullPileBehavior());
        itemUseOnBlock.add(new DoubleCakeBehavior());
        itemUseOnBlock.add(new WrittenBookHackBehavior());

        itemUseOnBlock.add(new MapMarkerBehavior());
        itemUseOnBlock.add(new XPBottlingBehavior());

        if (CommonConfigs.Tweaks.WRITTEN_BOOKS.get()) {
            ((IExtendedItem) Items.WRITABLE_BOOK).addAdditionalBehavior(new SimplePlacement(ModRegistry.BOOK_PILE.get()));
            ((IExtendedItem) Items.WRITTEN_BOOK).addAdditionalBehavior(new SimplePlacement(ModRegistry.BOOK_PILE.get()));
        }
        outer:
        for (Item i : Registry.ITEM) {

            if (CommonConfigs.Tweaks.WALL_LANTERN_PLACEMENT.get()) {
                if (i instanceof BlockItem bi && WallLanternBlock.isValidBlock(bi.getBlock())) {
                    ((IExtendedItem) i).addAdditionalBehavior(new WallLanternPlacement());
                    continue;
                }
            }
            if (CommonConfigs.Tweaks.PLACEABLE_BOOKS.get()) {
                if (BookPileBlock.isQuarkTome(i)) {
                    ((IExtendedItem) i).addAdditionalBehavior(new SimplePlacement(ModRegistry.BOOK_PILE.get()));
                    continue;
                }
            }
            //block items don't work here
            /*
            if (ServerConfigs.cached.SKULL_CANDLES) {
                if (i.builtInRegistryHolder().is(ItemTags.CANDLES) &&
                        i.getRegistryName().getNamespace().equals("minecraft")) {
                    ((IExtendedItem) i).addAdditionalBehavior(new SkullCandlesPlacement());
                    continue;
                }
            }*/


            for (ItemUseOnBlockOverride b : itemUseOnBlock) {
                if (b.appliesToItem(i)) {
                    ITEM_USE_ON_BLOCK.put(i, b);
                    continue outer;
                }
            }
            for (ItemUseOverride b : itemUse) {
                if (b.appliesToItem(i)) {
                    ITEM_USE.put(i, b);
                    continue outer;
                }
            }
            for (ItemUseOnBlockOverride b : itemUseOnBlockHP) {
                if (b.appliesToItem(i)) {
                    ITEM_USE_ON_BLOCK_HP.put(i, b);
                    continue outer;
                }
            }
        }
        for (Block block : Registry.BLOCK) {
            for (BlockUseOverride b : blockUse) {
                if (b.appliesToBlock(block)) {
                    BLOCK_USE.put(block, b);
                    break;
                }
            }
        }
    }

    public static InteractionResult onItemUsedOnBlockHP(Player player, Level level, ItemStack stack,
                                                        InteractionHand hand, BlockHitResult hit) {
        Item item = stack.getItem();

        ItemUseOnBlockOverride override = ITEM_USE_ON_BLOCK_HP.get(item);
        if (override != null && override.isEnabled()) {
            if(CompatHandler.FLAN && override.altersWorld() && !FlanCompat.canPlace(player,hit.getBlockPos())){
                return InteractionResult.PASS;
            }
            return override.tryPerformingAction(level, player, hand, stack, hit);
        }
        return InteractionResult.PASS;
    }


    //item clicked on block overrides
    public static InteractionResult onItemUsedOnBlock(Player player, Level level, ItemStack stack,
                                                      InteractionHand hand, BlockHitResult hit) {
        Item item = stack.getItem();

        ItemUseOnBlockOverride override = ITEM_USE_ON_BLOCK.get(item);
        if (override != null && override.isEnabled()) {
            if(CompatHandler.FLAN && override.altersWorld() && !FlanCompat.canPlace(player,hit.getBlockPos())){
                return InteractionResult.PASS;
            }
            InteractionResult result = override.tryPerformingAction(level, player, hand, stack, hit);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        //block overrides behaviors (work for any item)
        if (!player.isShiftKeyDown()) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);

            BlockUseOverride o = BLOCK_USE.get(state.getBlock());
            if (o != null && o.isEnabled()) {
                if(CompatHandler.FLAN && o.altersWorld() && !FlanCompat.canPlace(player,hit.getBlockPos())){
                    return InteractionResult.PASS;
                }
                return o.tryPerformingAction(state, pos, level, player, hand, stack, hit);
            }
        }
        return InteractionResult.PASS;
        //not sure if this is needed
        //CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, po, heldStack);
    }

    //item clicked overrides
    public static InteractionResultHolder<ItemStack> onItemUse(
            Player player, Level level, InteractionHand hand, ItemStack stack) {
        Item item = stack.getItem();

        ItemUseOverride override = ITEM_USE.get(item);
        if (override != null && override.isEnabled()) {

            var ret = override.tryPerformingAction(level, player, hand, stack, null);
            return switch (ret) {
                case CONSUME -> InteractionResultHolder.consume(stack);
                case SUCCESS -> InteractionResultHolder.success(stack);
                default -> InteractionResultHolder.pass(stack);
                case FAIL -> InteractionResultHolder.fail(stack);
            };
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void addOverrideTooltips(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> components) {
        Item item = itemStack.getItem();

        ItemUseOnBlockOverride override = ITEM_USE_ON_BLOCK.get(item);
        if (override != null && override.isEnabled()) {
            MutableComponent t = override.getTooltip();
            if (t != null) components.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        } else {
            ItemUseOverride o = ITEM_USE.get(item);
            if (o != null && o.isEnabled()) {
                MutableComponent t = o.getTooltip();
                if (t != null) components.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }
        }
    }

    public static InteractionResult replaceSimilarBlock(Block blockOverride, Player player, ItemStack stack,
                                                        BlockPos pos, Level level, BlockState replaced,
                                                        @Nullable SoundType sound, Property<?>... properties) {

        BlockState newState = blockOverride.defaultBlockState();
        for (Property<?> p : properties) {
            newState = BlockUtil.replaceProperty(replaced, newState, p);
        }
        if (newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            FluidState fluidstate = level.getFluidState(pos);
            newState = newState.setValue(BlockStateProperties.WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);
        }
        if (!level.setBlock(pos, newState, 3)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, stack);
        }
        level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);

        if (sound == null) sound = newState.getSoundType();
        level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        // if (player instanceof ServerPlayer serverPlayer && !isRanged) {
        //     CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        // }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
