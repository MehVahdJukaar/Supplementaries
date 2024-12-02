package net.mehvahdjukaar.supplementaries.common.block.faucet;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.BlockTestLevel;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class FaucetBehaviorsManager extends SimpleJsonResourceReloadListener {

    private static final Codec<Either<DataItemInteraction, DataFluidInteraction>> CODEC =
            Codec.either(DataItemInteraction.CODEC, DataFluidInteraction.CODEC);

    private static final Set<Runnable> SERVER_LISTENERS = new HashSet<>();
    private static final SidedInstance<FaucetBehaviorsManager> INSTANCES = SidedInstance.of(FaucetBehaviorsManager::new);

    public static FaucetBehaviorsManager getInstance(HolderLookup.Provider ra) {
        return INSTANCES.get(ra);
    }

    public static FaucetBehaviorsManager getInstance(Level level) {
        return getInstance(level.registryAccess());
    }

    public static void addRegisterFaucetInteraction(Runnable listener) {
        SERVER_LISTENERS.add(listener);
    }

    private final Set<Object> dataInteractions = new HashSet<>();
    private final HolderLookup.Provider registryAccess;

    public FaucetBehaviorsManager(HolderLookup.Provider ra) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "faucet_interactions");
        this.registryAccess = ra;

        INSTANCES.set(ra, this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        dataInteractions.clear();
        map.forEach((key, json) -> {
            try {
                var either = CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registryAccess), json).getOrThrow();
                Object o = either.mapBoth(i -> i, f -> f);
                dataInteractions.add(o);
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for faucet interaction {}", key);
            }
        });
        if (!dataInteractions.isEmpty())
            Supplementaries.LOGGER.info("Loaded  {} custom faucet interactions", dataInteractions.size());

    }

    public static void onLevelLoad(ServerLevel level) {
        var instance = getInstance(level);
        FaucetBlockTile.clearBehaviors();

        instance.dataInteractions.forEach(FaucetBlockTile::registerInteraction);

        FaucetBlockTile.registerInteraction(new SoftFluidProviderInteraction());
        FaucetBlockTile.registerInteraction(new WaterCauldronInteraction());
        FaucetBlockTile.registerInteraction(new FullBucketCauldronInteraction(Blocks.LAVA_CAULDRON.defaultBlockState(), Items.LAVA_BUCKET.getDefaultInstance()));
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
        //if (CompatHandler.FARMERS_RESPRITE) FaucetBlockTile.registerInteraction(new KettleInteraction());

        BlockTestLevel testLevel = BlockTestLevel.get(level);
        Player player = FakePlayerManager.getDefault(testLevel);
        InteractionHand hand = InteractionHand.MAIN_HAND;
        BlockState emptyCauldron = Blocks.CAULDRON.defaultBlockState();
        for (var e : CauldronInteraction.EMPTY.map().entrySet()) {
            Item i = e.getKey();
            CauldronInteraction interaction = e.getValue();
            // skip vanilla. we already registered them
            if (!Utils.getID(i).getNamespace().equals("minecraft")) {
                testLevel.setup();
                ItemStack fullBucket = i.getDefaultInstance();
                ItemStack fullBucketCopy = fullBucket.copy();
                player.setItemInHand(hand, fullBucket);
                interaction.interact(emptyCauldron, testLevel, BlockPos.ZERO, player, hand, fullBucket);
                BlockState fullCauldron = testLevel.blockState;
                if (fullCauldron != null) {
                    // reject layered cauldrons as we dont know how to treat them due to conversion issues (forge only)
                    if (fullCauldron.hasProperty(LayeredCauldronBlock.LEVEL) && PlatHelper.getPlatform().isForge()) {
                        continue;
                    }
                    FaucetBlockTile.registerInteraction(new FullBucketCauldronInteraction(fullCauldron, fullBucketCopy));
                }
            }

        }
        testLevel.invalidate();

        SERVER_LISTENERS.forEach(Runnable::run);
    }


    @Deprecated(forRemoval = true)
    public static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid, @Nullable CompoundTag tag) {
    }

}



