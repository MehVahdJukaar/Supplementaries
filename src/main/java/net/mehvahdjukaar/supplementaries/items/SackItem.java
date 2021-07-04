package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkTooltipPlugin;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SackItem extends BlockItem {
    public SackItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if(!ServerConfigs.cached.SACK_PENALTY)return;
        if(entityIn instanceof ServerPlayerEntity && !((PlayerEntity) entityIn).isCreative() && !entityIn.isSpectator() && worldIn.getGameTime() % 20L == 0L){
            ServerPlayerEntity player = (ServerPlayerEntity) entityIn;
            Collection<EffectInstance> effects = player.getActiveEffects();
            for (EffectInstance effect : effects) {
                if (effect.getEffect() == Effects.MOVEMENT_SLOWDOWN)
                    return;
            }

            int i = 0;
            AtomicReference<IItemHandler> _iitemhandlerref = new AtomicReference<>();
            entityIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(_iitemhandlerref::set);
            if (_iitemhandlerref.get() != null) {
                for (int _idx = 0; _idx < _iitemhandlerref.get().getSlots(); _idx++) {
                    if(_iitemhandlerref.get().getStackInSlot(_idx).getItem() instanceof SackItem){
                        i++;
                    }
                }
            }
            int inc = ServerConfigs.cached.SACK_INCREMENT;
            if(i>inc){
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 80,  i/(inc+1)));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if(!CompatHandler.quark || !QuarkTooltipPlugin.canRenderTooltip()) {
            CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
            if (compoundnbt != null) {
                if (compoundnbt.contains("LootTable", 8)) {
                    tooltip.add(new StringTextComponent("???????").withStyle(TextFormatting.GRAY));
                }

                if (compoundnbt.contains("Items", 9)) {
                    NonNullList<ItemStack> nonnulllist = NonNullList.withSize(9, ItemStack.EMPTY);
                    ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                    int i = 0;
                    int j = 0;

                    for (ItemStack itemstack : nonnulllist) {
                        if (!itemstack.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                IFormattableTextComponent iformattabletextcomponent = itemstack.getHoverName().copy();
                                iformattabletextcomponent.append(" x").append(String.valueOf(itemstack.getCount()));
                                tooltip.add(iformattabletextcomponent.withStyle(TextFormatting.GRAY));
                            }
                        }
                    }
                    if (j - i > 0) {
                        tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                    }
                }
            }
        }
        if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)return;
        tooltip.add(new TranslationTextComponent("message.supplementaries.sack").withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
    }
}
