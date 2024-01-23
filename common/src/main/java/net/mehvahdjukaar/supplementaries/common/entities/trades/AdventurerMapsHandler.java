package net.mehvahdjukaar.supplementaries.common.entities.trades;

import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class AdventurerMapsHandler {

    public static final int SEARCH_RADIUS = 150;


    public static ItemStack createMapOrQuill(ServerLevel serverLevel, BlockPos pos, @Nullable HolderSet<Structure> targets,
                                             int radius, boolean skipKnown,
                                             int zoom, @Nullable ResourceLocation mapMarker,
                                             @Nullable String name, int color) {
        if (CompatHandler.QUARK && CommonConfigs.Tweaks.QUARK_QUILL.get()) {
            var item = QuarkCompat.makeAdventurerQuill(serverLevel, targets,
                    radius, skipKnown, zoom, null, name, color);
            item.setHoverName(Component.translatable(name));
            return item;
        }

        if (!serverLevel.getServer().getWorldData().worldGenOptions().generateStructures())
            return ItemStack.EMPTY;

        if (targets == null) {
            targets = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(ModTags.ADVENTURE_MAP_DESTINATIONS)
                    .orElse(null);
            if (targets == null) targets = HolderSet.direct();
        }

        var found = StructureLocator.findNearestRandomMapFeature(
                serverLevel, targets, pos, radius, skipKnown);

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
            var type = MapDataRegistry.getAssociatedType(structure);
            decoration = Utils.getID(type);
            if (color == 0) {
                color = type.getDefaultMapColor();
            }
        }
        MapHelper.addDecorationToMap(stack, pos, decoration, color);

        if (name != null) {
            stack.setHoverName(Component.translatable(name));
        }
        return stack;
    }


    public static ItemStack createCustomMapForTrade(Level level, BlockPos pos, ResourceLocation structureName,
                                                    @Nullable String mapName, int mapColor, @Nullable ResourceLocation mapMarker) {

        if (level instanceof ServerLevel serverLevel) {
            var destination = TagKey.create(Registries.STRUCTURE, structureName);
            String name = mapName == null ?
                    "filled_map." + structureName.getPath().toLowerCase(Locale.ROOT) : mapName;
            var targets = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .getTag(destination).orElse(null);

            if (targets != null) {
                return createMapOrQuill(serverLevel, pos, targets, SEARCH_RADIUS, true,2, mapMarker, name, mapColor);
            }
        }
        return ItemStack.EMPTY;
    }


}