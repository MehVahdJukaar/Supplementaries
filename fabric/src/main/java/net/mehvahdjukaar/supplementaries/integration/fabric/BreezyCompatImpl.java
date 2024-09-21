package net.mehvahdjukaar.supplementaries.integration.fabric;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.mehvahdjukaar.moonlight.api.client.model.fabric.ModelWrapper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.SelfCustomBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;

public class BreezyCompatImpl {
    public static float getWindAngle(BlockPos pos, Level level) {
        return 90;
    }

}
