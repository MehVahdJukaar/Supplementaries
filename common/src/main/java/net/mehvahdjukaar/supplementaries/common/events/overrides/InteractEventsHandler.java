package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class InteractEventsHandler {

    //equivalent to Item.useOnBlock to the item itself (called before that though)
    //high priority
    private static final Map<Item, ItemUseOnBlockBehavior> ITEM_USE_ON_BLOCK_HP = new IdentityHashMap<>();
    private static final Map<Item, ItemUseOnBlockBehavior> ITEM_USE_ON_BLOCK = new IdentityHashMap<>();
    //equivalent to Item.use
    private static final Map<Item, ItemUseBehavior> ITEM_USE = new IdentityHashMap<>();
    //equivalent to Block.use
    private static final Map<Block, BlockUseBehavior> BLOCK_USE = new IdentityHashMap<>();

    public static boolean hasBlockPlacementAssociated(Item item) {
        var v = ITEM_USE_ON_BLOCK.getOrDefault(item, ITEM_USE_ON_BLOCK_HP.get(item));
        return v != null && v.placesBlock();
    }

    public static void init() {


    }


    public static void setupOverrides() {
        ITEM_USE_ON_BLOCK.clear();
        ITEM_USE_ON_BLOCK_HP.clear();
        ITEM_USE.clear();
        BLOCK_USE.clear();

        //registers event stuff
        List<ItemUseOnBlockBehavior> itemUseOnBlockHP = new ArrayList<>();
        List<ItemUseOnBlockBehavior> itemUseOnBlock = new ArrayList<>();
        List<ItemUseBehavior> itemUse = new ArrayList<>();
        List<BlockUseBehavior> blockUse = new ArrayList<>();

        blockUse.add(new FDStickBehavior());

        itemUse.add(new ThrowableBricksBehavior());
        itemUse.add(new ThrowableSlimeballBehavior());
        itemUse.add(new ClockItemBehavior());
        itemUse.add(new CompassItemBehavior());
        LightUpLumiseneLiquidBehavior liquidHack = new LightUpLumiseneLiquidBehavior();
        // needs to be on both as neither is called if the other is.
        itemUse.add(liquidHack);
        itemUseOnBlock.add(liquidHack);

        itemUseOnBlockHP.add(new AntiqueInkBehavior());
        itemUseOnBlockHP.add(new SoapBehavior());
        itemUseOnBlockHP.add(new WrenchBehavior());

        //maybe move in mixin system (can't for cakes as block interaction has priority)
        itemUseOnBlock.add(new WrittenBookHackBehavior());

        itemUseOnBlock.add(new MapMarkerBehavior());
        itemUseOnBlock.add(new XPBottlingBehavior());


        outer:
        for (Item i : BuiltInRegistries.ITEM) {

            //block items don't work here

            for (ItemUseOnBlockBehavior b : itemUseOnBlock) {
                if (b.appliesToItem(i)) {
                    ITEM_USE_ON_BLOCK.put(i, b);
                }
            }
            for (ItemUseBehavior b : itemUse) {
                if (b.appliesToItem(i)) {
                    ITEM_USE.put(i, b);
                    continue outer;
                }
            }
            for (ItemUseOnBlockBehavior b : itemUseOnBlockHP) {
                if (b.appliesToItem(i)) {
                    ITEM_USE_ON_BLOCK_HP.put(i, b);
                    continue outer;
                }
            }
        }
        for (Block block : BuiltInRegistries.BLOCK) {
            for (BlockUseBehavior b : blockUse) {
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

        ItemUseOnBlockBehavior override = ITEM_USE_ON_BLOCK_HP.get(item);
        if (override != null && override.isEnabled()) {
            if (CompatHandler.FLAN && override.altersWorld() && !FlanCompat.canPlace(player, hit.getBlockPos())) {
                return InteractionResult.PASS;
            }
            if (override.altersWorld() && !Utils.mayPerformBlockAction(player, hit.getBlockPos(), stack)) {
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


        ItemUseOnBlockBehavior override = ITEM_USE_ON_BLOCK.get(item);
        BlockPos pos = hit.getBlockPos();

        boolean canAlter = Utils.mayPerformBlockAction(player, pos, stack) && (!CompatHandler.FLAN || FlanCompat.canPlace(player, pos));

        if (override != null && override.isEnabled()) {
            if (override.altersWorld() && !canAlter) {
                return InteractionResult.PASS;
            }
            InteractionResult result = override.tryPerformingAction(level, player, hand, stack, hit);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        //block overrides behaviors (work for any item)
        if (!player.isShiftKeyDown()) {
            BlockState state = level.getBlockState(pos);

            BlockUseBehavior o = BLOCK_USE.get(state.getBlock());
            if (o != null && o.isEnabled()) {
                if (o.altersWorld() && !canAlter) {
                    return InteractionResult.PASS;
                }
                return o.tryPerformingAction(state, pos, level, player, hand, stack, hit);
            }
        }

        // lightable fluid hacks since fluids have no collision
        if (canAlter) {
            BlockState aboveState = level.getBlockState(pos.above());
            if (aboveState.getFluidState().is(ModFluids.LUMISENE_FLUID.get())) {
                return aboveState.useItemOn(stack, level, player, hand, new BlockHitResult(hit.getLocation(),
                        hit.getDirection(), pos.above(), false)).result();
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

        ItemUseBehavior override = ITEM_USE.get(item);
        if (override != null && override.isEnabled()) {
            var ret = override.tryPerformingAction(level, player, hand, stack, null);
            return switch (ret) {
                case CONSUME -> InteractionResultHolder.consume(stack);
                case SUCCESS -> InteractionResultHolder.success(stack);
                case FAIL -> InteractionResultHolder.fail(stack);
                default -> InteractionResultHolder.pass(stack);
            };
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void addOverrideTooltips(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> components) {
        Item item = itemStack.getItem();

        ItemUseOnBlockBehavior override = ITEM_USE_ON_BLOCK.get(item);
        if (override != null && override.isEnabled()) {
            MutableComponent t = override.getTooltip();
            if (t != null) components.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        } else {
            ItemUseBehavior o = ITEM_USE.get(item);
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
