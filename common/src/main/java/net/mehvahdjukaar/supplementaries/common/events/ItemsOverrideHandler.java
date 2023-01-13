package net.mehvahdjukaar.supplementaries.common.events;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.api.IExtendedItem;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoubleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.mehvahdjukaar.supplementaries.common.items.additional_behaviors.SimplePlacement;
import net.mehvahdjukaar.supplementaries.common.items.additional_behaviors.WallLanternPlacement;
import net.mehvahdjukaar.supplementaries.common.misc.AntiqueInkHelper;
import net.mehvahdjukaar.supplementaries.common.misc.SoapWashableHelper;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ItemsOverrideHandler {

    //TODO: clean this up

    //equivalent to Item.useOnBlock to the item itself (called before that though)
    private static final Map<Item, ItemUseOnBlockOverride> HP_ON_BLOCK_OVERRIDES = new IdentityHashMap<>();
    private static final Map<Item, ItemUseOnBlockOverride> ON_BLOCK_OVERRIDES = new IdentityHashMap<>();

    //equivalent to Item.use
    private static final Map<Item, ItemUseOverride> ITEM_USE_OVERRIDES = new IdentityHashMap<>();

    //equivalent to Block.use
    private static final Map<Block, BlockInteractedWithOverride> BLOCK_USE_OVERRIDES = new IdentityHashMap<>();

    public static boolean hasBlockPlacementAssociated(Item item) {
        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        return override != null;
    }

    public static void registerOverrides() {

        List<ItemUseOnBlockOverride> HPItemActionOnBlock = new ArrayList<>();
        List<ItemUseOnBlockOverride> itemActionOnBlock = new ArrayList<>();
        List<ItemUseOverride> itemAction = new ArrayList<>();
        List<BlockInteractedWithOverride> actionOnBlock = new ArrayList<>();

        actionOnBlock.add(new DirectionalCakeConversionBehavior());
        actionOnBlock.add(new BellChainBehavior());
        actionOnBlock.add(new FDStickBehavior());

        itemAction.add(new ThrowableBrickBehavior());
        itemAction.add(new ClockItemBehavior());
        itemAction.add(new CompassItemBehavior());

        HPItemActionOnBlock.add(new AntiqueInkBehavior());
        HPItemActionOnBlock.add(new SoapBehavior());
        HPItemActionOnBlock.add(new WrenchBehavior());
        HPItemActionOnBlock.add(new SkullCandlesBehavior());

        //maybe move in mixin system (can't for cakes as block interaction has priority)
        itemActionOnBlock.add(new SkullPileBehavior());

        itemActionOnBlock.add(new DoubleCakeBehavior());

        itemActionOnBlock.add(new MapMarkerBehavior());
        itemActionOnBlock.add(new XpBottlingBehavior());


        if (CommonConfigs.Tweaks.WRITTEN_BOOKS.get()) {
            itemActionOnBlock.add(new WrittenBookHackBehavior());
            ((IExtendedItem) Items.WRITABLE_BOOK).addAdditionalBehavior(new SimplePlacement(ModRegistry.BOOK_PILE.get()));
            ((IExtendedItem) Items.WRITTEN_BOOK).addAdditionalBehavior(new SimplePlacement(ModRegistry.BOOK_PILE.get()));
        }
        outer:
        for (Item i : Registry.ITEM) {

            if (CommonConfigs.Tweaks.WALL_LANTERN_PLACEMENT.get()) {
                if (i instanceof BlockItem bi && MiscUtils.isLanternBlock(bi.getBlock())) {
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


            for (ItemUseOnBlockOverride b : itemActionOnBlock) {
                if (b.appliesToItem(i)) {
                    ON_BLOCK_OVERRIDES.put(i, b);
                    continue outer;
                }
            }
            for (ItemUseOverride b : itemAction) {
                if (b.appliesToItem(i)) {
                    ITEM_USE_OVERRIDES.put(i, b);
                    continue outer;
                }
            }
            for (ItemUseOnBlockOverride b : HPItemActionOnBlock) {
                if (b.appliesToItem(i)) {
                    HP_ON_BLOCK_OVERRIDES.put(i, b);
                    continue outer;
                }
            }
        }
        for (Block block : Registry.BLOCK) {
            for (BlockInteractedWithOverride b : actionOnBlock) {
                if (b.appliesToBlock(block)) {
                    BLOCK_USE_OVERRIDES.put(block, b);
                    break;
                }
            }
        }
    }

    public static InteractionResult tryHighPriorityClickedBlockOverride(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();

        ItemUseOnBlockOverride override = HP_ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            return override.tryPerformingAction(level, player, hand, stack, hit, false);
        }
        return InteractionResult.PASS;
    }


    //item clicked on block overrides
    public static InteractionResult tryPerformClickedBlockOverride(Player player, Level level, InteractionHand hand, BlockHitResult hit, boolean isRanged) {
        return tryPerformClickedBlockOverride(player, level, player.getItemInHand(hand), hand, hit, isRanged);
    }


    public static InteractionResult tryPerformClickedBlockOverride(Player player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit, boolean isRanged) {

        Item item = stack.getItem();

        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            InteractionResult result = override.tryPerformingAction(level, player, hand, stack, hit, isRanged);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        //block overrides behaviors (work for any item)
        if (!player.isShiftKeyDown()) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);

            BlockInteractedWithOverride o = BLOCK_USE_OVERRIDES.get(state.getBlock());
            if (o != null && o.isEnabled()) {

                return o.tryPerformingAction(state, pos, level, player, hand, stack, hit);
            }
        }
        return InteractionResult.PASS;
        //not sure if this is needed
        //CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, po, heldStack);
    }

    //item clicked overrides
    public static InteractionResultHolder<ItemStack> tryPerformClickedItemOverride(Player player, Level level, InteractionHand hand, ItemStack stack) {
        Item item = stack.getItem();

        ItemUseOverride override = ITEM_USE_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            var ret = override.tryPerformingAction(level, player, hand, stack, null, false);
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

        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {
            MutableComponent t = override.getTooltip();
            if (t != null) components.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        } else {
            ItemUseOverride o = ITEM_USE_OVERRIDES.get(item);
            if (o != null && o.isEnabled()) {
                MutableComponent t = o.getTooltip();
                if (t != null) components.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }
        }
    }


    private abstract static class BlockInteractedWithOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToBlock(Block block);

        public abstract InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player, InteractionHand hand,
                                                              ItemStack stack, BlockHitResult hit);
    }

    private abstract static class ItemUseOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToItem(Item item);

        @Nullable
        public MutableComponent getTooltip() {
            return null;
        }

        public abstract InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                              ItemStack stack, BlockHitResult hit, boolean isRanged);
    }

    private abstract static class ItemUseOnBlockOverride extends ItemUseOverride {

        public boolean shouldBlockMapToItem(Item item) {
            return appliesToItem(item);
        }

        @Override
        @Nullable
        public MutableComponent getTooltip() {
            return null;
        }
    }


    private static class WrittenBookHackBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.WRITTEN_BOOKS.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.WRITTEN_BOOK || item == Items.WRITABLE_BOOK;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.isSecondaryUseActive()) {
                var r = ((IExtendedItem) stack.getItem()).getAdditionalBehavior()
                        .overrideUseOn(new UseOnContext(player, hand, hit), null);
                if (r.consumesAction()) return r;
            }
            return InteractionResult.PASS;
        }
    }

    private static class ClockItemBehavior extends ItemUseOverride {

        @Override
        public boolean isEnabled() {
            return ClientConfigs.Tweaks.CLOCK_CLICK.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.CLOCK;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (world.isClientSide) {
                ClockBlock.displayCurrentHour(world, player);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
    }

    private static class CompassItemBehavior extends ItemUseOverride {

        @Override
        public boolean isEnabled() {
            return ClientConfigs.Tweaks.COMPASS_CLICK.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.COMPASS;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (world.isClientSide) {
                GlobeBlock.displayCurrentCoordinates(world, player, player.blockPosition());
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
    }

    private static class ThrowableBrickBehavior extends ItemUseOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.THROWABLE_BRICKS_ENABLED.get();
        }

        @Nullable
        @Override
        public MutableComponent getTooltip() {
            return Component.translatable("message.supplementaries.throwable_brick");
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item.builtInRegistryHolder().is(ModTags.BRICKS);
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!world.isClientSide) {
                ThrowableBrickEntity brickEntity = new ThrowableBrickEntity(world, player);
                brickEntity.setItem(stack);
                float pow = 0.7f;
                brickEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F * pow, 1.0F * pow);
                world.addFreshEntity(brickEntity);
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
    }

    private static class DirectionalCakeConversionBehavior extends BlockInteractedWithOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.DIRECTIONAL_CAKE.get();
        }

        @Override
        public boolean appliesToBlock(Block block) {
            return block == net.minecraft.world.level.block.Blocks.CAKE || (block.builtInRegistryHolder().is(BlockTags.CANDLE_CAKES) && Utils.getID(block).getNamespace().equals("minecraft"));
        }

        @Override
        public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
            //lets converting to candle cake
            if (state.is(BlockTags.CANDLE_CAKES) && stack.is(ItemTags.CANDLES)) {
                return InteractionResult.PASS;
            }
            if (state.is(net.minecraft.world.level.block.Blocks.CAKE) && (stack.is(ItemTags.CANDLES) || player.getDirection() == Direction.EAST || state.getValue(CakeBlock.BITES) != 0)) {
                return InteractionResult.PASS;
            }
            if (!(CommonConfigs.Tweaks.DOUBLE_CAKE_PLACEMENT.get() && stack.is(Items.CAKE))) {
                //for candles. normal cakes have no drops
                BlockState newState = ModRegistry.DIRECTIONAL_CAKE.get().defaultBlockState();
                if (world.isClientSide) world.setBlock(pos, newState, 3);
                BlockHitResult raytrace = new BlockHitResult(
                        new Vec3(pos.getX(), pos.getY(), pos.getZ()), hit.getDirection(), pos, false);

                var r = newState.use(world, player, hand, raytrace);
                if (world instanceof ServerLevel serverLevel) {
                    if (r.consumesAction()) {
                        //prevents dropping cake
                        Block.getDrops(state, serverLevel, pos, null).forEach((d) -> {
                            if (d.getItem() != Items.CAKE) {
                                Block.popResource(world, pos, d);
                            }
                        });
                        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
                    } else world.setBlock(pos, state, 3); //returns to normal
                }
                return r;
            }
            //fallback to default cake interaction
            return InteractionResult.PASS;
        }
    }

    private static class BellChainBehavior extends BlockInteractedWithOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.BELL_CHAIN.get();
        }

        @Override
        public boolean appliesToBlock(Block block) {
            return block instanceof ChainBlock;
        }

        @Override
        public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
            //bell chains
            if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                if (RopeBlock.findAndRingBell(world, pos, player, 0, s -> s.getBlock() instanceof ChainBlock && s.getValue(ChainBlock.AXIS) == Direction.Axis.Y)) {
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.PASS;
        }
    }

    private static class FDStickBehavior extends BlockInteractedWithOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.PLACEABLE_STICKS.get() && CompatHandler.FARMERS_DELIGHT;
        }

        @Override
        public boolean appliesToBlock(Block block) {
            return block == CompatObjects.TOMATOES.get();
        }

        @Override
        public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
            //bell chains
            if (stack.getItem() == Items.STICK) {
                var tomato = FarmersDelightCompat.getStickTomato();
                if (tomato != null) {
                    return ItemsOverrideHandler.replaceSimilarBlock(tomato,
                            player, stack, pos, level, state, SoundType.WOOD, BlockStateProperties.AGE_3);
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class MapMarkerBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.MAP_MARKERS.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item instanceof MapItem || (CompatHandler.MAPATLAS && MapAtlasCompat.isAtlas(item));
        }

        @Override
        public InteractionResult tryPerformingAction(Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            BlockPos pos = hit.getBlockPos();
            if (MapHelper.toggleMarkersAtPos(level, pos, stack, player)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }
    }

    private static class XpBottlingBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.BOTTLE_XP.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.GLASS_BOTTLE || item instanceof JarItem || item == Items.EXPERIENCE_BOTTLE;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {

            JarBlockTile dummyTile = new JarBlockTile(BlockPos.ZERO, ModRegistry.JAR.get().defaultBlockState());

            BlockPos pos = hit.getBlockPos();
            Item i = stack.getItem();
            if (world.getBlockState(pos).getBlock() instanceof EnchantmentTableBlock) {
                ItemStack returnStack = null;

                //prevent accidentally releasing bottles
                if (i == Items.EXPERIENCE_BOTTLE) {
                    return InteractionResult.FAIL;
                }

                if (player.experienceLevel > 0 || player.isCreative()) {
                    if (i == Items.GLASS_BOTTLE) {
                        returnStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
                    } else if (i instanceof JarItem) {
                        dummyTile.resetHolders();
                        CompoundTag tag = stack.getTagElement("BlockEntityTag");
                        if (tag != null) {
                            dummyTile.load(tag);
                        }

                        if (dummyTile.canInteractWithSoftFluidTank()) {
                            ItemStack tempStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
                            ItemStack temp = dummyTile.fluidHolder.interactWithItem(tempStack, null, null, false);
                            if (temp != null && temp.getItem() == Items.GLASS_BOTTLE) {
                                returnStack = ((JarBlock) ((BlockItem) i).getBlock()).getJarItem(dummyTile);
                            }
                        }
                    }

                    if (returnStack != null) {
                        player.hurt(ModDamageSources.BOTTLING_DAMAGE, CommonConfigs.Tweaks.BOTTLING_COST.get());
                        Utils.swapItem(player, hand, returnStack);

                        if (!player.isCreative())
                            player.giveExperiencePoints(-Utils.getXPinaBottle(1, world.random) - 3);

                        if (world.isClientSide) {
                            Minecraft.getInstance().particleEngine.createTrackingEmitter(player, ModParticles.BOTTLING_XP_PARTICLE.get(), 1);
                        }
                        world.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1, 1);

                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class DoubleCakeBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public MutableComponent getTooltip() {
            return Component.translatable("message.supplementaries.double_cake");
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.CAKE;
        }

        private InteractionResult placeDoubleCake(Player player, ItemStack stack, BlockPos pos, Level world, BlockState state, boolean isRanged) {
            boolean isDirectional = state.getBlock() == ModRegistry.DIRECTIONAL_CAKE.get();

            if ((isDirectional && state.getValue(DirectionalCakeBlock.BITES) == 0) || state == net.minecraft.world.level.block.Blocks.CAKE.defaultBlockState()) {

                return replaceSimilarBlock(ModRegistry.DOUBLE_CAKE.get(), player, stack, pos, world, state,
                        null, DoubleCakeBlock.FACING);
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                BlockPos pos = hit.getBlockPos();
                BlockState state = world.getBlockState(pos);
                Block b = state.getBlock();
                if (b == net.minecraft.world.level.block.Blocks.CAKE || b == ModRegistry.DIRECTIONAL_CAKE.get()) {
                    InteractionResult result = InteractionResult.FAIL;

                    if (CommonConfigs.Tweaks.DOUBLE_CAKE_PLACEMENT.get()) {
                        result = placeDoubleCake(player, stack, pos, world, state, isRanged);
                    }
                    return result;
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class SoapBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return RegistryConfigs.SOAP_ENABLED.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == ModRegistry.SOAP.get();
        }

        @Override
        public InteractionResult tryPerformingAction(Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                BlockPos pos = hit.getBlockPos();
                if (SoapWashableHelper.tryWash(level, pos, level.getBlockState(pos))) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        if (!player.getAbilities().instabuild) stack.shrink(1);

                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                        level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);

                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class AntiqueInkBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return AntiqueInkHelper.isEnabled();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.INK_SAC || item == ModRegistry.ANTIQUE_INK.get();
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                boolean newState = !stack.is(Items.INK_SAC);
                BlockPos pos = hit.getBlockPos();
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile != null && (!(tile instanceof IOwnerProtected op) || op.isAccessibleBy(player))) {
                    if (AntiqueInkHelper.toggleAntiqueInkOnSigns(world, player, stack, newState, pos, tile)) {
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    //needed to suppress block actions, so we can always rotate a block even if for example it would open an inventory normally
    private static class WrenchBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return RegistryConfigs.WRENCH_ENABLED.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == ModRegistry.WRENCH.get();
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                var h = CommonConfigs.Items.WRENCH_BYPASS.get();
                if ((h == CommonConfigs.Hands.MAIN_HAND && hand == InteractionHand.MAIN_HAND) ||
                        (h == CommonConfigs.Hands.OFF_HAND && hand == InteractionHand.OFF_HAND) || h == CommonConfigs.Hands.BOTH) {

                    return stack.useOn(new UseOnContext(player, hand, hit));
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class SkullPileBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public MutableComponent getTooltip() {
            return Component.translatable("message.supplementaries.double_cake");
        }

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.SKULL_PILES.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item instanceof BlockItem bi && bi.getBlock() instanceof SkullBlock skull &&
                    skull.getType() != SkullBlock.Types.DRAGON && skull.getType() != EndermanSkullBlock.TYPE;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                BlockPos pos = hit.getBlockPos();

                if (world.getBlockEntity(pos) instanceof SkullBlockEntity oldTile) {
                    BlockState state = oldTile.getBlockState();
                    if ((state.getBlock() instanceof SkullBlock skullBlock && skullBlock.getType() != SkullBlock.Types.DRAGON)) {

                        ItemStack copy = stack.copy();

                        InteractionResult result = replaceSimilarBlock(ModRegistry.SKULL_PILE.get(), player, stack, pos, world,
                                state, null, SkullBlock.ROTATION);

                        if (result.consumesAction()) {
                            if (world.getBlockEntity(pos) instanceof DoubleSkullBlockTile tile) {
                                tile.initialize(oldTile, skullBlock, copy, player, hand);
                            }
                        }
                        return result;
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class SkullCandlesBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return CommonConfigs.Tweaks.SKULL_CANDLES.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            if (item.builtInRegistryHolder().is(ItemTags.CANDLES)) {
                var n = Utils.getID(item).getNamespace();
                return (n.equals("minecraft") || n.equals("tinted") || item == CompatObjects.SOUL_CANDLE_ITEM.get());
            }
            return false;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                BlockPos pos = hit.getBlockPos();

                if (world.getBlockEntity(pos) instanceof SkullBlockEntity oldTile) {
                    BlockState state = oldTile.getBlockState();
                    if ((state.getBlock() instanceof SkullBlock skullBlock && skullBlock.getType() != SkullBlock.Types.DRAGON)) {

                        ItemStack copy = stack.copy();

                        Block b;
                        if (CompatHandler.BUZZIER_BEES && stack.getItem() == CompatObjects.SOUL_CANDLE_ITEM.get()) {
                            b = ModRegistry.SKULL_CANDLE_SOUL.get();
                        } else b = ModRegistry.SKULL_CANDLE.get();

                        InteractionResult result = replaceSimilarBlock(b, player, stack, pos, world,
                                state, SoundType.CANDLE, SkullBlock.ROTATION);

                        if (result.consumesAction()) {
                            if (world.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
                                tile.initialize(oldTile, skullBlock, copy, player, hand);
                            }
                        }
                        return result;
                    }
                }
            }
            return InteractionResult.PASS;
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
