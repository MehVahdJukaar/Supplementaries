package net.mehvahdjukaar.supplementaries.events;

import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.blocks.DirectionalCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.items.BlockHolderItem;
import net.mehvahdjukaar.supplementaries.items.FullJarItem;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
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

    public static void tryPerformOverride(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            ActionResultType result = override.tryPerformingAction(event.getWorld(), event.getPos(), event.getFace(),
                    event.getPlayer(), event.getHand(), stack, event.getHitVec());
            if (result != ActionResultType.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }

        }
    }

    public static void tryPerformOverride(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        ItemInteractionOverride override = ITEM_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {

            ActionResultType result = override.tryPerformingAction(event.getWorld(), event.getPos(), event.getFace(),
                    event.getPlayer(), event.getHand(), stack, null);
            if (result != ActionResultType.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }

        }
    }

    public static void addOverrideTooltips(ItemTooltipEvent event){
        Item item = event.getItemStack().getItem();

        ItemInteractionOverride override = ON_BLOCK_OVERRIDES.get(item);
        if (override != null && override.isEnabled()) {
            List<ITextComponent> tooltip = event.getToolTip();
            TextComponent t = override.getTooltip();
            if(t != null) tooltip.add(t.withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
        }
        //TODO: add these
        else if (ServerConfigs.cached.THROWABLE_BRICKS_ENABLED && CommonUtil.isBrick(item)) {
            event.getToolTip().add(new TranslationTextComponent("message.supplementaries.throwable_brick").withStyle(TextFormatting.GRAY).withStyle(TextFormatting.ITALIC));
        }
    }


    private static abstract class ItemInteractionOverride {

        public abstract boolean isEnabled();

        public abstract boolean appliesToItem(Item item);

        //if this item can place a block. only accepts already checked items
        @Nullable
        public Block getBlockOverride(Item i) {
            return null;
        }

        @Nullable
        public TextComponent getTooltip(){
            return null;
        }

        public abstract ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir,
                                                             PlayerEntity player, Hand hand, ItemStack stack, @Nullable BlockRayTraceResult hit);
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
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
                            Minecraft.getInstance().particleEngine.createTrackingEmitter(player, Registry.BOTTLING_XP_PARTICLE.get(), 1);
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
            return Registry.DOUBLE_CAKE.get();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean appliesToItem(Item item) {
            return CommonUtil.isCake(item);
        }

        private ActionResultType placeDoubleCake(PlayerEntity player, ItemStack stack, BlockPos pos, World world, BlockState state) {
            boolean isDirectional = state.getBlock() == Registry.DIRECTIONAL_CAKE.get();

            if ((isDirectional && state.getValue(DirectionalCakeBlock.BITES) == 0) || state == Blocks.CAKE.defaultBlockState()) {
                BlockState newState = Registry.DOUBLE_CAKE.get().defaultBlockState()
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
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
            return ActionResultType.PASS;
        }

        @Override
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
            if (player.abilities.mayBuild) {
                BlockState state = world.getBlockState(pos);
                Block b = state.getBlock();
                if (b == Blocks.CAKE || b == Registry.DIRECTIONAL_CAKE.get()) {
                    ActionResultType result = ActionResultType.FAIL;

                    if (ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT) {
                        result = placeDoubleCake(player, stack, pos, world, state);
                    }
                    if (!result.consumesAction() && ServerConfigs.cached.DIRECTIONAL_CAKE) {
                        result = paceBlockOverride(Registry.DIRECTIONAL_CAKE_ITEM.get(), player, hand, stack, pos, dir, world);
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
            return Registry.CEILING_BANNERS.get(((BannerItem) i).getColor()).get();
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(Registry.CEILING_BANNERS_ITEMS.get(((BannerItem) stack.getItem()).getColor()).get(), player, hand, stack, pos, dir, world);
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
            return Registry.HANGING_FLOWER_POT.get();
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(Registry.HANGING_FLOWER_POT_ITEM.get(), player, hand, stack, pos, dir, world);
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
            return Registry.STICK_BLOCK.get();
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(Registry.STICK_BLOCK_ITEM.get(), player, hand, stack, pos, dir, world);
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
            return Registry.BLAZE_ROD_BLOCK.get();
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(Registry.BLAZE_ROD_ITEM.get(), player, hand, stack, pos, dir, world);
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
            return Registry.GUNPOWDER_BLOCK.get();
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
            if (player.abilities.mayBuild) {
                return paceBlockOverride(Registry.GUNPOWDER_BLOCK_ITEM.get(), player, hand, stack, pos, dir, world);
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
            return Registry.WALL_LANTERN.get();
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
        public ActionResultType tryPerformingAction(World world, BlockPos pos, Direction dir, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult hit) {
            if (player.abilities.mayBuild) {
                if (CompatHandler.torchslab) {
                    double y = hit.getLocation().y() % 1;
                    if (y < 0.5) return ActionResultType.FAIL;
                }
                return paceBlockOverride(Registry.WALL_LANTERN_ITEM.get(), player, hand, stack, pos, dir, world);
            }
            return ActionResultType.PASS;
        }
    }

    //TODO: improve
    private static ActionResultType paceBlockOverride(Item itemOverride, PlayerEntity player, Hand hand,
                                                      ItemStack heldStack, BlockPos pos, Direction dir, World world) {
        if (dir != null) {
            //try interacting with block behind
            BlockState blockstate = world.getBlockState(pos);
            BlockRayTraceResult raytrace = new BlockRayTraceResult(
                    new Vector3d(pos.getX(), pos.getY(), pos.getZ()), dir, pos, false);

            ActionResultType result = ActionResultType.PASS;

            if (!player.isShiftKeyDown()) {
                result = blockstate.use(world, player, hand, raytrace);
            }

            if (!result.consumesAction()) {

                //place block
                BlockItemUseContext ctx = new BlockItemUseContext(new ItemUseContext(player, hand, raytrace));

                if (itemOverride instanceof BlockHolderItem && heldStack.getItem() instanceof BlockItem) {
                    result = ((BlockHolderItem) itemOverride).tryPlace(ctx, ((BlockItem) heldStack.getItem()).getBlock());
                } else if (itemOverride instanceof BlockItem) {
                    result = ((BlockItem) itemOverride).place(ctx);
                }
            }
            if (result.consumesAction() && player instanceof ServerPlayerEntity) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, heldStack);
            }
            if (result == ActionResultType.FAIL) return ActionResultType.PASS;
            return result;
        }
        return ActionResultType.PASS;
    }


}
