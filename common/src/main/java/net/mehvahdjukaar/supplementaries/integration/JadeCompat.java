package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.AbstractPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import snownee.jade.addon.universal.ItemStorageProvider;
import snownee.jade.api.*;
import snownee.jade.api.view.*;

import java.util.List;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerItemStorage(new HideItemsProvider<>(Supplementaries.res("present")), AbstractPresentBlockTile.class);
        registration.registerItemStorage(new HideItemsProvider<>(Supplementaries.res("safe")), SafeBlockTile.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerItemStorageClient(new HideItemsProvider<>(Supplementaries.res("present")));
        registration.registerItemStorageClient(new HideItemsProvider<>(Supplementaries.res("safe")));
    }

    public record HideItemsProvider<T extends BaseContainerBlockEntity>(ResourceLocation id) implements
            IServerExtensionProvider<T, ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {

        @Override
        public ResourceLocation getUid() {
            return id;
        }

        @Override
        public @NotNull List<ViewGroup<ItemStack>> getGroups(ServerPlayer player, ServerLevel world, T blockEntity, boolean showDetails) {
            if (blockEntity instanceof SafeBlockTile || blockEntity instanceof AbstractPresentBlockTile) {
                if (blockEntity.canOpen(player)) {
                    ItemStorageProvider.INSTANCE.getGroups(player, world, blockEntity, showDetails);
                }
            }
            return List.of();
        }

        @Override
        public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> list) {
            if(accessor.getHitResult() instanceof BlockHitResult h) {
                Level level = accessor.getLevel();
                BlockEntity blockEntity = level.getBlockEntity(h.getBlockPos());
                if (blockEntity instanceof SafeBlockTile || blockEntity instanceof AbstractPresentBlockTile) {
                    Player player = accessor.getPlayer();
                    if (((BaseContainerBlockEntity) blockEntity).canOpen(player)) {
                        ItemStorageProvider.INSTANCE.getClientGroups(accessor, list);
                    }
                }
            }
            return List.of();
        }
    }

}
