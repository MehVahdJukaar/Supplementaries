package net.mehvahdjukaar.supplementaries.integration.forge;

import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModDamageSources;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.addons.oddities.item.BackpackItem;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule;
import vazkii.quark.content.building.block.WoodPostBlock;
import vazkii.quark.content.building.module.VerticalSlabsModule;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;
import vazkii.quark.content.management.module.ExpandedItemInteractionsModule;
import vazkii.quark.content.tweaks.module.DoubleDoorOpeningModule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

    public static InteractionResult tryCaptureTater(JarItem jarItem, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public static boolean isDoubleDoorEnabled() {
        return ModuleLoader.INSTANCE.isModuleEnabled(DoubleDoorOpeningModule.class);
    }

    public static boolean canMoveBlockEntity(BlockState state) {
        return !PistonsMoveTileEntitiesModule.shouldMoveTE(true, state);
    }

    public static int getSacksInBackpack(ItemStack stack) {
        int j = 0;
        if (stack.getItem() instanceof BackpackItem) {
            LazyOptional<IItemHandler> handlerOpt = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handlerOpt.isPresent()) {
                IItemHandler handler = handlerOpt.orElse(null);
                for (int i = 0; i < handler.getSlots(); ++i) {
                    ItemStack slotItem = handler.getStackInSlot(i);
                    if (slotItem.getItem() instanceof SackItem) {
                        CompoundTag tag = stack.getTag();
                        if (tag != null && tag.contains("BlockEntityTag")) {
                            j++;
                        }
                    }
                }
            }
        }
        return j;
    }

    public static boolean isVerticalSlabEnabled() {
        return ModuleLoader.INSTANCE.isModuleEnabled(VerticalSlabsModule.class);
    }

    public static boolean canRenderBlackboardTooltip() {
        return canRenderQuarkTooltip();
    }

    public static boolean canRenderQuarkTooltip() {
        return ModuleLoader.INSTANCE.isModuleEnabled(ExpandedItemInteractionsModule.class) &&
                (!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown());
    }

    public static boolean shouldHaveButtonOnRight() {
        return !(GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton);
    }


    //--------tooltips-------

    public static void registerTooltipComponent(ClientPlatformHelper.TooltipComponentEvent event) {
        event.register(ItemsUtil.InventoryTooltip.class, QuarkInventoryTooltipComponent::new);
    }


    private static final BlockState DEFAULT_SAFE = ModRegistry.SAFE.get().defaultBlockState();
    private static final SafeBlockTile DUMMY_SAFE_TILE = new SafeBlockTile(BlockPos.ZERO, DEFAULT_SAFE);


    public static void onItemTooltipEvent(ItemStack stack, TooltipFlag tooltipFlag, List<Component> components) {
        if (canRenderQuarkTooltip()) {
            CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", true);
            if (cmp != null && !cmp.contains("LootTable")) {
                Item i = stack.getItem();
                if (i == ModRegistry.SAFE_ITEM.get()) {
                    DUMMY_SAFE_TILE.load(cmp);
                    Player player = Minecraft.getInstance().player;
                    if (player == null || DUMMY_SAFE_TILE.canPlayerOpen(Minecraft.getInstance().player, false)) {
                        cleanupTooltip(components);
                    }
                } else if (i == ModRegistry.SACK_ITEM.get()) {
                    cleanupTooltip(components);
                }
            }
        }
    }

    private static void cleanupTooltip(List<Component> tooltip) {
        var tooltipCopy = new ArrayList<>(tooltip);

        for (int i = 1; i < tooltipCopy.size(); ++i) {
            Component component = tooltipCopy.get(i);
            String s = component.getString();
            if (!s.startsWith("§") || s.startsWith("§o")) {
                tooltip.remove(component);
            }
        }
        if (ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown()) {
            tooltip.add(1, Component.translatable("quark.misc.shulker_box_shift"));
        }
    }


    //--------bamboo spikes-------

    //called by mixin code

    public static void tickPiston(Level level, BlockPos pos, AABB pistonBB, boolean sameDir, BlockEntity movingTile) {
        List<Entity> list = level.getEntities(null, pistonBB);
        for (Entity entity : list) {
            if (entity instanceof Player player && player.isCreative()) return;
            if (entity instanceof LivingEntity livingEntity && entity.isAlive()) {
                AABB entityBB = entity.getBoundingBox();
                if (pistonBB.intersects(entityBB)) {
                    //apply potions using quark moving tiles
                    if (CompatHandler.quark) {
                        //get tile
                        if (getMovingBlockEntity(pos, level) instanceof BambooSpikesBlockTile tile) {
                            //apply effects
                            if (tile.interactWithEntity(livingEntity, level)) {
                                //change blockstate if empty
                                if (movingTile instanceof IBlockHolder te) {
                                    //remove last charge and set new blockState
                                    BlockState state = te.getHeldBlock();
                                    if (state.getBlock() instanceof BambooSpikesBlock) {
                                        te.setHeldBlock(state.setValue(BambooSpikesBlock.TIPPED, false));
                                    }
                                }
                            }
                            //update tile entity in its list
                            updateMovingTile(pos, level, tile);
                        }
                    }
                    entity.hurt(ModDamageSources.SPIKE_DAMAGE, sameDir ? 3 : 1);
                }
            }
        }
    }

    private static Field MOVEMENTS = null;

    private static void updateMovingTile(BlockPos pos, Level world, BlockEntity tile) {
        //not very nice of me to change its private fields :/
        try {
            //Class c = Class.forName("vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule");
            if (MOVEMENTS == null) {
                MOVEMENTS = ObfuscationReflectionHelper.findField(PistonsMoveTileEntitiesModule.class, "movements");
            }

            Object o = MOVEMENTS.get(null);
            if (o instanceof WeakHashMap) {
                WeakHashMap<Level, Map<BlockPos, CompoundTag>> movements = (WeakHashMap<Level, Map<BlockPos, CompoundTag>>) o;
                if (movements.containsKey(world)) {
                    Map<BlockPos, CompoundTag> worldMovements = movements.get(world);
                    if (worldMovements.containsKey(pos)) {
                        worldMovements.remove(pos);
                        worldMovements.put(pos, tile.saveWithFullMetadata());
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static BlockEntity getMovingBlockEntity(BlockPos pos, Level level) {
        return PistonsMoveTileEntitiesModule.getMovement(level, pos);
    }


}
