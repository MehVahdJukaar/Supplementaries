package net.mehvahdjukaar.supplementaries.integration.malum;

import com.sammy.malum.common.block.SapFilledLogBlock;
import com.sammy.malum.core.helper.BlockHelper;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.setup.ModSoftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MalumPlugin {
    public static boolean isSappyLog(Block backBlock) {
        return backBlock instanceof SapFilledLogBlock;
    }

    public static SoftFluid getSap(Block block) {

        return block.getRegistryName().getPath().contains("unholy") ? ModSoftFluids.UNHOLY_SAP : ModSoftFluids.HOLY_SAP;
    }

    public static void extractSap(Level level, BlockState backState, BlockPos pos) {
        SapFilledLogBlock block = ((SapFilledLogBlock) backState.getBlock());
        var stripped = block.stripped;

        if (level.random.nextBoolean()) {
            BlockHelper.setBlockStateWithExistingProperties(level, pos, stripped.get().defaultBlockState(), 3);
        }

        if (level.isClientSide) {
            //ParticleUtil.spawnParticlesOnBlockFaces(level, pos,ParticleRegistry.WISP_PARTICLE, UniformInt.of(3, 5), );
            // RenderUtilities.create(ParticleRegistry.WISP_PARTICLE).setAlpha(0.16F, 0.0F).setLifetime(20).setSpin(0.2F).setScale(0.2F, 0.0F).setColor(block.sapColor, block.sapColor).enableNoClip().randomOffset(0.10000000149011612D, 0.10000000149011612D).randomVelocity(0.0010000000474974513D, 0.0010000000474974513D).evenlyRepeatEdges(level, pos, 8, new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST});
            // RenderUtilities.create(ParticleRegistry.SMOKE_PARTICLE).setAlpha(0.08F, 0.0F).setLifetime(40).setSpin(0.1F).setScale(0.4F, 0.0F).setColor(block.sapColor, block.sapColor).randomOffset(0.20000000298023224D).enableNoClip().randomVelocity(0.0010000000474974513D, 0.0010000000474974513D).evenlyRepeatEdges(level, pos, 12, new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST});
        }

    }
}
