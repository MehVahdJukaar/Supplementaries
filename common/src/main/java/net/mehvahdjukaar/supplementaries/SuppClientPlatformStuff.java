package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.ISlider;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.worldgen.GalleonStructure;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

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
