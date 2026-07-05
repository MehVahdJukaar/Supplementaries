package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.UUID;

/**
 * Create integration: lets cannons be aimed and fired while mounted on trains and contraptions.
 *
 * <p>Create 6 is multiloader, so the common module never references Create directly. Platform impls
 * register movement/interaction behaviours and implement the {@link PlatformImpl} bridge methods below.
 */
public class CreateCompat {

    public static void setup() {
        registerCannonBehaviours(ModRegistry.CANNON.get());
        registerExtraMovementBehaviours();
    }

    /**
     * Client-side callback from the contraption interaction behaviour: enter cannon maneuver mode when the
     * player sneak-uses a cannon block on a moving contraption.
     */
    public static boolean onContraptionInteractClient(@Nullable BlockEntity be, Entity contraption, BlockPos localPos,
                                                      boolean secondaryUse) {
        if (!secondaryUse || !(be instanceof CannonBlockTile cannon)) return false;
        CreateClientCompat.startControlling(cannon, contraption, localPos);
        return true;
    }

    // === platform bridge: everything below is hard-cast to Create's AbstractContraptionEntity in the impl ===

    @PlatformImpl
    public static void registerCannonBehaviours(Block cannon) {
        throw new AssertionError();
    }

    /** NeoForge-only extra behaviours (hourglass, bamboo spikes, etc.). No-op on Fabric. */
    @PlatformImpl
    public static void registerExtraMovementBehaviours() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Vec3 contraptionPosToGlobalPos(Entity contraption, Vec3 localVec, float partialTicks) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Quaternionf getContraptionRotation(Entity contraption, float partialTicks) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Vec3 getContactPointMotion(Entity contraption, Vec3 worldPoint) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    public static BlockEntity getClientBlockEntity(Entity contraption, BlockPos localPos) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    public static Entity findContraption(Level level, UUID contraptionId) {
        throw new AssertionError();
    }

    /**
     * Bake a cannon's aim into the contraption's stored block NBT so it persists past the live render.
     */
    @PlatformImpl
    public static void persistCannonAim(Entity contraption, BlockPos localPos, Quaternionf localRot, byte firePower) {
        throw new AssertionError();
    }
}
