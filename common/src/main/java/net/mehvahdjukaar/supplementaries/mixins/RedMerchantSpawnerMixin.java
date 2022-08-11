package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.RedMerchantEntity;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WanderingTraderSpawner.class)
public abstract class RedMerchantSpawnerMixin {

    @Shadow @Final private RandomSource random;
    @Final
    @Shadow
    private ServerLevelData serverLevelData;

    @Unique
    private int redSpawnDelay = 0;

    //remove
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(ServerLevel p_230253_1_, boolean p_230253_2_, boolean p_230253_3_, CallbackInfoReturnable<Integer> cir) {
        if (this.redSpawnDelay > 0) {
            this.redSpawnDelay--;
        }
    }


    @Inject(method = "spawn", at = @At("RETURN"))
    public void spawn(ServerLevel world, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && redSpawnDelay == 0) {
            //doesn't set cir to true, so it doesn't interfere with wandering trader spawn
            Player player = world.getRandomPlayer();
            //1/10 chance here already. raised it a bit since when normal one spawns it prevents this
            if (player != null && this.random.nextInt(9) == 0) {

                BlockPos blockpos = player.blockPosition();

                //17.5 % max on hard ->1.75% (wandering trader maxes at 7.5%)
                if (this.calculateNormalizeDifficulty(world, blockpos) * CommonConfigs.Spawns.RED_MERCHANT_SPAWN_MULTIPLIER.get()
                        > random.nextFloat() * 90) {

                    PoiManager poiManager = world.getPoiManager();
                    Optional<BlockPos> optional = poiManager.find((h) -> h.is(PoiTypes.MEETING),
                            (pos) -> true, blockpos, 48, PoiManager.Occupancy.ANY);
                    BlockPos targetPos = optional.orElse(blockpos);
                    BlockPos spawnPos = this.findSpawnPositionNear(world, targetPos, 48);
                    if (spawnPos != null && this.hasEnoughSpace(world, spawnPos)) {
                        if (!world.getBiome(spawnPos).is(Biomes.THE_VOID)) {

                            RedMerchantEntity trader = ModRegistry.RED_MERCHANT.get().spawn(world, null, null, null, spawnPos, MobSpawnType.EVENT, false, false);
                            if (trader != null) {
                                this.serverLevelData.setWanderingTraderId(trader.getUUID());
                                int lifetime = 25000;
                                trader.setDespawnDelay(lifetime);
                                trader.setWanderTarget(targetPos);
                                trader.restrictTo(targetPos, 16);
                                this.redSpawnDelay = lifetime;
                            }
                        }
                    }
                }
            }
        }

    }

    private float calculateNormalizeDifficulty(ServerLevel world, BlockPos pos) {
        float dragon = 1;
        CompoundTag tag = world.getServer().getWorldData().endDragonFightData();

        if (tag.contains("DragonKilled", 99)) {

            if (tag.getBoolean("DragonKilled")) dragon = 1.25f;
        }

        long i = 0L;
        float f = 0.0F;
        if (world.hasChunkAt(pos)) {
            f = world.getMoonBrightness();
            i = world.getChunkAt(pos).getInhabitedTime();
        }
        //goes from 1.5 to 4 on normal
        DifficultyInstance instance = new DifficultyInstance(Difficulty.NORMAL, world.getDayTime(), i, f);
        float diff = instance.getEffectiveDifficulty();
        diff -= 1.5;
        //from 0 to 10
        diff *= 4;
        float scale = switch (world.getDifficulty()) {
            case PEACEFUL -> 1;
            case EASY -> 1.25f;
            case NORMAL -> 1.5f;
            case HARD -> 1.75f;
        };

        //max 17.5
        diff *= scale;
        //max 21.875
        diff *= dragon;

        //ho ho ho
        if(CommonUtil.FESTIVITY.isChristmas()) diff *= 8;

        return diff;
    }

    @Shadow
    protected abstract boolean hasEnoughSpace(BlockGetter reader, BlockPos pos);

    @Shadow
    protected abstract BlockPos findSpawnPositionNear(LevelReader world, BlockPos pos, int i);
}