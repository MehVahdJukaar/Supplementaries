package net.mehvahdjukaar.supplementaries.common.entities.trades;

import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdventurerMapsHandler {

    public static ItemStack createMapOrQuill(ServerLevel serverLevel, BlockPos pos, @Nullable HolderSet<Structure> targets,
                                             int radius, boolean skipKnown,
                                             int zoom, @Nullable ResourceLocation mapMarker,
                                             @Nullable String name, int color) {

        if (!serverLevel.getServer().getWorldData().worldGenOptions().generateStructures())
            return ItemStack.EMPTY;

        if (targets == null) {
            targets = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(ModTags.ADVENTURE_MAP_DESTINATIONS)
                    .orElse(null);
        }

        if (targets == null || targets.size() < 1) {
            return ItemStack.EMPTY;
        }


        if (CompatHandler.QUARK && CommonConfigs.Tweaks.QUARK_QUILL.get()) {
            var item = QuarkCompat.makeAdventurerQuill(serverLevel, targets,
                    radius, skipKnown, zoom, mapMarker, name, color);
            if (name != null) {
                item.set(DataComponents.ITEM_NAME, Component.translatable(name));
            }
            return item;
        }

        int maxSearches = CommonConfigs.Tweaks.RANDOM_ADVENTURER_MAPS_MAX_SEARCHES.get();
        var found = StructureLocator.findNearestMapFeature(
                serverLevel, targets, pos, radius, skipKnown, maxSearches, false);

        if (found != null) {
            BlockPos toPos = found.pos();
            return createStructureMap(serverLevel, toPos, found.structure(), zoom, mapMarker, name, color);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    public static ItemStack createStructureMap(ServerLevel level, BlockPos pos, Holder<Structure> structure, int zoom,
                                               @Nullable ResourceLocation decoration, @Nullable String name,
                                               int color) {
        ItemStack stack = MapItem.create(level, pos.getX(), pos.getZ(), (byte) zoom, true, true);
        MapItem.renderBiomePreviewMap(level, stack);

        //adds custom decoration
        if (decoration == null) {
            var type =
                    MapDataRegistry.getDecorationFoStructure(level, structure);
            decoration = type.unwrapKey().get().location();
            if (color == 0) {
                color = type.value().getDefaultMapColor();
            }
        }
        MapHelper.addTargetDecorationToItem(level, stack, pos, decoration, color);

        if (name != null) {
            stack.set(DataComponents.ITEM_NAME, Component.translatable(name));
        }
        return stack;
    }


    public static ItemStack createCustomMapForTrade(Level level, BlockPos pos, HolderSet<Structure> destinations,
                                                    @Nullable String mapName, int mapColor, @Nullable ResourceLocation mapMarker) {
        if (level instanceof ServerLevel serverLevel) {
            return createMapOrQuill(serverLevel, pos, destinations,
                    CommonConfigs.Tweaks.RANDOM_ADVENTURER_MAX_SEARCH_RADIUS.get(),
                    true, 2, mapMarker, mapName, mapColor);
        }
        return ItemStack.EMPTY;
    }


}