package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.blocks.BookPileBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DirectionalCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
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
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
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


public class ItemsOverrideHandler {

    private static final Map<Item, ItemInteractionOverride> ON_BLOCK_OVERRIDES = new HashMap<>();

    private static final Map<Item, ItemInteractionOverride> ITEM_OVERRIDES = new HashMap<>();

    public static boolean hasBlockOverride(Item item) {
        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        return override != null && override.getBlockOverride(item) != null;
    }

    public static void registerOverrides() {
        List<ItemInteractionOverride> itemBehaviors = new ArrayList<>();
        List<ItemInteractionOverride> blockBehaviors = new ArrayList<>();
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
                if (b.appliesToItem(i)) {
                    //adds item to block item map
                    Block block = b.getBlockOverride(i);
                    if (b != null) Item.BY_BLOCK.put(block, i);
                    ON_BLOCK_OVERRIDES.put(i, b);
                    break;
                }
            }
            for (ItemInteractionOverride b : itemBehaviors) {
                if (b.appliesToItem(i)) {
                    ITEM_OVERRIDES.put(i, b);
                    break;
                }
            }
        }
    }

    public static void tryPerformOverride(PlayerInteractEvent.RightClickBlock event, ItemStack stack, boolean isRanged) {
        Item item = stack.getItem();

        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            ActionResultType result = override.tryPerformingAction(event.getWorld(), event.getPlayer(), event.getHand(), stack, event.getHitVec(), isRanged);
            if (result != ActionResultType.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }

        }
    }

    public static void tryPerformOverride(PlayerInteractEvent.RightClickItem event, ItemStack stack, boolean isRanged) {
        Item item = stack.getItem();

        ItemInteractionOverride override = ITEM_OVERRIDES.get(item);
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

        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {
            List<ITextComponent> tooltip = event.getToolTip();
            TextComponent t = override.getTooltip();
            if (t != null) tooltip.add(t.withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
        }
        //TODO: add these
        else if (ServerConfigs.cached.THROWABLE_BRICKS_ENABLED && CommonUtil.isBrick(item)) {
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.throwable_brick").withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
        }
    }


    private static abstract class ItemInteractionOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToItem(Item item);

        //if this item can place a block
        @Nullable
        public Block getBlockOverride(Item i) {
            return null;
        }

        @Nullable
        public TextComponent getTooltip() {
            return null;
        }

        public abstract ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand,
                                                             ItemStack stack, @Nullable BlockRayTraceResult hit, boolean isRanged);
    }

    private static class MapMarkerBehavior extends ItemInteractionOverride {

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

    private static class XpBottlingBehavior extends ItemInteractionOverride {

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
                            player.giveExperiencePoints(-Utils.getXPinaBottle(1, world.random));

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

    private static class EnhancedCakeBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.double_cake");
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

        private ActionResultType placeDoubleCake(PlayerEntity player, ItemStack stack, BlockPos pos, World world, BlockState state, boolean isRanged) {
            boolean isDirectional = state.getBlock() == ModRegistry.DIRECTIONAL_CAKE.get();

            if ((isDirectional && state.getValue(DirectionalCakeBlock.BITES) == 0) || state == Blocks.CAKE.defaultBlockState()) {
                BlockState newState = ModRegistry.DOUBLE_CAKE.get().defaultBlockState()
                        .setValue(DoubleCakeBlock.FACING, isDirectional ? state.getValue(DoubleCakeBlock.FACING) : Direction.WEST)
                        .setValue(DoubleCakeBlock.WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
                if (!world.setBlock(pos, newState, 3)) {
                    return ActionResultType.FAIL;
                }
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                SoundType soundtype = newState.getSoundType(world, pos, player);
                world.playSound(player, pos, newState.getSoundType(world, pos, player).getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (player == null || !player.abilities.instabuild) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayerEntity && !isRanged) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.CEILING_BANNERS.get(((BannerItem) stack.getItem()).getColor()).get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class HangingPotBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.hanging_pot");
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.HANGING_FLOWER_POT.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class PlaceableSticksBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.STICK_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class PlaceableRodsBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.BLAZE_ROD_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class PlaceableGunpowderBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
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
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.GUNPOWDER_BLOCK.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class BookPileHorizontalBehavior extends ItemInteractionOverride {

        //hax. I'll leave this here and see what happens
        private static final Item BOOK_PILE_H_ITEM =  new BlockItem(ModRegistry.BOOK_PILE_H.get(), (new Item.Properties()).tab(null));

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
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
            return ((BookPileBlock)ModRegistry.BOOK_PILE_H.get()).isAcceptedItem(item);
        }

        @Override
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(BOOK_PILE_H_ITEM, player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class BookPileBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.placeable");
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
            return ((BookPileBlock)ModRegistry.BOOK_PILE.get()).isAcceptedItem(item);
        }

        @Override
        public ActionResultType tryPerformingAction(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit, boolean isRanged) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(ModRegistry.BOOK_PILE.get(), player, hand, stack, world, hit, isRanged);
            }
            return ActionResultType.PASS;
        }
    }

    private static class WallLanternBehavior extends ItemInteractionOverride {

        @Nullable
        @Override
        public TextComponent getTooltip() {
            return new TranslationTextComponent("message.supplementaries.wall_lantern");
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

}
