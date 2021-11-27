package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DirectionalCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.StaticBlockItem;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.items.FullJarItem;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;


public class ItemsOverrideHandler {

    private static final Map<Item, ItemUseOnBlockOverride> HIGH_PRIORITY_OVERRIDES = new HashMap<>();

    private static final Map<Item, ItemUseOnBlockOverride> ON_BLOCK_OVERRIDES = new HashMap<>();

    private static final Map<Item, ItemUseOnBlockOverride> ITEM_OVERRIDES = new HashMap<>();

    public static boolean hasBlockOverride(Item item) {
        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        return override != null && override.getPlacedBlock(item) != null;
    }

    public static void registerOverrides() {

        List<ItemUseOnBlockOverride> HPBlockBehaviors = new ArrayList<>();
        List<ItemUseOnBlockOverride> itemBehaviors = new ArrayList<>();
        List<ItemUseOnBlockOverride> blockBehaviors = new ArrayList<>();

        HPBlockBehaviors.add(new WallLanternBehavior());

        blockBehaviors.add(new WallLanternBehavior());
        blockBehaviors.add(new MapMarkerBehavior());
        blockBehaviors.add(new CeilingBannersBehavior());
        blockBehaviors.add(new HangingPotBehavior());
        blockBehaviors.add(new EnhancedCakeBehavior());
        blockBehaviors.add(new XpBottlingBehavior());
        blockBehaviors.add(new PlaceableGunpowderBehavior());
        blockBehaviors.add(new BookPileBehavior());
        blockBehaviors.add(new BookPileHorizontalBehavior());
        blockBehaviors.add(new PlaceableRodsBehavior());
        blockBehaviors.add(new PlaceableSticksBehavior(ModRegistry.STICK_BLOCK, Items.STICK));

        PlaceableSticksBehavior.optional(ModRegistry.PRISMARINE_ROD_BLOCK, ModRegistry.PRISMARINE_ROD_BLOCK.get().getStickItem())
                .ifPresent(blockBehaviors::add);
        PlaceableSticksBehavior.optional(ModRegistry.PROPELPLANT_ROD_BLOCK, ModRegistry.PROPELPLANT_ROD_BLOCK.get().getStickItem())
                .ifPresent(blockBehaviors::add);
        PlaceableSticksBehavior.optional(ModRegistry.EDELWOOD_STICK_BLOCK, ModRegistry.EDELWOOD_STICK_BLOCK.get().getStickItem())
                .ifPresent(blockBehaviors::add);


        for (Item i : ForgeRegistries.ITEMS) {
            for (ItemUseOnBlockOverride b : blockBehaviors) {
                try {
                    if (b.appliesToItem(i)) {
                        //adds item to block item map
                        Block block = b.getPlacedBlock(i);
                        if (block != null && b.shouldBlockMapToItem(i)) Item.BY_BLOCK.put(block, i);
                        ON_BLOCK_OVERRIDES.put(i, b);
                        break;
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("failed to register for override " + b.getClass().getSimpleName() + " for " + i.getRegistryName() + " with exception: " + e);
                }
            }
            for (ItemUseOnBlockOverride b : itemBehaviors) {
                if (b.appliesToItem(i)) {
                    ITEM_OVERRIDES.put(i, b);
                    break;
                }

            }
            for (ItemUseOnBlockOverride b : HPBlockBehaviors) {
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

        ItemUseOnBlockOverride override = HIGH_PRIORITY_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            ActionResultType result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, event.getHitVec(), false);
            if (result != ActionResultType.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }


    public static boolean tryPerformOverride(PlayerInteractEvent.RightClickBlock event, ItemStack stack, boolean isRanged) {
        Item item = stack.getItem();

        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            ActionResultType result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, event.getHitVec(), isRanged);
            if (result != ActionResultType.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
                return true;
            }

        }
        return false;
    }

    public static void tryPerformOverride(PlayerInteractEvent.RightClickItem event, ItemStack stack, boolean isRanged) {
        Item item = stack.getItem();

        ItemUseOnBlockOverride override = ITEM_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            ActionResultType result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, null, isRanged);
            if (result != ActionResultType.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }

        }
    }

    public static void addOverrideTooltips(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();

        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {
            List<ITextComponent> tooltip = event.getToolTip();
            TextComponent t = override.getTooltip();
            if (t != null) tooltip.add(t.withStyle(TextFormatting.DARK_GRAY).withStyle(TextFormatting.ITALIC));
        }
        //TODO: add these
        else if (ServerConfigs.cached.THROWABLE_BRICKS_ENABLED && CommonUtil.isBrick(item)) {
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.throwable_brick").withStyle(TextFormatting.DARK_GRAY).withStyle(TextFormatting.ITALIC));
        }
    }


    private static abstract class ItemUseOnBlockOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToItem(Item item);

        public boolean shouldBlockMapToItem(Item item) {
            return appliesToItem(item);
        }

        //if this item can place a block
        @Nullable
        public Block getPlacedBlock(Item i) {
            return null;
        }

        @Nullable
        public TextComponent getTooltip() {
            return null;
        }

        public abstract ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand,
                                                             ItemStack stack, @Nullable BlockRayTraceResult hit, boolean isRanged);
    }

    private static class MapMarkerBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.MAP_MARKERS;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item instanceof FilledMapItem;
        }

        private final List<Block> BLOCK_MARKERS = Arrays.asList(Blocks.LODESTONE, Blocks.NETHER_PORTAL, Blocks.BEACON,
                Blocks.CONDUIT, Blocks.RESPAWN_ANCHOR, Blocks.END_GATEWAY, Blocks.END_PORTAL);

        @Override
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            BlockPos pos = hit.getBlockPos();
            Block b = world.getBlockState(pos).getBlock();
            if (b instanceof BedBlock || BLOCK_MARKERS.contains(b)) {
                if (!world.isClientSide) {
                    MapData data = FilledMapItem.getOrCreateSavedData(stack, world);
                    if (data instanceof CustomDecorationHolder) {
                        ((CustomDecorationHolder) data).toggleCustomDecoration(world, pos);
                    }
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
            return ActionResultType.PASS;
        }
    }

    private static class XpBottlingBehavior extends ItemUseOnBlockOverride {

        private static final JarBlockTile DUMMY_JAR_TILE = new JarBlockTile();

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.BOTTLE_XP;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == Items.GLASS_BOTTLE || item instanceof FullJarItem || item instanceof JarItem || item == Items.EXPERIENCE_BOTTLE;
        }

        @Override
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {

            BlockPos pos = hit.getBlockPos();
            Item i = stack.getItem();
            if (world.getBlockState(pos).getBlock() instanceof EnchantingTableBlock) {
                ItemStack returnStack = null;

                //prevent accidentally releasing bottles
                if (i == Items.EXPERIENCE_BOTTLE) {
                    return ActionResultType.FAIL;
                }

                if (player.experienceLevel > 0 || player.isCreative()) {
                    if (i == Items.GLASS_BOTTLE) {
                        returnStack = new ItemStack(Items.EXPERIENCE_BOTTLE);
                    } else if (i instanceof JarItem || i instanceof FullJarItem) {
                        DUMMY_JAR_TILE.resetHolders();
                        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
                        if (compoundnbt != null) {
                            DUMMY_JAR_TILE.load(((BlockItem) i).getBlock().defaultBlockState(), compoundnbt);
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
                            player.giveExperiencePoints(-Utils.getXPinaBottle(1, world.random) - 1);

                        if (world.isClientSide) {
                            Minecraft.getInstance().particleEngine.createTrackingEmitter(player, ModRegistry.BOTTLING_XP_PARTICLE.get(), 1);
                        }
                        world.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 1, 1);

                        return ActionResultType.sidedSuccess(world.isClientSide);
                    }
                }
            }
            return ActionResultType.PASS;
        }
    }

    private static class EnhancedCakeBehavior extends ItemUseOnBlockOverride {


        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.double_cake");
        }

        @Override
        public Block getPlacedBlock(Item i) {
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

        private ActionResultType placeDoubleCake(PlayerEntity player, ItemStack stack, BlockPos pos, World world, BlockState state, boolean isRanged) {
            boolean isDirectional = state.getBlock() == ModRegistry.DIRECTIONAL_CAKE.get();

            if ((isDirectional && state.getValue(DirectionalCakeBlock.BITES) == 0) || state == Blocks.CAKE.defaultBlockState()) {

                return replaceSimilarBlock(ModRegistry.DOUBLE_CAKE.get(), player, stack, pos, world, state, isRanged,
                        null, DoubleCakeBlock.FACING);
            }
            return ActionResultType.PASS;
        }

        @Override
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                BlockPos pos = hit.getBlockPos();
                BlockState state = world.getBlockState(pos);
                Block b = state.getBlock();
                if (b == Blocks.CAKE || b == ModRegistry.DIRECTIONAL_CAKE.get()) {
                    ActionResultType result = ActionResultType.FAIL;

                    if (ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT) {
                        result = placeDoubleCake(player, stack, pos, world, state, isRanged);
                    }
                    if (!result.consumesAction() && ServerConfigs.cached.DIRECTIONAL_CAKE) {
                        result = paceBlockOverride(ModRegistry.DIRECTIONAL_CAKE.get(), player, hand, stack, world, hit, isRanged);
                    }
                    return result;
                }
            }
            return ActionResultType.PASS;
        }
    }

    private static class CeilingBannersBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public Block getPlacedBlock(Item i) {
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.CEILING_BANNERS.get(((BannerItem) stack.getItem()).getColor()).get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class HangingPotBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.hanging_pot");
        }

        @Nullable
        @Override
        public Block getPlacedBlock(Item i) {
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.HANGING_FLOWER_POT.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class PlaceableSticksBehavior<T extends Block> extends ItemUseOnBlockOverride {

        private final Supplier<T> block;
        private final Item item;

        private PlaceableSticksBehavior(Supplier<T> block, Item item) {
            this.block = block;
            this.item = item;
        }

        private static <A extends Block> Optional<PlaceableSticksBehavior<A>> optional(Supplier<A> block, Item item) {
            if (item != null && item != Items.AIR) {
                return Optional.of(new PlaceableSticksBehavior<>(block, item));
            }
            return Optional.empty();
        }

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getPlacedBlock(Item i) {
            return this.block.get();
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.PLACEABLE_STICKS;
        }

        @Override
        public boolean appliesToItem(Item i) {
            return i == this.item;
        }

        @Override
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(block.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class PlaceableRodsBehavior extends PlaceableSticksBehavior {

        private PlaceableRodsBehavior() {
            super(ModRegistry.BLAZE_ROD_BLOCK, Items.BLAZE_ROD);
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.PLACEABLE_RODS;
        }
    }

    private static class PlaceableGunpowderBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getPlacedBlock(Item i) {
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.GUNPOWDER_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class BookPileHorizontalBehavior extends ItemUseOnBlockOverride {

        //hax. I'll leave this here and see what happens
        private static final Item BOOK_PILE_H_ITEM = new BlockItem(ModRegistry.BOOK_PILE_H.get(), (new Item.Properties()).tab(null));

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getPlacedBlock(Item i) {
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                //require shift for written books
                if (BookPileBlock.isWrittenBook(stack.getItem()) && !player.isShiftKeyDown())
                    return ActionResultType.PASS;
                return paceBlockOverride(BOOK_PILE_H_ITEM, player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class BookPileBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
        }

        @Nullable
        @Override
        public Block getPlacedBlock(Item i) {
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                if (BookPileBlock.isWrittenBook(stack.getItem()) && !player.isShiftKeyDown())
                    return ActionResultType.PASS;
                return paceBlockOverride(ModRegistry.BOOK_PILE.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class WallLanternBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.wall_lantern");
        }

        @Nullable
        @Override
        public Block getPlacedBlock(Item i) {
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                if (CompatHandler.torchslab) {
                    double y = hit.getLocation().y() % 1;
                    if (y < 0.5) return ActionResultType.PASS;
                }
                return paceBlockOverride(ModRegistry.WALL_LANTERN.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static ActionResultType paceBlockOverride(Item itemOverride, PlayerEntity player, Hand hand, ItemStack heldStack,
                                                      World world, BlockRayTraceResult raytrace, boolean isRanged) {
        if (raytrace.getDirection() != null) {
            //try interacting with block behind
            BlockPos pos = raytrace.getBlockPos();

            ActionResultType result = ActionResultType.PASS;

            if (!player.isShiftKeyDown() && !isRanged) {
                BlockState blockstate = world.getBlockState(pos);
                result = blockstate.use(world, player, hand, raytrace);
            }

            if (!result.consumesAction()) {

                //place block
                BlockItemUseContext ctx = new BlockItemUseContext(world, player, hand, heldStack, raytrace);

                if (itemOverride instanceof BlockItem) {
                    result = ((BlockItem) itemOverride).place(ctx);

                }
            }
            if (result.consumesAction() && player instanceof ServerPlayerEntity && !isRanged) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, heldStack);
            }
            if (result == ActionResultType.FAIL) return ActionResultType.PASS;
            return result;
        }
        return ActionResultType.PASS;
    }

    private static ActionResultType paceBlockOverride(Block blockOverride, PlayerEntity player, Hand hand, ItemStack heldStack,
                                                      World world, BlockRayTraceResult raytrace, boolean isRanged) {
        if (raytrace.getDirection() != null) {
            //try interacting with block behind
            BlockPos pos = raytrace.getBlockPos();

            ActionResultType result = ActionResultType.PASS;

            if (!player.isShiftKeyDown() && !isRanged) {
                BlockState blockstate = world.getBlockState(pos);
                result = blockstate.use(world, player, hand, raytrace);
            }

            if (!result.consumesAction()) {

                //place block
                BlockItemUseContext ctx = new BlockItemUseContext(world, player, hand, heldStack, raytrace);

                result = StaticBlockItem.place(ctx, blockOverride);
            }
            if (result.consumesAction() && player instanceof ServerPlayerEntity && !isRanged) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, heldStack);
            }
            if (result == ActionResultType.FAIL) return ActionResultType.PASS;
            return result;
        }
        return ActionResultType.PASS;
    }

    private static ActionResultType replaceSimilarBlock(Block blockOverride, PlayerEntity player, ItemStack stack,
                                                        BlockPos pos, World world, BlockState replaced,
                                                        boolean isRanged, @Nullable SoundType sound, Property<?>... properties) {

        BlockState newState = blockOverride.defaultBlockState();

        for (Property<?> p : properties) {
            newState = BlockUtils.replaceProperty(replaced, newState, p);
        }
        if (newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            FluidState fluidstate = world.getFluidState(pos);
            newState = newState.setValue(BlockStateProperties.WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);
        }
        if (!world.setBlock(pos, newState, 3)) {
            return ActionResultType.FAIL;
        }
        if (player instanceof ServerPlayerEntity) {
            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
        }
        if (sound == null) sound = newState.getSoundType(world, pos, player);
        world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player == null || !player.abilities.instabuild) {
            stack.shrink(1);
        }
        if (player instanceof ServerPlayerEntity && !isRanged) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
        }
        return ActionResultType.sidedSuccess(world.isClientSide);

    }

}
