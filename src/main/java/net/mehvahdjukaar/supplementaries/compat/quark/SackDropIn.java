package net.mehvahdjukaar.supplementaries.compat.quark;

import net.mehvahdjukaar.supplementaries.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;
import vazkii.arl.util.AbstractDropIn;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.management.module.ShulkerBoxRightClickModule;

public class SackDropIn extends AbstractDropIn {

    private static final SackBlockTile DUMMY_SACK_TILE = new SackBlockTile();
    private static final BlockState DEFAULT_SACK = ModRegistry.SACK.get().defaultBlockState();

    public SackDropIn() {
    }

    public boolean canDropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
        return ModuleLoader.INSTANCE.isModuleEnabled(ShulkerBoxRightClickModule.class) && this.tryAddToShulkerBox(stack, incoming, true);
    }

    public ItemStack dropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming) {
        this.tryAddToShulkerBox(stack, incoming, false);
        return stack;
    }
    public boolean canDropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming, Slot slot) {
        return canDropItemIn(player,stack,incoming);
    }

    public ItemStack dropItemIn(PlayerEntity player, ItemStack stack, ItemStack incoming, Slot slot) {
        return dropItemIn(player,stack,incoming);
    }

    private boolean tryAddToShulkerBox(ItemStack sack, ItemStack stack, boolean simulate) {
        if (CommonUtil.isAllowedInShulker(stack)) {
            CompoundNBT cmp = ItemNBTHelper.getCompound(sack, "BlockEntityTag", false);
            if (!cmp.contains("LootTable")) {
                if (cmp != null) {
                    cmp = cmp.copy();
                    cmp.putString("id", "supplementaries:sack");
                    Item i = sack.getItem();
                    if (i instanceof SackItem) {

                        DUMMY_SACK_TILE.load(DEFAULT_SACK, cmp);

                        LazyOptional<IItemHandler> handlerHolder = DUMMY_SACK_TILE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                        if (handlerHolder.isPresent()) {
                            IItemHandler handler = handlerHolder.orElseGet(EmptyHandler::new);
                            ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
                            boolean did = result.isEmpty() || result.getCount() != stack.getCount();
                            if (!simulate && did) {
                                stack.setCount(result.getCount());
                                CompoundNBT compoundnbt = DUMMY_SACK_TILE.save(cmp);
                                if (!compoundnbt.isEmpty()) {
                                    sack.addTagElement("BlockEntityTag", compoundnbt);
                                }
                            }

                            return did;
                        }
                    }
                }

            }
        }
        return false;
    }
}