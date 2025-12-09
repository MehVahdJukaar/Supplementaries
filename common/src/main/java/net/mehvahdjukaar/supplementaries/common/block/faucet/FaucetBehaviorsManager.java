package net.mehvahdjukaar.supplementaries.common.block.faucet;

import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.misc.SidedInstance;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevelManager;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
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
import net.minecraft.server.MinecraftServer;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

//really we just use this on the server. its info should not be needed on the client
public class FaucetBehaviorsManager extends SimpleJsonResourceReloadListener {

    private static final Codec<Either<DataItemInteraction, DataFluidInteraction>> CODEC =
            Codec.either(DataItemInteraction.CODEC, DataFluidInteraction.CODEC);

    private static final Set<Consumer<IFaucetEvent>> SERVER_LISTENERS = new HashSet<>();
    private static final SidedInstance<FaucetBehaviorsManager> INSTANCES = SidedInstance.of(FaucetBehaviorsManager::new);

    public static FaucetBehaviorsManager getInstance(HolderLookup.Provider ra) {
        return INSTANCES.get(ra);
    }

    public static FaucetBehaviorsManager getInstance(@NotNull Level level) {
        Preconditions.checkNotNull(level);
        return getInstance(level.registryAccess());
    }

    public interface IFaucetEvent {
        void registerInteraction(Object interaction);
    }

    synchronized public static void addRegisterFaucetInteractions(Consumer<IFaucetEvent> listener) {
        SERVER_LISTENERS.add(listener);
    }

    private final List<FaucetSource.BlState> blockInteractions = new ArrayList<>();
    private final List<FaucetSource.Tile> tileInteraction = new ArrayList<>();
    private final List<FaucetSource.Fluid> sourceFluidInteractions = new ArrayList<>();
    private final List<FaucetItemSource> itemInteractions = new ArrayList<>();
    private final List<FaucetTarget.BlState> targetBlockInteractions = new ArrayList<>();
    private final List<FaucetTarget.Tile> targetTileInteractions = new ArrayList<>();
    private final List<FaucetTarget.Fluid> targetFluidInteractions = new ArrayList<>();


    private final HolderLookup.Provider registryAccess;

    public FaucetBehaviorsManager(HolderLookup.Provider ra) {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(),
                "faucet_interactions");
        this.registryAccess = ra;

        INSTANCES.set(ra, this);
    }

    public Iterable<FaucetSource.BlState> getBlockInteractions() {
        return blockInteractions;
    }

    public Iterable<FaucetSource.Tile> getTileInteractions() {
        return tileInteraction;
    }

    public Iterable<FaucetSource.Fluid> getSourceFluidInteractions() {
        return sourceFluidInteractions;
    }

    public Iterable<FaucetItemSource> getItemInteractions() {
        return itemInteractions;
    }

    public Iterable<FaucetTarget.BlState> getTargetBlockInteractions() {
        return targetBlockInteractions;
    }

    public Iterable<FaucetTarget.Tile> getTargetTileInteractions() {
        return targetTileInteractions;
    }

    public Iterable<FaucetTarget.Fluid> getTargetFluidInteractions() {
        return targetFluidInteractions;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        tileInteraction.clear();
        sourceFluidInteractions.clear();
        itemInteractions.clear();
        targetBlockInteractions.clear();
        targetTileInteractions.clear();
        targetFluidInteractions.clear();

        RegistryOps<JsonElement> ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess, this);
        var codec = ForgeHelper.conditionalCodec(CODEC);
        List<DataItemInteraction> dataInteractions = new ArrayList<>();
        List<DataFluidInteraction> dataFluidInteractions = new ArrayList<>();
        map.forEach((key, json) -> {
            try {
                var either = codec.parse(ops, json).getOrThrow().get();
                if (either.left().isPresent()) {
                    var interaction = either.left().get();
                    dataInteractions.add(interaction);
                } else if (either.right().isPresent()) {
                    var interaction = either.right().get();
                    dataFluidInteractions.add(interaction);
                }
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for faucet interaction {}", key, e);
            }
        });
        if (!dataFluidInteractions.isEmpty()) {
            Supplementaries.LOGGER.info("Loaded  {} custom faucet interactions", dataFluidInteractions.size() + dataInteractions.size());
        }

        dataInteractions.forEach(this::registerInteraction);
        dataFluidInteractions.forEach(this::registerInteraction);


        registerInteraction(new SoftFluidProviderInteraction());
        registerInteraction(new WaterCauldronInteraction());
        registerInteraction(new FullBucketCauldronInteraction(Blocks.LAVA_CAULDRON.defaultBlockState(), Items.LAVA_BUCKET.getDefaultInstance()));
        registerInteraction(new PowderSnowCauldronInteraction());
        registerInteraction(new BeehiveInteraction());
        registerInteraction(new SoftFluidTankInteraction());
        registerInteraction(new APIFluidTankInteraction());
        registerInteraction(new BrewingStandInteraction());
        registerInteraction(new FiniteFluidInteraction());
        registerInteraction(new LiquidBlockInteraction());
        registerInteraction(new SpongeInteraction());
        registerInteraction(new XPDroppingInteraction());
        registerInteraction(new ConcreteInteraction());
        registerInteraction(new MudInteraction());
        registerInteraction(new ContainerItemInteraction());
        if (CompatHandler.AUTUMNITY) registerInteraction(new SappyLogInteraction());
        //if (CompatHandler.FARMERS_RESPRITE) FaucetBlockTile.registerInteraction(new KettleInteraction());


        SERVER_LISTENERS.forEach(l -> l.accept(FaucetBehaviorsManager.this::registerInteraction));

        //true when data reload
        MinecraftServer currentServer = PlatHelper.getCurrentServer();
        if (currentServer == null) return;
        ServerLevel overworld = currentServer.overworld();
        this.onLevelLoad(overworld);
    }

    public void onLevelLoad(ServerLevel level) {
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
                    registerInteraction(new FullBucketCauldronInteraction(fullCauldron, fullBucketCopy));
                }
            }

        }
        FakeLevelManager.invalidate(testLevel);
    }

    protected void registerInteraction(Object interaction) {
        boolean success = false;
        if (interaction instanceof FaucetSource.BlState bs) {
            blockInteractions.add(bs);
            success = true;
        }
        if (interaction instanceof FaucetSource.Tile ts) {
            tileInteraction.add(ts);
            success = true;
        }
        if (interaction instanceof FaucetSource.Fluid bs) {
            sourceFluidInteractions.add(bs);
            success = true;
        }
        if (interaction instanceof FaucetTarget.BlState tb) {
            targetBlockInteractions.add(tb);
            success = true;
        }
        if (interaction instanceof FaucetTarget.Tile tt) {
            targetTileInteractions.add(tt);
            success = true;
        }
        if (interaction instanceof FaucetTarget.Fluid tf) {
            targetFluidInteractions.add(tf);
            success = true;
        }
        if (interaction instanceof FaucetItemSource is) {
            itemInteractions.add(is);
            success = true;
        }
        if (!success)
            throw new UnsupportedOperationException("Unsupported faucet interaction class: " + interaction.getClass().getSimpleName());
    }

    @Deprecated(forRemoval = true)
    public static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid, @Nullable CompoundTag tag) {
    }

}



