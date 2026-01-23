package net.mehvahdjukaar.supplementaries.common.items.tooltip_components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.CustomData;

public record PaintingTooltip(PaintingVariant data) implements TooltipComponent {

    public static PaintingTooltip create(CustomData customData) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        RegistryAccess ra = level.registryAccess();
        var painting = customData.read(ra.createSerializationContext(NbtOps.INSTANCE), Painting.VARIANT_MAP_CODEC);
        if (painting.isError()) return null;
        return new PaintingTooltip(painting.getOrThrow().value());
    }
}
