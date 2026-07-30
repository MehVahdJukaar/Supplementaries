package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.world.level.block.piston.PistonStructureResolver;

public class PistonMovementHelper {

    /**
     * Whether our own capture/restore runs for piston block entity moves.
     * <p>
     * Quark's <i>pistons move tile entities</i> module does the same job with its own pipeline, and
     * the two cannot share a move: both strip the source block entity and both rebuild one at the
     * destination, so whichever writes last wins and the loser's data is silently gone (only blocks
     * on Quark's delayed-update list survive, since those are written a tick later). Quark detaches
     * at the very top of {@code moveBlocks} and so always captures first, leaving ours empty.
     * Rather than race, we hand pistons over entirely whenever that module is on.
     * <p>
     * Pulleys keep our pipeline either way: Quark only hooks {@code PistonBaseBlock.moveBlocks} and
     * {@code PistonMovingBlockEntity.tick}, neither of which a pulley move goes through, so there
     * is nothing to defer to there.
     */
    public static boolean BEMovementHandledByUs() {
        return CommonConfigs.Tweaks.PUSH_BLOCK_ENTITIES.get() && !BEMovementHandledByQuark();
    }

    /**
     * Whether Quark's module is present and enabled, and therefore owns piston moves.
     */
    public static boolean BEMovementHandledByQuark() {
        return CompatHandler.QUARK && QuarkCompat.isMovingTileEntitiesEnabled();
    }


    /**
     * How many blocks a single piston may push, which is what each cooperator contributes to the
     * pooled budget. Vanilla's {@link PistonStructureResolver#MAX_PUSH_DEPTH} unless Zeta's piston
     * resolver is in use, in which case its configurable limit (Zeta general config,
     * {@code pistonPushLimit}) is authoritative: cooperation has to scale off the same number the
     * user configured, or raising it would silently stop cooperation from adding anything and
     * lowering it would let us push past what they asked for.
     */
    public static int perPistonPushLimit() {
        if (CompatHandler.QUARK) return QuarkCompat.getPistonPushLimit();
        return PistonStructureResolver.MAX_PUSH_DEPTH;
    }
}
