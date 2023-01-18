package net.mehvahdjukaar.supplementaries.common.block.faucet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile.*;
import net.mehvahdjukaar.supplementaries.common.misc.songs.Song;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class FaucetBehaviorsManager extends SimpleJsonResourceReloadListener {

    public static final SongsManager RELOAD_INSTANCE = new SongsManager();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Set<DataSourceInteraction> dataInteractions = new HashSet<>();

    public FaucetBehaviorsManager() {
        super(GSON, "faucet_interactions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        FaucetBlockTile.removeDataInteractions(dataInteractions);
        dataInteractions.clear();
        jsons.forEach((key, json) -> {
            try {
                var result = DataSourceInteraction.CODEC.parse(JsonOps.INSTANCE, json);
                var d = result.getOrThrow(false, e -> Supplementaries.LOGGER.error("Failed to fluid interaction: {}", e));
                dataInteractions.add(d);
                FaucetBlockTile.registerInteraction(d);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for faucet interaction " + key);
            }
        });
        if (dataInteractions.size() != 0) Supplementaries.LOGGER.info("Loaded  " + dataInteractions.size() + " custom faucet interactions");
    }


    public static void registerBehaviors() {
        FaucetBlockTile.registerInteraction(new SoftFluidProviderInteraction());
        FaucetBlockTile.registerInteraction(new WaterCauldronInteraction());
        FaucetBlockTile.registerInteraction(new LavaCauldronInteraction());
        FaucetBlockTile.registerInteraction(new PowderSnowCauldronInteraction());
        FaucetBlockTile.registerInteraction(new BeehiveInteraction());
        FaucetBlockTile.registerInteraction(new SoftFluidTankInteraction());
        FaucetBlockTile.registerInteraction(new ForgeFluidTankInteraction());
        FaucetBlockTile.registerInteraction(new WaterBlockInteraction());
        FaucetBlockTile.registerInteraction(new SpongeInteraction());
        FaucetBlockTile.registerInteraction(new XPDroppingInteraction());
        if (CompatHandler.BUZZIER_BEES) FaucetBlockTile.registerInteraction(new HoneyPotInteraction());
        if (CompatHandler.AUTUMNITY) FaucetBlockTile.registerInteraction(new SappyLogInteraction());
    }




    private static class MalumInteraction implements IFaucetBlockSource {

        @Override
        public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                          BlockPos pos, BlockState state, FillAction fillAction) {
  /* else if (CompatHandler.malum && MalumPlugin.isSappyLog(backBlock)) {
            this.prepareToTransferBottle(MalumPlugin.getSap(backBlock));
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                MalumPlugin.extractSap(level, backState, behind);
                return true;
            }
        }*/
            return InteractionResult.PASS;
        }
    }



    static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid) {
        tempFluidHolder.fill(softFluid);
        tempFluidHolder.setCount(2);
    }

    static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid, CompoundTag tag) {
        tempFluidHolder.fill(softFluid, tag);
        tempFluidHolder.setCount(2);
    }

    static void prepareToTransferBucket(SoftFluidTank tempFluidHolder, SoftFluid softFluid) {
        tempFluidHolder.fill(softFluid);
    }

}



