package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PancakeItem extends RecordItem {

    public PancakeItem(int i, SoundEvent soundEvent, Properties properties, int seconds) {
        super(i, soundEvent, properties, seconds);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
    }

    @Override
    public String getDescriptionId() {
        return ModRegistry.PANCAKE.get().getDescriptionId();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        int oldAmount = itemStack.getCount();
        itemStack.setCount(1);
        var r = super.useOn(context);
        if(itemStack.isEmpty()){
            itemStack.setCount(oldAmount-1);
        }else itemStack.setCount(oldAmount);
        if (!r.consumesAction()) {
            return AdditionalItemPlacement.getBlockPlacer()
                    .mimicUseOn(context, ModRegistry.PANCAKE.get(), null);
        }
        return r;
    }
}
