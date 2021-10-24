package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientRegistry {

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(Supplementaries.res(name), name);
    }

    public static ModelLayerLocation BELLOWS_MODEL = loc("bellows");
    public static ModelLayerLocation BELL_EXTENSION = loc("bell_extension");
    public static ModelLayerLocation BOOK_MODEL = loc("book");
    public static ModelLayerLocation CLOCK_HANDS_MODEL = loc("clock_hands");
    public static ModelLayerLocation GLOBE_BASE_MODEL = loc("globe");
    public static ModelLayerLocation GLOBE_SPECIAL_MODEL = loc("globe_special");
    public static ModelLayerLocation SIGN_POST_MODEL = loc("bellows");

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BELLOWS_MODEL, BellowsBlockTileRenderer::createMesh);
        event.registerLayerDefinition(BELL_EXTENSION, BellTileMixinRenderer::createMesh);
        event.registerLayerDefinition(BOOK_MODEL, BookPileBlockTileRenderer::createMesh);
        event.registerLayerDefinition(CLOCK_HANDS_MODEL, ClockBlockTileRenderer::createMesh);
        event.registerLayerDefinition(GLOBE_BASE_MODEL, GlobeBlockTileRenderer::createBaseMesh);
        event.registerLayerDefinition(GLOBE_SPECIAL_MODEL, GlobeBlockTileRenderer::createSpecialMesh);
        event.registerLayerDefinition(SIGN_POST_MODEL, SignPostBlockTileRenderer::createMesh);
    }
}
