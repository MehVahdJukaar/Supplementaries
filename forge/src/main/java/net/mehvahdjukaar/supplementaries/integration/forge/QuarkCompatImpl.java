package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.forge.quark.CartographersQuillItem;
import net.mehvahdjukaar.supplementaries.integration.forge.quark.TaterInAJarBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.RegUtils;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.addons.oddities.block.be.MagnetizedBlockBlockEntity;
import vazkii.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import vazkii.quark.addons.oddities.item.BackpackItem;
import vazkii.quark.api.event.GatherAdvancementModifiersEvent;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.automation.module.JukeboxAutomationModule;
import vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule;
import vazkii.quark.content.building.block.StoolBlock;
import vazkii.quark.content.building.block.WoodPostBlock;
import vazkii.quark.content.building.module.VerticalSlabsModule;
import vazkii.quark.content.client.module.UsesForCursesModule;
import vazkii.quark.content.management.module.ExpandedItemInteractionsModule;
import vazkii.quark.content.tools.item.SlimeInABucketItem;
import vazkii.quark.content.tools.module.SlimeInABucketModule;
import vazkii.quark.content.tweaks.module.DoubleDoorOpeningModule;
import vazkii.quark.content.tweaks.module.EnhancedLaddersModule;
import vazkii.quark.content.tweaks.module.MoreBannerLayersModule;
import vazkii.quark.content.tweaks.module.MoreNoteBlockSoundsModule;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class QuarkCompatImpl {

    public static final String TATER_IN_A_JAR_NAME = "tater_in_a_jar";

    public static final Supplier<Block> TATER_IN_A_JAR;
    public static final Supplier<BlockEntityType<TaterInAJarBlock.Tile>> TATER_IN_A_JAR_TILE;
    public static final Supplier<Item> CARTOGRAPHERS_QUILL;

    static {
        TATER_IN_A_JAR = RegUtils.regWithItem(TATER_IN_A_JAR_NAME, TaterInAJarBlock::new,
                new Item.Properties().tab(null).rarity(Rarity.UNCOMMON), 0);

        TATER_IN_A_JAR_TILE = RegUtils.regTile(TATER_IN_A_JAR_NAME, () -> BlockEntityType.Builder.of(
                TaterInAJarBlock.Tile::new, TATER_IN_A_JAR.get()).build(null));

        CARTOGRAPHERS_QUILL = RegUtils.regItem("cartographers_quill", CartographersQuillItem::new);
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(QuarkCompatImpl.class);
    }

    @SubscribeEvent
    public static void gatherAdvModifiersEvent(GatherAdvancementModifiersEvent event) {
        if (CommonConfigs.Tools.CANDY_ENABLED.get()) {
            event.register(event.createBalancedDietMod(Set.of(ModRegistry.CANDY_ITEM.get())));
        }

        if (CommonConfigs.Functional.SACK_PENALTY.get() && CommonConfigs.Functional.SACK_ENABLED.get()) {
            event.register(event.createFuriousCocktailMod(() -> false, Set.of(ModRegistry.OVERENCUMBERED.get())));
        }

        if (CommonConfigs.Functional.FLAX_ENABLED.get()) {
            event.register(event.createASeedyPlaceMod(Set.of(ModRegistry.FLAX.get())));
        }
        Set<Block> signs = new HashSet<>();
        if (CommonConfigs.Building.SIGN_POST_ENABLED.get()) {
            signs.add(ModRegistry.SIGN_POST.get());
        }
        if (CommonConfigs.Building.HANGING_SIGN_ENABLED.get()) {
            signs.addAll(ModRegistry.HANGING_SIGNS.values());
        }
        if (!signs.isEmpty()) {
            event.register(event.createGlowAndBeholdMod(signs));
        }
    }

    @SubscribeEvent
    public static void noteBlockEvent(final NoteBlockEvent.Play event) {
        if (isMoreNoteBlockSoundsOn()) {
            LevelAccessor world = event.getLevel();
            BlockPos pos = event.getPos();
            if (world.getBlockState(pos).getBlock() == Blocks.NOTE_BLOCK) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockState state = world.getBlockState(pos.relative(dir));
                    Block block = state.getBlock();
                    if (block instanceof WallSkullBlock && state.getValue(WallSkullBlock.FACING) == dir) {
                        if (block == ModRegistry.ENDERMAN_SKULL_BLOCK_WALL.get()) {
                            SoundEvent sound = SoundEvents.ENDERMAN_TELEPORT;
                            event.setCanceled(true);
                            float pitch = (float) Math.pow(2.0, (event.getVanillaNoteId() - 12) / 12.0);
                            world.playSound(null, pos.above(), sound, SoundSource.BLOCKS, 1.0F, pitch);
                            return;
                        }
                    }
                }
            }
        }
    }

    //this should have been implemented in the post block updateShape method
    public static @Nullable BlockState updateWoodPostShape(BlockState post, Direction facing, BlockState facingState) {
        if (post.getBlock() instanceof WoodPostBlock) {
            Direction.Axis axis = post.getValue(WoodPostBlock.AXIS);
            if (facing.getAxis() != axis) {
                boolean chain = (facingState.getBlock() instanceof ChainBlock &&
                        facingState.getValue(BlockStateProperties.AXIS) == facing.getAxis());
                return post.setValue(WoodPostBlock.CHAINED[facing.ordinal()], chain);
            }
        }
        return null;
    }

    public static boolean isFastSlideModuleEnabled() {
        return ModuleLoader.INSTANCE.isModuleEnabled(EnhancedLaddersModule.class) && EnhancedLaddersModule.allowSliding;
    }

    public static boolean isDoubleDoorEnabled() {
        return ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class);
    }

    public static boolean canMoveBlockEntity(BlockState state) {
        return !PistonsMoveTileEntitiesModule.shouldMoveTE(true, state);
    }

    public static float getEncumbermentFromBackpack(ItemStack stack) {
        float j = 0;
        if (stack.getItem() instanceof BackpackItem) {
            LazyOptional<IItemHandler> handlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            if (handlerOpt.isPresent()) {
                IItemHandler handler = handlerOpt.resolve().get();
                for (int i = 0; i < handler.getSlots(); ++i) {
                    ItemStack slotItem = handler.getStackInSlot(i);
                    j += SackItem.getEncumber(slotItem);
                }
            }
        }
        return j;
    }

    public static boolean isVerticalSlabEnabled() {
        return ModuleLoader.INSTANCE.isModuleEnabled(VerticalSlabsModule.class);
    }

    public static boolean shouldHideOverlay(ItemStack stack) {
        return UsesForCursesModule.staticEnabled && EnchantmentHelper.hasVanishingCurse(stack);
    }

    public static int getBannerPatternLimit(int current) {
        return MoreBannerLayersModule.getLimit(current);
    }

    public static void tickPiston(Level level, BlockPos pos, BlockState spikes, AABB pistonBB, boolean sameDir, BlockEntity movingTile) {
        List<Entity> list = level.getEntities(null, pistonBB);
        for (Entity entity : list) {
            if (entity instanceof Player player && player.isCreative()) return;
            if (entity instanceof LivingEntity livingEntity && entity.isAlive()) {
                AABB entityBB = entity.getBoundingBox();
                if (pistonBB.intersects(entityBB)) {
                    //apply potions using quark moving tiles
                    if (CompatHandler.QUARK) {
                        //get tile
                        if (getMovingBlockEntity(pos, spikes, level) instanceof BambooSpikesBlockTile tile) {
                            //apply effects
                            if (tile.interactWithEntity(livingEntity, level)) {
                                //change block state if empty
                                if (movingTile instanceof IBlockHolder te) {
                                    //remove last charge and set new blockState
                                    BlockState state = te.getHeldBlock();
                                    if (state.getBlock() instanceof BambooSpikesBlock) {
                                        te.setHeldBlock(state.setValue(BambooSpikesBlock.TIPPED, false));
                                    }
                                }
                            }
                            //update tile entity in its list
                            PistonsMoveTileEntitiesModule.setMovingBlockEntityData(level, pos, tile.saveWithFullMetadata());
                        }
                    }
                    entity.hurt(BambooSpikesBlock.getDamageSource(level), sameDir ? 3 : 1);
                }
            }
        }
    }

    public static BlockEntity getMovingBlockEntity(BlockPos pos, BlockState state, Level level) {
        if (!(state.getBlock() instanceof EntityBlock eb)) return null;
        BlockEntity tile = eb.newBlockEntity(pos, state);
        if (tile == null) return null;
        CompoundTag tileTag = PistonsMoveTileEntitiesModule.getMovingBlockEntityData(level, pos);
        if (tileTag != null && tile.getType() == ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(new ResourceLocation(tileTag.getString("id"))))
            tile.load(tileTag);
        return tile;
    }

    public static boolean isJukeboxModuleOn() {
        return ModuleLoader.INSTANCE.isModuleEnabled(JukeboxAutomationModule.class);
    }

    public static InteractionResult tryCaptureTater(JarItem item, UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        if (world.getBlockEntity(pos) instanceof TinyPotatoBlockEntity te && te.getType() != TATER_IN_A_JAR_TILE.get()) {
            ItemStack stack = context.getItemInHand();
            CompoundTag com = stack.getTagElement("BlockEntityTag");
            if (com == null || com.isEmpty()) {
                if (!world.isClientSide) {
                    Player player = context.getPlayer();
                    item.playCatchSound(player);

                    ItemStack returnItem = new ItemStack(TATER_IN_A_JAR.get());
                    if (te.hasCustomName()) returnItem.setHoverName(te.getCustomName());
                    Utils.swapItemNBT(player, context.getHand(), stack, returnItem);

                    world.removeBlock(pos, false);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public static boolean isMoreNoteBlockSoundsOn() {
        return ModuleLoader.INSTANCE.isModuleEnabled(MoreNoteBlockSoundsModule.class) && MoreNoteBlockSoundsModule.enableSkullSounds;
    }

    public static BlockState getMagnetStateForFlintBlock(BlockEntity be, Direction dir) {
        if (be instanceof MagnetizedBlockBlockEntity magnet && dir == magnet.getFacing()) {
            return magnet.getMagnetState();
        }
        return null;
    }

    public static ItemStack getSlimeBucket(Entity entity) {
        if (ModuleLoader.INSTANCE.isModuleEnabled(SlimeInABucketModule.class)) {
            if (entity.getType() == EntityType.SLIME && ((Slime) entity).getSize() == 1 && entity.isAlive()) {
                ItemStack outStack = new ItemStack(SlimeInABucketModule.slime_in_a_bucket);
                CompoundTag cmp = entity.serializeNBT();
                ItemNBTHelper.setCompound(outStack, SlimeInABucketItem.TAG_ENTITY_DATA, cmp);
                return outStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isShulkerDropInOn() {
        return ModuleLoader.INSTANCE.isModuleEnabled(ExpandedItemInteractionsModule.class)
                && ExpandedItemInteractionsModule.enableShulkerBoxInteraction;
    }

    public static boolean tryRotateStool(Level level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof StoolBlock) {
            level.setBlockAndUpdate(pos, state.cycle(StoolBlock.BIG));
            return true;
        }
        return false;
    }


    private static Field f2 = null;

    public static void removeStuffFromARLHack() {
        if (f2 == null) {
            f2 = ObfuscationReflectionHelper.findField(RegistryHelper.class, "modData");
            f2.setAccessible(true);
        }
        try {
            var data = (Map<String, ?>) f2.get(null);
            data.remove(Supplementaries.MOD_ID);
            data.remove("suppsquared");
        } catch (Exception ignored) {
        }
    }

    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, TagKey<Structure> destination, int radius, boolean skipKnown, int zoom, MapDecoration.Type destinationType, @org.jetbrains.annotations.Nullable String name, int color) {
       return CartographersQuillItem.forStructure(serverLevel, destination, radius, skipKnown,zoom, destinationType, name, color);
    }
}
