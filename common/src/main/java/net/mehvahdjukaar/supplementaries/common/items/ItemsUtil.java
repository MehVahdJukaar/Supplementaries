package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.api.IExtendedItem;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.items.additional_behaviors.SimplePlacement;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class ItemsUtil {

    //placeable item stuff

    @Nullable
    public static BlockState getPlacementState(BlockPlaceContext context, Block block) {
        return ModRegistry.BLOCK_PLACER.get().mimicGetPlacementState(context, block);
    }

    public static InteractionResult place(BlockPlaceContext context, Block blockToPlace) {
        return place(context, blockToPlace, null);
    }

    public static InteractionResult place(BlockPlaceContext context, Block blockToPlace, @Nullable SoundType placeSound) {
        return ModRegistry.BLOCK_PLACER.get().mimicPlace(context, blockToPlace, placeSound);
    }

    //helper for slingshot. Calls both block item and this in case it as additional behavior
    public static InteractionResult place(Item item, BlockPlaceContext pContext) {
        //this also calls mixin
        if (item instanceof BlockItem bi) return bi.place(pContext);
        if (((IExtendedItem) item).getAdditionalBehavior() instanceof SimplePlacement si)
            return si.overridePlace(pContext);
        return InteractionResult.PASS;
    }

    public record InventoryTooltip(CompoundTag tag, Item item, int size) implements TooltipComponent {
    }

    public static boolean tryInteractingWithContainerItem(ItemStack containerStack, ItemStack incoming, Slot slot, ClickAction action, Player player, boolean inSlot) {
        if (action != ClickAction.PRIMARY) {
            //drop content in empty slot
            if (incoming.isEmpty()) {
                if (!inSlot) {
                    return ItemsUtil.extractFromContainerItemIntoSlot(player, containerStack, slot);
                }
            } else if (ItemsUtil.addToContainerItem(player, containerStack, incoming, slot, true, inSlot)) {
                return ItemsUtil.addToContainerItem(player, containerStack, incoming, slot, false, inSlot);
            }
        }
        return false;
    }


    @Nullable
    @ExpectPlatform
    public static boolean addToContainerItem(Player player, ItemStack containerStack, ItemStack stack, Slot slot, boolean simulate, boolean inSlot) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static boolean extractFromContainerItemIntoSlot(Player player, ItemStack containerStack, Slot slot) {
        throw new AssertionError();
    }


    @Nullable
    public static BlockEntity loadBlockEntityFromItem(CompoundTag tag, Item item) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof EntityBlock entityBlock) {
                BlockEntity te = entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
                if (te != null) te.load(tag);
                return te;
            }
        }
        return null;
    }

    @ExpectPlatform
    public static int getAllSacksInInventory(ItemStack stack, Entity entityIn, ServerPlayer player, int amount) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static KeyLockableTile.KeyStatus hasKeyInInventory(Player player, String key) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static boolean faucetSpillItems(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        throw new AssertionError();
    }

}
