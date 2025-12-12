package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;
import java.util.Optional;

public class SpawnEntityWithPassengersFeature extends Feature<SpawnEntityWithPassengersFeature.Config> {

    public SpawnEntityWithPassengersFeature() {
        super(SpawnEntityWithPassengersFeature.Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {

        Config config = context.config();
        BlockPos blockPos = context.origin();
        WorldGenLevel level = context.level();
        ServerLevel serverLevel = level.getLevel();

        for (int i = 0; i < config.attempts + 5; i++) {
            int dx = context.random().nextIntBetweenInclusive(-config.spread, config.spread);
            int dz = context.random().nextIntBetweenInclusive(-config.spread, config.spread);
            BlockPos spawnPos = blockPos.offset(dx, 0, dz);
            if (config.projection == StructureTemplatePool.Projection.TERRAIN_MATCHING) {
                //same that projection processor does
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockPos.getX(), blockPos.getZ());
                spawnPos = spawnPos.atY(y);
                //  level.setBlock(spawnPos, Blocks.GLOWSTONE.defaultBlockState(), 2);
            }
            if (level.isEmptyBlock(spawnPos)) {

                BlockPos groundPos = spawnPos.below();
                if (config.groundRule.test(level.getBlockState(groundPos), context.random())) {
                    if (trySpawningAt(context, config, serverLevel, spawnPos, level)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean trySpawningAt(FeaturePlaceContext<Config> context, Config config,
                                         ServerLevel serverLevel, BlockPos blockPos,
                                         WorldGenLevel worldgenLevel) {
        Entity boat = config.entity.create(serverLevel);
        if (boat == null) return false;
        if (boat instanceof Boat b && config.data.boatType.isPresent()) {
            b.setVariant(config.data.boatType.get().toVanillaBoat());
            if (boat instanceof CannonBoatEntity cb) {
                cb.setWoodType(config.data.boatType.get());
            }
        }
        if (boat instanceof ContainerEntity cb && config.data.lootTable.isPresent()) {
            cb.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, config.data.lootTable.get()));
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
                if (config.persistent) m.setPersistenceRequired();
                m.finalizeSpawn(worldgenLevel, worldgenLevel.getCurrentDifficultyAt(blockPos),
                        MobSpawnType.STRUCTURE, null);
            }
        }
        //TODO: figure out why this deadlocks
        //if (!worldgenLevel.hasChunkAt(blockPos) || !worldgenLevel.noCollision(boat)) {
        //    return false;
        //}
        MinecraftServer server = serverLevel.getServer();
        server.executeIfPossible(() -> {
            //some bullshit bukkit or c2me error hat doesnt like spawning an entity offthread
            serverLevel.addFreshEntityWithPassengers(boat);
        });
        return true;
    }

    public record Config(EntityType<?> entity, List<EntityType<?>> passengers,
                         int spread, StructureTemplatePool.Projection projection, int attempts, RuleTest groundRule,
                         boolean persistent, ExtraData data) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(Config::entity),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().optionalFieldOf("passengers", List.of()).forGetter(Config::passengers),
                Codec.INT.optionalFieldOf("spread", 0).forGetter(Config::spread),
                StructureTemplatePool.Projection.CODEC.optionalFieldOf("projection", StructureTemplatePool.Projection.RIGID)
                        .forGetter(Config::projection),
                Codec.INT.optionalFieldOf("attempts", 1).forGetter(Config::attempts),
                RuleTest.CODEC.optionalFieldOf("ground_rule", AlwaysTrueTest.INSTANCE).forGetter(Config::groundRule),
                Codec.BOOL.optionalFieldOf("persistent", true).forGetter(Config::persistent),
                ExtraData.CODEC.optionalFieldOf("data", ExtraData.EMPTY).forGetter(Config::data)
        ).apply(instance, Config::new));
    }


    record ExtraData(Optional<WoodType> boatType, Optional<ResourceLocation> lootTable) {
        private static final Codec<ExtraData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                WoodType.CODEC.optionalFieldOf("boat_type").forGetter(ExtraData::boatType),
                ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(ExtraData::lootTable)
        ).apply(instance, ExtraData::new));

        public static final ExtraData EMPTY = new ExtraData(Optional.empty(), Optional.empty());
    }

}
