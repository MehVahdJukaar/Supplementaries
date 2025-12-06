package net.mehvahdjukaar.supplementaries.common.events.fabric;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.phys.BlockHitResult;

public class ServerEventsFabric {

    public static void init() {
        UseBlockCallback.EVENT.register(ServerEventsFabric::onRightClickBlock);
        UseItemCallback.EVENT.register(ServerEvents::onUseItem);
        ServerPlayConnectionEvents.JOIN.register((l, s, m) -> ServerEvents.onPlayerLoggedIn(l.player));
        UseEntityCallback.EVENT.register(ServerEvents::onRightClickEntity);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ServerEvents::onDataSyncToPlayer);
        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            ServerEvents.beforeServerStart(s.registryAccess());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register(ServerEvents::onLivingDeath);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var p : server.getPlayerList().getPlayers()) {
                ServerEvents.serverPlayerTick(p);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            ServerEvents.onServerStopped();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(ServerEvents::onServerStart);

        if (CommonConfigs.Functional.URN_PILE_ENABLED.get() && CommonConfigs.Functional.URN_ENABLED.get()) {
            BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_CAVE_URNS),
                    GenerationStep.Decoration.UNDERGROUND_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Supplementaries.res("cave_urns")));
        }
        if (CommonConfigs.Functional.WILD_FLAX_ENABLED.get() && CommonConfigs.Functional.FLAX_ENABLED.get()) {
            BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_WILD_FLAX),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Supplementaries.res("wild_flax")));
        }
        if (CommonConfigs.Building.BASALT_ASH_ENABLED.get() && CommonConfigs.Building.ASH_ENABLED.get()) {
            BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_BASALT_ASH),
                    GenerationStep.Decoration.UNDERGROUND_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Supplementaries.res("basalt_ash")));
        }
        if(CommonConfigs.Building.BARNACLES_ENABLED.get()){
            BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_BARNACLES),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Supplementaries.res("shore_barnacles")));
            BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_BARNACLES),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Supplementaries.res("ocean_barnacles")));
        }
    }

    private static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        var res = ServerEvents.onRightClickBlockHP(player, level, hand, hitResult);
        if (res != InteractionResult.PASS) return res;
        res = ServerEvents.onRightClickBlock(player, level, hand, hitResult);
        if (res != InteractionResult.PASS) return res;

        //raked gravel
        if (CommonConfigs.Tweaks.RAKED_GRAVEL.get()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof HoeItem) {
                BlockPos pos = hitResult.getBlockPos();
                if (level.getBlockState(pos).is(Blocks.GRAVEL)) {
                    BlockState raked = ModRegistry.RAKED_GRAVEL.get().defaultBlockState();
                    if (raked.canSurvive(level, pos)) {
                        level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                        level.setBlockAndUpdate(pos, RakedGravelBlock.getConnectedState(raked, level, pos, player.getDirection()));
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }


}
