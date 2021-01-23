package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        if(entityIn instanceof ServerPlayerEntity && !((ServerPlayerEntity) entityIn).isCreative() && !entityIn.isSpectator() && worldIn.getGameTime() % 20L == 0L){
            ServerPlayerEntity player = (ServerPlayerEntity) entityIn;
            Collection<EffectInstance> effects = player.getActivePotionEffects();
            for (EffectInstance effect : effects) {
                if (effect.getPotion() == Effects.SLOWNESS)
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
                player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 80,  i/(inc+1)));
            }
        }
    }


    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("LootTable", 8)) {
                tooltip.add(new StringTextComponent("???????").mergeStyle(TextFormatting.GRAY));
            }

            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(9, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                int i = 0;
                int j = 0;

                for(ItemStack itemstack : nonnulllist) {
                    if (!itemstack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            IFormattableTextComponent iformattabletextcomponent = itemstack.getDisplayName().deepCopy();
                            iformattabletextcomponent.appendString(" x").appendString(String.valueOf(itemstack.getCount()));
                            tooltip.add(iformattabletextcomponent.mergeStyle(TextFormatting.GRAY));
                        }
                    }
                }
                if (j - i > 0) {
                    tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).mergeStyle(TextFormatting.ITALIC).mergeStyle(TextFormatting.GRAY));
                }
            }
        }
        tooltip.add(new TranslationTextComponent("message.supplementaries.sack").mergeStyle(TextFormatting.ITALIC).mergeStyle(TextFormatting.GRAY));
    }

}
