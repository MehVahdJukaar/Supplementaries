package net.mehvahdjukaar.supplementaries.common.events;

import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoubleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.BlockItemUtils;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ItemsOverrideHandler {

    //equivalent to Item.useOnBlock to the item itself (called before that though)
    private static final Map<Item, ItemUseOnBlockOverride> HP_ON_BLOCK_OVERRIDES = new HashMap<>();
    private static final Map<Item, ItemUseOnBlockOverride> ON_BLOCK_OVERRIDES = new HashMap<>();

    //equivalent to Item.use
    private static final Map<Item, ItemUseOverride> ITEM_USE_OVERRIDES = new HashMap<>();

    //equivalent to Block.use
    private static final Map<Block, BlockInteractedWithOverride> BLOCK_USE_OVERRIDES = new HashMap<>();

    public static boolean hasBlockPlacementAssociated(Item item) {
        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        return override != null && override.getPlacedBlock(item) != null;
    }

    public static void registerOverrides() {

        List<ItemUseOnBlockOverride> HPItemActionOnBlock = new ArrayList<>();
        List<ItemUseOnBlockOverride> itemActionOnBlock = new ArrayList<>();

        List<ItemUseOverride> itemAction = new ArrayList<>();

        List<BlockInteractedWithOverride> actionOnBlock = new ArrayList<>();

        actionOnBlock.add(new DirectionalCakeConversionBehavior());
        actionOnBlock.add(new BellChainBehavior());

        itemAction.add(new ThrowableBrickBehavior());
        itemAction.add(new ClockItemBehavior());

        HPItemActionOnBlock.add(new WallLanternBehavior());
        HPItemActionOnBlock.add(new AntiqueInkBehavior());

        itemActionOnBlock.add(new WrenchBehavior());
        itemActionOnBlock.add(new WallLanternBehavior());
        itemActionOnBlock.add(new MapMarkerBehavior());
        itemActionOnBlock.add(new CeilingBannersBehavior());
        itemActionOnBlock.add(new HangingPotBehavior());
        itemActionOnBlock.add(new EnhancedCakeBehavior());
        itemActionOnBlock.add(new XpBottlingBehavior());
        itemActionOnBlock.add(new PlaceableGunpowderBehavior());
        itemActionOnBlock.add(new BookPileBehavior());
        itemActionOnBlock.add(new BookPileHorizontalBehavior());
        itemActionOnBlock.add(new PlaceableRodsBehavior());
        itemActionOnBlock.add(new PlaceableSticksBehavior<>(ModRegistry.STICK_BLOCK, Items.STICK));
        itemActionOnBlock.add(new SkullPileBehavior());
        itemActionOnBlock.add(new SkullCandlesBehavior());

        PlaceableSticksBehavior.optional(ModRegistry.PRISMARINE_ROD_BLOCK, ModRegistry.PRISMARINE_ROD_BLOCK.get().getStickItem())
                .ifPresent(itemActionOnBlock::add);
        PlaceableSticksBehavior.optional(ModRegistry.PROPELPLANT_ROD_BLOCK, ModRegistry.PROPELPLANT_ROD_BLOCK.get().getStickItem())
                .ifPresent(itemActionOnBlock::add);
        PlaceableSticksBehavior.optional(ModRegistry.EDELWOOD_STICK_BLOCK, ModRegistry.EDELWOOD_STICK_BLOCK.get().getStickItem())
                .ifPresent(itemActionOnBlock::add);

        for (Item i : ForgeRegistries.ITEMS) {
            for (ItemUseOnBlockOverride b : itemActionOnBlock) {
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
            for (ItemUseOverride b : itemAction) {
                try {
                    if (b.appliesToItem(i)) {
                        ITEM_USE_OVERRIDES.put(i, b);
                        break;
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("failed to register for override " + b.getClass().getSimpleName() + " for " + i.getRegistryName() + " with exception: " + e);
                }

            }
            for (ItemUseOnBlockOverride b : HPItemActionOnBlock) {
                try {
                    if (b.appliesToItem(i)) {
                        HP_ON_BLOCK_OVERRIDES.put(i, b);
                        break;
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("failed to register for override " + b.getClass().getSimpleName() + " for " + i.getRegistryName() + " with exception: " + e);
                }
            }
        }
        for (Block block : ForgeRegistries.BLOCKS) {
            for (BlockInteractedWithOverride b : actionOnBlock) {
                try {
                    if (b.appliesToBlock(block)) {
                        BLOCK_USE_OVERRIDES.put(block, b);
                        break;
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("failed to register for override " + b.getClass().getSimpleName() + " for " + block.getRegistryName() + " with exception: " + e);
                }
            }
        }
    }

    public static void tryHighPriorityClickedBlockOverride(PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        Item item = stack.getItem();

        ItemUseOnBlockOverride override = HP_ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            InteractionResult result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, event.getHitVec(), false);
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }


    //item clicked on block overrides
    public static void tryPerformClickedBlockOverride(PlayerInteractEvent.RightClickBlock event, ItemStack stack, boolean isRanged) {
        Item item = stack.getItem();
        Player player = event.getPlayer();

        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            InteractionResult result = override.tryPerformingAction(event.getWorld(), player, event.getHand(), stack, event.getHitVec(), isRanged);
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
                return;
            }
        }
        //block overrides behaviors (work for any item)
        if (!player.isShiftKeyDown()) {
            Level world = event.getWorld();
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);

            BlockInteractedWithOverride o = BLOCK_USE_OVERRIDES.get(state.getBlock());
            if (o != null && o.isEnabled()) {

                InteractionResult result = o.tryPerformingAction(state, pos, world, player, event.getHand(), stack, event.getHitVec());
                if (result != InteractionResult.PASS) {
                    event.setCanceled(true);
                    event.setCancellationResult(result);
                }
            }
        }
    }

    //item clicked overrides
    public static void tryPerformClickedItemOverride(PlayerInteractEvent.RightClickItem event, ItemStack stack) {
        Item item = stack.getItem();

        ItemUseOverride override = ITEM_USE_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            InteractionResult result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, null, false);
            if (result != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }

        }
    }

    public static void addOverrideTooltips(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();

        ItemUseOnBlockOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {
            List<Component> tooltip = event.getToolTip();
            BaseComponent t = override.getTooltip();
            if (t != null) tooltip.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        } else {
            ItemUseOverride o = ITEM_USE_OVERRIDES.get(item);
            if (o != null && o.isEnabled()) {
                List<Component> tooltip = event.getToolTip();
                BaseComponent t = o.getTooltip();
                if (t != null) tooltip.add(t.withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
            }
        }

    }


    private static abstract class BlockInteractedWithOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToBlock(Block block);

        public abstract InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player, InteractionHand hand,
                                                              ItemStack stack, BlockHitResult hit);
    }

    private static abstract class ItemUseOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToItem(Item item);

        @Nullable
        public BaseComponent getTooltip() {
            return null;
        }

        public abstract InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                              ItemStack stack, BlockHitResult hit, boolean isRanged);
    }

    private static abstract class ItemUseOnBlockOverride extends ItemUseOverride {

        public boolean shouldBlockMapToItem(Item item) {
            return appliesToItem(item);
        }

        //if this item can place a block. needed for items that dont already have one.
        //TODO: fix block map
        @Nullable
        public Block getPlacedBlock(Item i) {
            return null;
        }

        @Nullable
        public BaseComponent getTooltip() {
            return null;
        }
    }


    private static class ClockItemBehavior extends ItemUseOverride {

        @Override
        public boolean isEnabled() {
            return ClientConfigs.cached.CLOCK_CLICK;
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

    private static class ThrowableBrickBehavior extends ItemUseOverride {

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.THROWABLE_BRICKS_ENABLED;
        }

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.throwable_brick");
        }

        @Override
        public boolean appliesToItem(Item item) {
            return CommonUtil.isBrick(item);
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
            return ServerConfigs.cached.DIRECTIONAL_CAKE;
        }

        @Override
        public boolean appliesToBlock(Block block) {
            return block == Blocks.CAKE || BlockTags.CANDLE_CAKES.contains(block);
        }

        @Override
        public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
            //lets converting to candle cake
            if (state.is(Blocks.CAKE) && (stack.is(ItemTags.CANDLES) || player.getDirection() == Direction.EAST || state.getValue(CakeBlock.BITES) != 0)) {
                return InteractionResult.PASS;
            }
            if (!(ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT && stack.is(Items.CAKE))) {
                //for candles. normal cakes have no drops
                Block.dropResources(state, world, pos);
                BlockState newState = ModRegistry.DIRECTIONAL_CAKE.get().defaultBlockState();
                world.setBlock(pos, newState, 4);
                BlockHitResult raytrace = new BlockHitResult(
                        new Vec3(pos.getX(), pos.getY(), pos.getZ()), hit.getDirection(), pos, false);

                return newState.use(world, player, hand, raytrace);
            }
            //fallback to default cake interaction
            return InteractionResult.PASS;
        }
    }

    private static class BellChainBehavior extends BlockInteractedWithOverride {

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.BELL_CHAIN;
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

    private static class MapMarkerBehavior extends ItemUseOnBlockOverride {

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
            if (b instanceof BedBlock || BLOCK_MARKERS.contains(b) || Tags.Blocks.CHESTS.contains(b) ||
                    world.getFluidState(pos).getType().getRegistryName().toString().equals("betterportals:portal_fluid")) {
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

    private static class XpBottlingBehavior extends ItemUseOnBlockOverride {

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

    private static class EnhancedCakeBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.double_cake");
        }

        @Nullable
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

        private InteractionResult placeDoubleCake(Player player, ItemStack stack, BlockPos pos, Level world, BlockState state, boolean isRanged) {
            boolean isDirectional = state.getBlock() == ModRegistry.DIRECTIONAL_CAKE.get();

            if ((isDirectional && state.getValue(DirectionalCakeBlock.BITES) == 0) || state == Blocks.CAKE.defaultBlockState()) {

                return replaceSimilarBlock(ModRegistry.DOUBLE_CAKE.get(), player, stack, pos, world, state, isRanged,
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
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.CEILING_BANNERS.get(((BannerItem) stack.getItem()).getColor()).get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class HangingPotBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.hanging_pot");
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
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.HANGING_FLOWER_POT.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
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
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
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
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(block.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
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

    private static class AntiqueInkBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return CapabilityHandler.ANTIQUE_CAP_ENABLED;
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
                if (tile != null) {
                    var cap = tile.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP);
                    AtomicBoolean success = new AtomicBoolean(false);
                    cap.ifPresent(c -> {
                        if (c.hasAntiqueInk() != newState) {
                            c.setAntiqueInk(newState);
                            tile.setChanged();
                            if (world instanceof ServerLevel serverLevel) {
                                NetworkHandler.sendToAllInRangeClients(pos, serverLevel, 256,
                                        new ClientBoundSyncAntiqueInk(pos, newState));
                            }
                            success.set(true);
                        }
                    });
                    if (success.get()) {
                        if (newState) {
                            world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        } else {
                            world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        if (!player.isCreative()) stack.shrink(1);
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class PlaceableGunpowderBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
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
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                return paceBlockOverride(ModRegistry.GUNPOWDER_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    //needed to suppress block actions, so we can always rotate a block even if for example it would open an inventory normally
    private static class WrenchBehavior extends ItemUseOnBlockOverride {

        @Override
        public boolean isEnabled() {
            return RegistryConfigs.reg.WRENCH_ENABLED.get();
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item == ModRegistry.WRENCH.get();
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                var h = ServerConfigs.cached.WRENCH_BYPASS;
                if ((h == ServerConfigs.Hands.MAIN_HAND && hand == InteractionHand.MAIN_HAND) ||
                        (h == ServerConfigs.Hands.OFF_HAND && hand == InteractionHand.OFF_HAND) || h == ServerConfigs.Hands.BOTH) {

                    return stack.useOn(new UseOnContext(player, hand, hit));
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class BookPileHorizontalBehavior extends ItemUseOnBlockOverride {

        //hax. I'll leave this here and see what happens
        private static final Item BOOK_PILE_H_ITEM = new BlockItem(ModRegistry.BOOK_PILE_H.get(), (new Item.Properties()).tab(null));

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
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
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                //require shift for written books
                if (BookPileBlock.isWrittenBook(stack.getItem()) && !player.isShiftKeyDown())
                    return InteractionResult.PASS;
                return paceBlockOverride(BOOK_PILE_H_ITEM, player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class BookPileBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.placeable");
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
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            //TODO: maybe replace with player.mayUseItemAt
            if (player.getAbilities().mayBuild) {
                //require shift for written books
                if (BookPileBlock.isWrittenBook(stack.getItem()) && !player.isShiftKeyDown())
                    return InteractionResult.PASS;
                return paceBlockOverride(ModRegistry.BOOK_PILE.get(), player, hand, stack, world, hit, isRanged);
            }
            return InteractionResult.PASS;
        }
    }

    private static class SkullPileBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.double_cake");
        }

        @Override
        public boolean isEnabled() {
            return ServerConfigs.cached.SKULL_PILES;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return item instanceof BlockItem bi && bi.getBlock() instanceof SkullBlock skull && skull.getType() != SkullBlock.Types.DRAGON;
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
                                state, isRanged, null, SkullBlock.ROTATION);

                        if (result.consumesAction()) {
                            if (world.getBlockEntity(pos) instanceof DoubleSkullBlockTile tile) {
                                tile.initialize(oldTile, skullBlock, copy, player);
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
            return ServerConfigs.cached.SKULL_CANDLES;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return ItemTags.CANDLES.contains(item) && item.getRegistryName().getNamespace().equals("minecraft");
        }

        @Override
        public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean isRanged) {
            if (player.getAbilities().mayBuild) {
                BlockPos pos = hit.getBlockPos();

                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof SkullBlockEntity oldTile) {
                    BlockState state = oldTile.getBlockState();
                    if ((state.getBlock() instanceof SkullBlock skullBlock && skullBlock.getType() != SkullBlock.Types.DRAGON)) {

                        ItemStack copy = stack.copy();

                        InteractionResult result = replaceSimilarBlock(ModRegistry.SKULL_CANDLE.get(), player, stack, pos, world,
                                state, isRanged, SoundType.CANDLE, SkullBlock.ROTATION);

                        if (result.consumesAction()) {
                            if (world.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
                                tile.initialize(oldTile, skullBlock, copy, player);
                            }
                        }
                        return result;
                    }
                }
            }
            return InteractionResult.PASS;
        }
    }

    private static class WallLanternBehavior extends ItemUseOnBlockOverride {

        @Nullable
        @Override
        public BaseComponent getTooltip() {
            return new TranslatableComponent("message.supplementaries.wall_lantern");
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
            //call block overrides
            BlockInteractedWithOverride o = BLOCK_USE_OVERRIDES.get(blockstate.getBlock());
            if (o != null && o.isEnabled()) {
                result = o.tryPerformingAction(blockstate, pos, world, player, hand, heldStack, raytrace);
            }

            if (result == InteractionResult.PASS) {
                result = blockstate.use(world, player, hand, raytrace);
            }
        }

        if (!result.consumesAction()) {

            //place block
            BlockPlaceContext ctx = new BlockPlaceContext(world, player, hand, heldStack, raytrace);

            if (itemOverride instanceof BlockItem blockItem) {
                result = blockItem.place(ctx);
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

            //call block overrides
            BlockInteractedWithOverride o = BLOCK_USE_OVERRIDES.get(blockstate.getBlock());
            if (o != null && o.isEnabled()) {
                result = o.tryPerformingAction(blockstate, pos, world, player, hand, heldStack, raytrace);
            }

            if (result == InteractionResult.PASS) {
                result = blockstate.use(world, player, hand, raytrace);
            }

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

    private static InteractionResult replaceSimilarBlock(Block blockOverride, Player player, ItemStack stack,
                                                         BlockPos pos, Level world, BlockState replaced,
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
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
        }
        world.gameEvent(player, GameEvent.BLOCK_PLACE, pos);

        if (sound == null) sound = newState.getSoundType(world, pos, player);
        world.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (player instanceof ServerPlayer serverPlayer && !isRanged) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);

    }

}
