package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.integration.create.ContraptionReferenceFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/**
 * Client-only entry point for the Create integration. Kept separate so referencing client classes
 * ({@link CannonController}) never loads them on a dedicated server.
 */
public class CreateClientCompat {

    public static void startControlling(CannonBlockTile cannon, Entity contraption, BlockPos localPos) {
        cannon.setReferenceFrame(new ContraptionReferenceFrame(contraption, localPos));
        CannonController.startControlling(cannon);
    }
}
