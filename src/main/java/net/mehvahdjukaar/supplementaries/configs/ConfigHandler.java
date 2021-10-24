package net.mehvahdjukaar.supplementaries.configs;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SyncConfigsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmlclient.ConfigGuiHandler;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ConfigHandler {

    public static void init() {
        //need to register on 2 different busses, can't use subscribe event
        MinecraftForge.EVENT_BUS.addListener(ConfigHandler::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(ConfigHandler::onPlayerLoggedOut);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigHandler::reloadConfigsEvent);
    }

    public static void openModConfigs(){
        Minecraft mc = Minecraft.getInstance();

        mc.setScreen(ModList.get().getModContainerById(Supplementaries.MOD_ID).get()
                .getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class).get().screenFunction()
                .apply(mc, mc.screen));
    }

    public static <T> void resetConfigValue(ForgeConfigSpec spec, ForgeConfigSpec.ConfigValue<T> value) {
        ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(value.getPath());
        if (valueSpec == null) Supplementaries.LOGGER.throwing(
                new Exception("No such config value: " + value + "in config " + spec));
        value.set((T) valueSpec.getDefault());
    }

    //maybe not needed anymore now with predicated below
    public static <T> T safeGetListString(ForgeConfigSpec spec, ForgeConfigSpec.ConfigValue<T> value) {
        Object o = value.get();
        //resets failed config value
        try {
            T o1 = (T) o;
        } catch (Exception e) {
            Supplementaries.LOGGER.warn(
                    new Exception("Resetting erroneous config value: " + value + "in config " + spec));
            resetConfigValue(spec, value);
        }
        return value.get();
    }

    public static Predicate<Object> STRING_CHECK = o -> o instanceof String;

    public static Predicate<Object> LIST_STRING_CHECK = o -> o instanceof List<?> && ((Collection<?>) o).stream().allMatch(s -> s instanceof String);

    public static void reloadConfigsEvent(ModConfigEvent event) {
        //TODO: common aren't working...
        if (event.getConfig().getSpec() == ServerConfigs.SERVER_SPEC) {
            //send this configuration to connected clients
            syncServerConfigs();
            ServerConfigs.cached.refresh();
        } else if (event.getConfig().getSpec() == ClientConfigs.CLIENT_SPEC)
            ClientConfigs.cached.refresh();
    }


    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            //send this configuration to connected clients
            syncServerConfigs(event.getPlayer());
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        //TODO: fix server configs sync
        if (event.getPlayer().level.isClientSide) {
            //reload local common configs
            //maybe not needed
            ServerConfigs.loadLocal();
            ServerConfigs.cached.refresh();
        }
    }


    public static Path getServerConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(Supplementaries.MOD_ID + "-common.toml").toAbsolutePath();
    }

    public static void syncServerConfigs() {
        final MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            final PlayerList playerList = currentServer.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                syncServerConfigs(player);
            }
        }

    }

    public static void syncServerConfigs(Player player) {
        try {
            final byte[] configData = Files.readAllBytes(getServerConfigPath());
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SyncConfigsPacket(configData));
        } catch (IOException e) {
            Supplementaries.LOGGER.error(Supplementaries.MOD_ID + ": Failed to sync common configs", e);
        }
    }


    //TODO: remake config system

    public static class CachedConfigValue<T, C extends ForgeConfigSpec.ConfigValue<T>> {
        private T cached;
        private final C config;

        public CachedConfigValue(C config) {
            this.config = config;
            this.refresh();
        }

        public T get() {
            return cached;
        }

        public void refresh() {
            this.cached = this.config.get();
        }

    }

}
