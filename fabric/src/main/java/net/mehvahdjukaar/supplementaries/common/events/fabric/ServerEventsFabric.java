package net.mehvahdjukaar.supplementaries.common.events.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ServerEventsFabric {

    public static void init() {
        UseBlockCallback.EVENT.register(ServerEventsFabric::onRightClickBlock);
        UseItemCallback.EVENT.register(ServerEvents::onUseItem);
        ServerPlayConnectionEvents.JOIN.register((l, s, m) -> ServerEvents.onPlayerLoggedIn(l.player));
        UseEntityCallback.EVENT.register(ServerEvents::onRightClickEntity);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ServerEvents::onDataSync);
        CommonLifecycleEvents.TAGS_LOADED.register(ServerEvents::onCommonTagUpdate);
        ServerEntityEvents.ENTITY_LOAD.register(ServerEvents::onEntityLoad);
        LootTableEvents.MODIFY.register((m, t, r, b, s) -> ServerEvents.injectLootTables(t, r, b::withPool));

        //  BiomeModificationImpl.INSTANCE.addModifier();
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
                        stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(hand));
                        level.setBlockAndUpdate(pos, RakedGravelBlock.getConnectedState(raked, level, pos, player.getDirection()));
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

/*
    @Deprecated
    public static void addStuffToBiomes(BiomeLoadingEvent event) {

        Biome.BiomeCategory category = event.getCategory();
        if (category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND && category != Biome.BiomeCategory.NONE) {

            if (ServerConfigs.spawn.URN_PILE_ENABLED.get()) {
                if (!ServerConfigs.spawn.URN_BIOME_BLACKLIST.get().contains(event.getName().toString())) {
                    if (!event.getName().getNamespace().equals("twilightforest")) //TODO: find a better way to handle dimensons with weird land height
                        event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, ModPlacedFeatures.PLACED_CAVE_URNS);
                }
            }

            if (ServerConfigs.spawn.WILD_FLAX_ENABLED.get()) {

                ResourceLocation res = event.getName();
                if (res != null && category != Biome.BiomeCategory.UNDERGROUND) {

                    ResourceKey<Biome> key = ResourceKey.create(ForgeRegistries.Keys.BIOMES, res);
                    Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
                    if (types.contains(SANDY) && (types.contains(HOT) || types.contains(DRY)) || types.contains(RIVER)) {
                        event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PLACED_WILD_FLAX_PATCH);
                    }
                }
            }
        }
    }
*/


}
