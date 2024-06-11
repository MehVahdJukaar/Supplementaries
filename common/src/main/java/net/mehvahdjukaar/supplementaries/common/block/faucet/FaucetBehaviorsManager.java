package net.mehvahdjukaar.supplementaries.common.block.faucet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FaucetBehaviorsManager extends RegistryAccessJsonReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final FaucetBehaviorsManager RELOAD_INSTANCE = new FaucetBehaviorsManager();

    private final Set<Object> dataInteractions = new HashSet<>();

    public FaucetBehaviorsManager() {
        super(GSON, "faucet_interactions");
    }

    private static final Codec<Either<DataItemInteraction, DataFluidInteraction>> CODEC = Codec.either(DataItemInteraction.CODEC, DataFluidInteraction.CODEC);


    @Override
    public void parse(Map<ResourceLocation, JsonElement> map, RegistryAccess registryAccess) {

        FaucetBlockTile.removeDataInteractions(dataInteractions);
        dataInteractions.clear();
        map.forEach((key, json) -> {
            try {
                var result = CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registryAccess), json);
                var d = result.getOrThrow(false, e -> Supplementaries.LOGGER.error("Failed to fluid interaction: {}", e));
                Object o;
                var l = d.left();
                if (l.isPresent()) o = l.get();
                else o = d.right().get();
                dataInteractions.add(o);
                FaucetBlockTile.registerInteraction(o);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for faucet interaction " + key);
            }
        });
        if (!dataInteractions.isEmpty())
            Supplementaries.LOGGER.info("Loaded  " + dataInteractions.size() + " custom faucet interactions");
    }


    public static void registerBehaviors() {
        FaucetBlockTile.registerInteraction(new SoftFluidProviderInteraction());
        FaucetBlockTile.registerInteraction(new WaterCauldronInteraction());
        FaucetBlockTile.registerInteraction(new LavaCauldronInteraction());
        FaucetBlockTile.registerInteraction(new PowderSnowCauldronInteraction());
        FaucetBlockTile.registerInteraction(new BeehiveInteraction());
        FaucetBlockTile.registerInteraction(new SoftFluidTankInteraction());
        FaucetBlockTile.registerInteraction(new APIFluidTankInteraction());
        FaucetBlockTile.registerInteraction(new BrewingStandInteraction());
        FaucetBlockTile.registerInteraction(new FiniteFluidInteraction());
        FaucetBlockTile.registerInteraction(new LiquidBlockInteraction());
        FaucetBlockTile.registerInteraction(new SpongeInteraction());
        FaucetBlockTile.registerInteraction(new XPDroppingInteraction());
        FaucetBlockTile.registerInteraction(new ConcreteInteraction());
        FaucetBlockTile.registerInteraction(new MudInteraction());
        FaucetBlockTile.registerInteraction(new ContainerItemInteraction());
        if (CompatHandler.AUTUMNITY) FaucetBlockTile.registerInteraction(new SappyLogInteraction());
        if (CompatHandler.FARMERS_RESPRITE) FaucetBlockTile.registerInteraction(new KettleInteraction());
    }


    @Deprecated(forRemoval = true)
    public static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid, @Nullable CompoundTag tag) {
    }

}



