package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.RegUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.addons.oddities.block.be.MagnetizedBlockBlockEntity;
import vazkii.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import vazkii.quark.addons.oddities.item.BackpackItem;
import vazkii.quark.api.event.GatherAdvancementModifiersEvent;
import vazkii.quark.base.handler.advancement.AdvancementModifier;
import vazkii.quark.base.handler.advancement.QuarkAdvancementHandler;
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

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class QuarkCompatImpl {

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


    //--------bamboo spikes-------

    //called by mixin code

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

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(QuarkCompatImpl::gatherAdvModifiersEvent);
    }

    public static void gatherAdvModifiersEvent(GatherAdvancementModifiersEvent event) {
        if (CommonConfigs.Tools.CANDY_ENABLED.get()) {
            QuarkAdvancementHandler.addModifier((AdvancementModifier)
                    event.delegate.modifyBalancedDiet(Set.of(ModRegistry.CANDY_ITEM.get())));
        }

        if (CommonConfigs.Functional.SACK_PENALTY.get() && CommonConfigs.Functional.SACK_ENABLED.get()) {
            QuarkAdvancementHandler.addModifier((AdvancementModifier)
                    event.delegate.modifyFuriousCocktail(() -> false, Set.of(ModRegistry.OVERENCUMBERED.get())));
        }
    }

    public static final String TATER_IN_A_JAR_NAME = "tater_in_a_jar";

    public static final Supplier<Block> TATER_IN_A_JAR;
    public static final Supplier<BlockEntityType<TaterInAJarBlock.Tile>> TATER_IN_A_JAR_TILE;

    static {
        TATER_IN_A_JAR = RegUtils.regWithItem(TATER_IN_A_JAR_NAME, TaterInAJarBlock::new,
                new Item.Properties().tab(null).rarity(Rarity.UNCOMMON), 0);

        TATER_IN_A_JAR_TILE = RegUtils.regTile(TATER_IN_A_JAR_NAME, () -> BlockEntityType.Builder.of(
                TaterInAJarBlock.Tile::new, TATER_IN_A_JAR.get()).build(null));
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


}
