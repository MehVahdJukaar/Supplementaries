package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.components.QuiverContent;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class QuiverItem extends SelectableContainerItem<QuiverContent, QuiverContent.Mutable> {

    public QuiverItem(Properties properties) {
        super(properties);
    }

    @Override
    public DataComponentType<QuiverContent> getComponentType() {
        return ModComponents.QUIVER_CONTENT.get();
    }

    @NotNull
    @Override
    public ItemStack getFirstInInventory(Player player) {
        return getQuiver(player);
    }

    @Override
    public int getMaxSlots() {
        return CommonConfigs.Tools.QUIVER_SLOTS.get();
    }

    @NotNull
    public static SlotReference getQuiverSlot(LivingEntity entity) {
        if (entity instanceof Player player) {
            var curioQuiver = CompatHandler.getQuiverFromModsSlots(player);
            if (!curioQuiver.isEmpty()) return curioQuiver;
            if (CommonConfigs.Tools.QUIVER_CURIO_ONLY.get()) return SlotReference.EMPTY;
        } else if (entity instanceof IQuiverEntity e) {
            return e::supplementaries$getQuiver;
        }

        return SuppPlatformStuff.getFirstInInventory(entity, i -> i.getItem() instanceof QuiverItem);
    }

    public static ItemStack getQuiver(LivingEntity entity) {
        return getQuiverSlot(entity).get();
    }

    public static boolean canAcceptItem(ItemStack toInsert) {
        return toInsert.getItem() instanceof ArrowItem && !toInsert.is(ModTags.QUIVER_BLACKLIST);
    }
}

