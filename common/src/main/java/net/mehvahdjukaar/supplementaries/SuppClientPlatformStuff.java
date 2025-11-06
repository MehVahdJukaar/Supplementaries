package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.trades.ItemListingManager;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class SuppClientPlatformStuff {

    @ExpectPlatform
    public static ISlider createSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue,
                                       double currentValue, double stepSize, int precision, boolean drawString) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean hasFixedAO() {
        throw new AssertionError();
    }

}
