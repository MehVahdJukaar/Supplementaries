package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.quark.CartographersQuillItem;
import net.mehvahdjukaar.supplementaries.integration.quark.TaterInAJarBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.RegUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.block.be.MagnetizedBlockBlockEntity;
import org.violetmoon.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.module.PistonsMoveTileEntitiesModule;
import org.violetmoon.quark.content.building.block.StoolBlock;
import org.violetmoon.quark.content.client.module.UsesForCursesModule;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;
import org.violetmoon.quark.content.tools.item.SlimeInABucketItem;
import org.violetmoon.quark.content.tools.module.SlimeInABucketModule;
import org.violetmoon.quark.content.tweaks.module.DoubleDoorOpeningModule;
import org.violetmoon.quark.content.tweaks.module.EnhancedLaddersModule;
import org.violetmoon.quark.content.tweaks.module.MoreBannerLayersModule;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZGatherAdvancementModifiers;
import org.violetmoon.zeta.util.ItemNBTHelper;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public class QuarkCompat {

    public static final String TATER_IN_A_JAR_NAME = "tater_in_a_jar";

    public static final Supplier<Block> TATER_IN_A_JAR;
    public static final Supplier<BlockEntityType<TaterInAJarBlock.Tile>> TATER_IN_A_JAR_TILE;
    public static final Supplier<Item> CARTOGRAPHERS_QUILL;

    static {
        TATER_IN_A_JAR = RegUtils.regWithItem(TATER_IN_A_JAR_NAME, TaterInAJarBlock::new,
                new Item.Properties().rarity(Rarity.UNCOMMON), 0);

        TATER_IN_A_JAR_TILE = RegUtils.regTile(TATER_IN_A_JAR_NAME, () -> BlockEntityType.Builder.of(
                TaterInAJarBlock.Tile::new, TATER_IN_A_JAR.get()).build(null));

        CARTOGRAPHERS_QUILL = RegUtils.regItem("cartographers_quill", CartographersQuillItem::new);
    }

    public static void init() {
        //hackerinos
        Quark.ZETA.loadBus.subscribe(QuarkCompat.class);
    }

    @LoadEvent
    public static void gatherAdvModifiersEvent(ZGatherAdvancementModifiers event) {
        if (CommonConfigs.Tools.CANDY_ENABLED.get()) {
            event.register(event.createBalancedDietMod(Set.of(ModRegistry.CANDY_ITEM.get())));
        }

        if (CommonConfigs.Functional.SACK_PENALTY.get() && CommonConfigs.Functional.SACK_ENABLED.get()) {
            event.register(event.createFuriousCocktailMod(() -> false, Set.of(ModRegistry.OVERENCUMBERED.get())));
        }

        if (CommonConfigs.Functional.FLAX_ENABLED.get()) {
            event.register(event.createASeedyPlaceMod(Set.of(ModRegistry.FLAX.get())));
        }
    }

    public static boolean isFastSlideModuleEnabled() {
        return Quark.ZETA.modules.isEnabled(EnhancedLaddersModule.class) && EnhancedLaddersModule.allowSliding;
    }

    public static boolean isDoubleDoorEnabled() {
        return Quark.ZETA.modules.isEnabled(DoubleDoorOpeningModule.class);
    }

    public static boolean canMoveBlockEntity(BlockState state) {
        return !PistonsMoveTileEntitiesModule.shouldMoveTE(true, state);
    }

    @ExpectPlatform
    public static float getEncumbermentFromBackpack(ItemStack stack) {
        throw new AssertionError();
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
        if (tileTag != null && tile.getType() == BuiltInRegistries.BLOCK_ENTITY_TYPE.get(new ResourceLocation(tileTag.getString("id"))))
            tile.load(tileTag);
        return tile;
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

    public static BlockState getMagnetStateForFlintBlock(BlockEntity be, Direction dir) {
        if (be instanceof MagnetizedBlockBlockEntity magnet && dir == magnet.getFacing()) {
            return magnet.getMagnetState();
        }
        return null;
    }

    public static ItemStack getSlimeBucket(Entity entity) {
        if (Quark.ZETA.modules.isEnabled(SlimeInABucketModule.class)) {
            if (entity.getType() == EntityType.SLIME && ((Slime) entity).getSize() == 1 && entity.isAlive()) {
                ItemStack outStack = new ItemStack(SlimeInABucketModule.slime_in_a_bucket);
                CompoundTag cmp = new CompoundTag();
                entity.save(cmp);
                ItemNBTHelper.setCompound(outStack, SlimeInABucketItem.TAG_ENTITY_DATA, cmp);
                return outStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isShulkerDropInOn() {
        return Quark.ZETA.modules.isEnabled(ExpandedItemInteractionsModule.class)
                && ExpandedItemInteractionsModule.enableShulkerBoxInteraction;
    }

    public static boolean tryRotateStool(Level level, BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof StoolBlock) {
            level.setBlockAndUpdate(pos, state.cycle(StoolBlock.BIG));
            return true;
        }
        return false;
    }

    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, @Nullable TagKey<Structure> destination,
                                                int radius, boolean skipKnown, int zoom,
                                                ResourceLocation destinationType, @Nullable String name, int color) {
        HolderSet<Structure> targets = null;
        if (destination != null) {
            var v = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(destination);
            if (v.isEmpty()) {
                return ItemStack.EMPTY;
            } else targets = v.get();
        }
        return makeAdventurerQuill(serverLevel, targets, radius, skipKnown, zoom, destinationType, name, color);
    }

    public static ItemStack makeAdventurerQuill(ServerLevel serverLevel, HolderSet<Structure> targets,
                                                int radius, boolean skipKnown, int zoom,
                                                ResourceLocation destinationType, @Nullable String name, int color) {
        var item = CartographersQuillItem.forStructure(serverLevel, targets, radius, skipKnown, zoom, null, name, color);
        if (destinationType != null) {
            item.getOrCreateTag().putString(CartographersQuillItem.TAG_DECORATION, destinationType.toString().toLowerCase(Locale.ROOT));
        }
        return item;
    }

}