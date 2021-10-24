package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DirectionalCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.BlockItemUtils;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class ItemsOverrideHandler {

    private static final Map<Item, ItemInteractionOverride> HIGH_PRIORITY_OVERRIDES = new HashMap<>();

    private static final Map<Item, ItemInteractionOverride> ON_BLOCK_OVERRIDES = new HashMap<>();

    private static final Map<Item, ItemInteractionOverride> ITEM_OVERRIDES = new HashMap<>();

    public static boolean hasBlockOverride(Item item) {
        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        return override != null && override.getBlockOverride(item) != null;
    }

    public static void registerOverrides() {

        List<ItemInteractionOverride> HPBlockBehaviors = new ArrayList<>();
        List<ItemInteractionOverride> itemBehaviors = new ArrayList<>();
        List<ItemInteractionOverride> blockBehaviors = new ArrayList<>();

        HPBlockBehaviors.add(new WallLanternBehavior());

        blockBehaviors.add(new WallLanternBehavior());
        blockBehaviors.add(new MapMarkerBehavior());
        blockBehaviors.add(new CeilingBannersBehavior());
        blockBehaviors.add(new HangingPotBehavior());
        blockBehaviors.add(new EnhancedCakeBehavior());
        blockBehaviors.add(new PlaceableSticksBehavior());
        blockBehaviors.add(new PlaceableRodsBehavior());
        blockBehaviors.add(new XpBottlingBehavior());
        blockBehaviors.add(new PlaceableGunpowderBehavior());
        blockBehaviors.add(new BookPileBehavior());
        blockBehaviors.add(new BookPileHorizontalBehavior());

        for (Item i : ForgeRegistries.ITEMS) {
            for (ItemInteractionOverride b : blockBehaviors) {
                try {
                    if (b.appliesToItem(i)) {
                        //adds item to block item map
                        Block block = b.getBlockOverride(i);
                        if (block != null && b.shouldBlockMapToItem(i)) Item.BY_BLOCK.put(block, i);
                        ON_BLOCK_OVERRIDES.put(i, b);
                        break;
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("failed to register for override " + b.getClass().getSimpleName() + " for " + i.getRegistryName() + " with exception: " + e);
                }
            }
            for (ItemInteractionOverride b : itemBehaviors) {
                if (b.appliesToItem(i)) {
                    ITEM_OVERRIDES.put(i, b);
                    break;
                }

            }
            for (ItemInteractionOverride b : HPBlockBehaviors) {
                try {
                    if (b.appliesToItem(i)) {
                        HIGH_PRIORITY_OVERRIDES.put(i, b);
                        break;
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("failed to register for override " + b.getClass().getSimpleName() + " for " + i.getRegistryName() + " with exception: " + e);
                }
            }
        }
    }

    public static void tryHighPriorityOverride(PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        Item item = stack.getItem();

        ItemInteractionOverride override = HIGH_PRIORITY_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            InteractionResult result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, event.getHitVec(), false);
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }


    public static boolean tryPerformOverride(PlayerInteractEvent.RightClickBlock event, ItemStack stack, boolean isRanged) {
        Item item = stack.getItem();

        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            InteractionResult result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, event.getHitVec(), isRanged);
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
                return true;
            }

        }
        return false;
    }

    public static void tryPerformOverride(PlayerInteractEvent.RightClickItem event, ItemStack stack, boolean isRanged) {
        Item item = stack.getItem();

        ItemInteractionOverride override = ITEM_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            InteractionResult result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, null, isRanged);
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }

        }
    }

    public static void addOverrideTooltips(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();

        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {
            List<Component> tooltip = event.getToolTip();
            BaseComponent t = override.getTooltip();
            if (t != null) tooltip.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
        //TODO: add these
        else if (ServerConfigs.cached.THROWABLE_BRICKS_ENABLED && CommonUtil.isBrick(item)) {
            event.getToolTip().add(new TranslatableComponent("message.supplementaries.throwable_brick").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }


    private static abstract class ItemInteractionOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToItem(Item item);

        public boolean shouldBlockMapToItem(Item item) {
            return appliesToItem(item);
        }

        //if this item can place a block
        @Nullable
        public Block getBlockOverride(Item i) {
            return null;
        }

        @Nullable
        public BaseComponent getTooltip() {
            return null;
        }

        public abstract InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                              ItemStack stack, @Nullable BlockHitResult hit, boolean isRanged);
    }

    private static class MapMarkerBehavior extends ItemInteractionOverride {

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.MAP_MARKERS;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item instanceof MapItem;
        }

        private final List<Block> BLOCK_MARKERS = Arrays.asList(Blocks.LODESTONE, Blocks.NETHER_PORTAL, Blocks.BEACON,
                Blocks.CONDUIT, Blocks.RESPAWN_ANCHOR, Blocks.END_GATEWAY, Blocks.END_PORTAL);

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            BlockPos pos = hit.getBlockPos();
            Block b = world.getBlockState(pos).getBlock();
            if (b instanceof BedBlock || BLOCK_MARKERS.contains(b)) {
                if (!world.isClientSide) {
                    if (MapItem.getSavedData(stack, world) instanceof ExpandedMapData data) {
                        data.toggleCustomDecoration(world, pos);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.PASS;
        }
    }

    private static class XpBottlingBehavior extends ItemInteractionOverride {

        private static final JarBlockTile DUMMY_JAR_TILE = new JarBlockTile(BlockPos.ZERO, ModRegistry.JAR_TINTED.get().defaultBlockState());

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.BOTTLE_XP;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.GLASS_BOTTLE || item instanceof JarItem || item == Items.EXPERIENCE_BOTTLE;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {

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
                        DUMMY_JAR_TILE.resetHolders();
                        CompoundTag tag = stack.getTagElement("BlockEntityTag");
                        if (tag != null) {
                            DUMMY_JAR_TILE.load(tag);
                        }

                        if (DUMMY_JAR_TILE.canInteractWithFluidHolder()) {
                            ItemStack tempStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
                            ItemStack temp = DUMMY_JAR_TILE.fluidHolder.interactWithItem(tempStack, null, null, false);
                            if (temp != null && temp.getItem() == Items.GLASS_BOTTLE) {
                                returnStack = ((JarBlock) ((BlockItem) i).getBlock()).getJarItem(DUMMY_JAR_TILE);
                            }
                        }
                    }

                    if (returnStack != null) {
                        player.hurt(CommonUtil.BOTTLING_DAMAGE, ServerConfigs.cached.BOTTLING_COST);
                        Utils.swapItem(player, hand, returnStack);

                        if (!player.isCreative())
                            player.giveExperiencePoints(-Utils.getXPinaBottle(1, world.random));

                        if (world.isClientSide) {
                            Minecraft.getInstance().particleEngine.createTrackingEmitter(player, ModRegistry.BOTTLING_XP_PARTICLE.get(), 1);
                        }
                        world.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 1, 1);

                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class EnhancedCakeBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.double_cake");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.DOUBLE_CAKE.get();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return CommonUtil.isCake(item);
        }

        private InteractionResult placeDoubleCake(Player player, ItemStack stack, BlockPos pos, Level world, BlockState state, boolean isRanged) {
            boolean isDirectional = state.getBlock() == ModRegistry.DIRECTIONAL_CAKE.get();

            if ((isDirectional && state.getValue(DirectionalCakeBlock.BITES) == 0) || state == Blocks.CAKE.defaultBlockState()) {
                BlockState newState = ModRegistry.DOUBLE_CAKE.get().defaultBlockState()
                        .setValue(DoubleCakeBlock.FACING, isDirectional ? state.getValue(DoubleCakeBlock.FACING) : Direction.WEST)
                        .setValue(DoubleCakeBlock.WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
                if (!world.setBlock(pos, newState, 3)) {
                    return InteractionResult.FAIL;
                }
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
                }
                SoundType soundtype = newState.getSoundType(world, pos, player);
                world.playSound(player, pos, newState.getSoundType(world, pos, player).getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (player == null || !player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayer && !isRanged) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.PASS;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                BlockPos pos = hit.getBlockPos();
                BlockState state = world.getBlockState(pos);
                Block b = state.getBlock();
                if (b == Blocks.CAKE || b == ModRegistry.DIRECTIONAL_CAKE.get()) {
                    InteractionResult result = InteractionResult.FAIL;

                    if (ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT) {
                        result = placeDoubleCake(player, stack, pos, world, state, isRanged);
                    }
                    if (!result.consumesAction() && ServerConfigs.cached.DIRECTIONAL_CAKE) {
                        result = paceBlockOverride(ModRegistry.DIRECTIONAL_CAKE.get(), player, hand, stack, world, hit, isRanged);
                    }
                    return result;
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class CeilingBannersBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            if (i instanceof BannerItem) {
                return ModRegistry.CEILING_BANNERS.get(((BannerItem) i).getColor()).get();
            }
            return null;
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.CEILING_BANNERS;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item instanceof BannerItem;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.CEILING_BANNERS.get(((BannerItem) stack.getItem()).getColor()).get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class HangingPotBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.hanging_pot");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.HANGING_FLOWER_POT.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.HANGING_POT_PLACEMENT;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return CommonUtil.isPot(item);
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.HANGING_FLOWER_POT.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class PlaceableSticksBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.STICK_BLOCK.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.PLACEABLE_STICKS;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.STICK;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.STICK_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class PlaceableRodsBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.BLAZE_ROD_BLOCK.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.PLACEABLE_RODS;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.BLAZE_ROD;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.BLAZE_ROD_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class PlaceableGunpowderBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.GUNPOWDER_BLOCK.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.PLACEABLE_GUNPOWDER;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.GUNPOWDER;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.GUNPOWDER_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class BookPileHorizontalBehavior extends ItemInteractionOverride {

        //hax. I'll leave this here and see what happens
        private static final Item BOOK_PILE_H_ITEM = new BlockItem(ModRegistry.BOOK_PILE_H.get(), (new Item.Properties()).tab(null));

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.BOOK_PILE_H.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.PLACEABLE_BOOKS;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return BookPileBlock.isNormalBook(item);
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(BOOK_PILE_H_ITEM, player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class BookPileBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.BOOK_PILE.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.PLACEABLE_BOOKS;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return BookPileBlock.isEnchantedBook(item);
        }

        @Override
        public boolean shouldBlockMapToItem(Item item) {
            return item == Items.ENCHANTED_BOOK;
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.BOOK_PILE.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class WallLanternBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.wall_lantern");
        }

        @Nullable
        @Override
        public Block getBlockOverride(Item i) {
            return ModRegistry.WALL_LANTERN.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.WALL_LANTERN_PLACEMENT;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return CommonUtil.isLantern(item);
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                if (CompatHandler.torchslab) {
                    double y = hit.getLocation().y() % 1;
                    if (y < 0.5) return InteractionResult.PASS;
                }
                return paceBlockOverride(ModRegistry.WALL_LANTERN.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static InteractionResult paceBlockOverride(Item itemOverride, Player player, InteractionHand hand, ItemStack heldStack,
                                                       Level world, BlockHitResult raytrace, boolean isRanged) {
        //try interacting with block behind
        BlockPos pos = raytrace.getBlockPos();

        InteractionResult result = InteractionResult.PASS;

        if (!player.isShiftKeyDown() && !isRanged) {
            BlockState blockstate = world.getBlockState(pos);
            result = blockstate.use(world, player, hand, raytrace);
        }

        if (!result.consumesAction()) {

            //place block
            BlockPlaceContext ctx = new BlockPlaceContext(world, player, hand, heldStack, raytrace);

            if (itemOverride instanceof BlockItem) {
                result = ((BlockItem) itemOverride).place(ctx);

            }
        }
        if (result.consumesAction() && player instanceof ServerPlayer && !isRanged) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, heldStack);
        }
        if (result == InteractionResult.FAIL) return InteractionResult.PASS;
        return result;
    }

    private static InteractionResult paceBlockOverride(Block blockOverride, Player player, InteractionHand hand, ItemStack heldStack,
                                                       Level world, BlockHitResult raytrace, boolean isRanged) {
        //try interacting with block behind
        BlockPos pos = raytrace.getBlockPos();

        InteractionResult result = InteractionResult.PASS;

        if (!player.isShiftKeyDown() && !isRanged) {
            BlockState blockstate = world.getBlockState(pos);
            result = blockstate.use(world, player, hand, raytrace);
        }

        if (!result.consumesAction()) {

            //place block
            BlockPlaceContext ctx = new BlockPlaceContext(world, player, hand, heldStack, raytrace);

            result = BlockItemUtils.place(ctx, blockOverride);
        }
        if (result.consumesAction() && player instanceof ServerPlayer && !isRanged) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, heldStack);
        }
        if (result == InteractionResult.FAIL) return InteractionResult.PASS;
        return result;
    }

}
