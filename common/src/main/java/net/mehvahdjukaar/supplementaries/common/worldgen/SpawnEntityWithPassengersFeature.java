package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;
import java.util.Optional;

public class SpawnEntityWithPassengersFeature extends Feature<SpawnEntityWithPassengersFeature.Config> {

    public SpawnEntityWithPassengersFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        BlockPos blockPos = context.origin();
        WorldGenLevel level = context.level();
        ServerLevel serverLevel = level.getLevel();

        Entity boat = config.base.create(serverLevel);
        if (boat == null) return false;
        if (boat instanceof Boat b && config.boatType.isPresent()) {
            b.setVariant(config.boatType.get());
        }
        boat.moveTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5,
                360 * context.random().nextFloat(), 0);
        for (EntityType<?> passengerType : config.passengers) {
            Entity passenger = passengerType.create(serverLevel);
            if (passenger != null) {
                passenger.startRiding(boat);
            }
        }

        for (var e : boat.getSelfAndPassengers().toList()) {
            if (e instanceof Mob m) {
                m.setPersistenceRequired();
                m.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPos), MobSpawnType.STRUCTURE, null);
            }
        }
        level.addFreshEntityWithPassengers(boat);
        return true;
    }

    public record Config(EntityType<?> base, List<EntityType<?>> passengers,
                         Optional<Boat.Type> boatType) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("base").forGetter(Config::base),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().fieldOf("passengers").forGetter(Config::passengers),
                Boat.Type.CODEC.optionalFieldOf("boat_type").forGetter(Config::boatType)
        ).apply(instance, Config::new));
    }
}
