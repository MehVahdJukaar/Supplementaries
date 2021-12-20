package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RedMerchantRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.SkullCandleOverlay;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.pickle.JarredModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.pickle.PickleModel;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.util.NonNullLazy;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ClientRegistry {

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(Supplementaries.res(name), name);
    }

    public static ModelLayerLocation BELLOWS_MODEL = loc("bellows");
    //public static ModelLayerLocation BELL_EXTENSION = loc("bell_extension");
    public static ModelLayerLocation BOOK_MODEL = loc("book");
    public static ModelLayerLocation CLOCK_HANDS_MODEL = loc("clock_hands");
    public static ModelLayerLocation GLOBE_BASE_MODEL = loc("globe");
    public static ModelLayerLocation GLOBE_SPECIAL_MODEL = loc("globe_special");
    public static ModelLayerLocation SIGN_POST_MODEL = loc("sign_post");
    public static ModelLayerLocation RED_MERCHANT_MODEL = loc("red_merchant");
    public static ModelLayerLocation SKULL_CANDLE_OVERLAY = loc("skull_candle");
    public static ModelLayerLocation JARVIS_MODEL = loc("jarvis");
    public static ModelLayerLocation PICKLE_MODEL = loc("pickle");

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BELLOWS_MODEL, BellowsBlockTileRenderer::createMesh);
        //event.registerLayerDefinition(BELL_EXTENSION, BellTileMixinRenderer::createMesh);
        event.registerLayerDefinition(BOOK_MODEL, BookPileBlockTileRenderer::createMesh);
        event.registerLayerDefinition(CLOCK_HANDS_MODEL, ClockBlockTileRenderer::createMesh);
        event.registerLayerDefinition(GLOBE_BASE_MODEL, GlobeBlockTileRenderer::createBaseMesh);
        event.registerLayerDefinition(GLOBE_SPECIAL_MODEL, GlobeBlockTileRenderer::createSpecialMesh);
        event.registerLayerDefinition(SIGN_POST_MODEL, SignPostBlockTileRenderer::createMesh);
        event.registerLayerDefinition(RED_MERCHANT_MODEL, RedMerchantRenderer::createMesh);
        event.registerLayerDefinition(SKULL_CANDLE_OVERLAY, SkullCandleOverlay::createMesh);
        event.registerLayerDefinition(JARVIS_MODEL, JarredModel::createMesh);
        event.registerLayerDefinition(PICKLE_MODEL, PickleModel::createMesh);
    }

    public static void registerISTER(Consumer<IItemRenderProperties> consumer, BiFunction<BlockEntityRenderDispatcher, EntityModelSet, BlockEntityWithoutLevelRenderer> factory) {
        consumer.accept(new IItemRenderProperties() {
            final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(
                    () -> factory.apply(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return renderer.get();
            }
        });
    }
}
