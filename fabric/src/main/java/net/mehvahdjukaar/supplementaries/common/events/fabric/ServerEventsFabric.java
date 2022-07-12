package net.mehvahdjukaar.supplementaries.common.events.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesRegistry;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.world.songs.FluteSongsReloadListener;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
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
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.BlockHitResult;

public class ServerEventsFabric {

    public static void init() {
        UseBlockCallback.EVENT.register(ServerEventsFabric::onRightClickBlock);
        UseItemCallback.EVENT.register(ServerEvents::onUseItem);
        ServerPlayConnectionEvents.JOIN.register((l, s, m) -> ServerEvents.onPlayerLoggedIn(l.player));
        UseEntityCallback.EVENT.register(ServerEvents::onRightClickEntity);
        ServerWorldEvents.UNLOAD.register(ServerEvents::onWorldUnload);
        ServerEntityEvents.ENTITY_LOAD.register(ServerEvents::onEntityLoad);
        LootTableEvents.MODIFY.register((m, t, r, b, s) -> ServerEvents.injectLootTables(t, r, b::withPool));


        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricFluteReload());
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FabricWallLanternReload());
    }

    public static class FabricWallLanternReload extends WallLanternTexturesRegistry implements IdentifiableResourceReloadListener{
        @Override
        public ResourceLocation getFabricId() {
            return Supplementaries.res("wall_lantern_textures");
        }
    }

    public static class FabricFluteReload extends FluteSongsReloadListener implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return Supplementaries.res("flute_songs");
        }
    }


    private static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        var res = ServerEvents.onRightClickBlockHP(player, level, hand, hitResult);
        if (res != InteractionResult.PASS) return res;
        res = ServerEvents.onRightClickBlock(player, level, hand, hitResult);
        if (res != InteractionResult.PASS) return res;

        //raked gravel
        if (ServerConfigs.Tweaks.RAKED_GRAVEL.get()) {
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


}
