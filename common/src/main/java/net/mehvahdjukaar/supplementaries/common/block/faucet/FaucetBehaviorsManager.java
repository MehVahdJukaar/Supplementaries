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
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.misc.FakeLevel;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.fake_level.BlockTestLevel;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

public class FaucetBehaviorsManager extends RegistryAccessJsonReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final FaucetBehaviorsManager RELOAD_INSTANCE = new FaucetBehaviorsManager();

    private final Set<Object> dataInteractions = new HashSet<>();
    private final Set<Runnable> listeners = new HashSet<>();

    public FaucetBehaviorsManager() {
        super(GSON, "faucet_interactions");
    }

    private static final Codec<Either<DataItemInteraction, DataFluidInteraction>> CODEC = Codec.either(DataItemInteraction.CODEC, DataFluidInteraction.CODEC);

    public static void addRegisterFaucetInteraction(Runnable listener) {
        RELOAD_INSTANCE.listeners.add(listener);
    }

    @Override
    public void parse(Map<ResourceLocation, JsonElement> map, RegistryAccess registryAccess) {
        FaucetBlockTile.clearBehaviors();

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
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to parse JSON object for faucet interaction {}", key);
            }
        });
        if (!dataInteractions.isEmpty())
            Supplementaries.LOGGER.info("Loaded  {} custom faucet interactions", dataInteractions.size());

    }

    public void onLevelLoad(ServerLevel level) {
        FaucetBlockTile.clearBehaviors();

        dataInteractions.forEach(FaucetBlockTile::registerInteraction);

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
        for (var e : CauldronInteraction.EMPTY.entrySet()) {
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

        listeners.forEach(Runnable::run);
    }


    @Deprecated(forRemoval = true)
    public static void prepareToTransferBottle(SoftFluidTank tempFluidHolder, SoftFluid softFluid, @Nullable CompoundTag tag) {
    }

}



