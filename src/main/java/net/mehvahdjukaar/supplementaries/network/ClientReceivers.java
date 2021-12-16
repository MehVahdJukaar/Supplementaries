package net.mehvahdjukaar.supplementaries.network;

import com.mojang.text2speech.Narrator;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.client.gui.IScreenProvider;
import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.inventories.RedMerchantContainerMenu;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;

import java.util.function.Consumer;

public class ClientReceivers {

    private static void withPlayerDo(Consumer<Player> action) {
        var player = Minecraft.getInstance().player;
        if (player != null) action.accept(player);
    }

    private static void withLevelDo(Consumer<Level> action) {
        var level = Minecraft.getInstance().level;
        if (level != null) action.accept(level);
    }

    public static void handleOpenScreenPacket(ClientBoundOpenScreenPacket message) {
        withLevelDo(l -> {
            BlockPos pos = message.getPos();
            if (l.getBlockEntity(pos) instanceof IScreenProvider tile) {
                withPlayerDo((p) -> tile.openScreen(l, pos, p));
            }
        });
    }

    public static void handlePlaySpeakerMessagePacket(ClientBoundPlaySpeakerMessagePacket message) {
        //TODO: add @p command support
        boolean narrator = message.getNarrator();
        Component str = message.getStr();
        if (narrator && !ClientConfigs.cached.SPEAKER_BLOCK_MUTE) {
            Narrator.getNarrator().say(str.getString(), true);
        } else {
            withPlayerDo((p) -> p.sendMessage(str, Util.NIL_UUID));
        }
    }

    public static void handleSendBombKnockbackPacket(ClientBoundSendBombKnockbackPacket message) {
        withPlayerDo((p) -> p.setDeltaMovement(p.getDeltaMovement()
                .add(message.getKnockbackX(), message.getKnockbackY(), message.getKnockbackZ())));
    }

    public static void handleSendLoginMessagePacket(ClientBoundSendLoginMessagePacket message) {
        withPlayerDo(p -> {
            if (ClientConfigs.general.ANTI_REPOST_WARNING.get()) {
                try {
                    String fileName = ModList.get().getModFileById(Supplementaries.MOD_ID).getFile().getFileName();
                    if (fileName.contains(".jar")) {
                        if (!fileName.toLowerCase().contains("supplementaries-1") || fileName.toLowerCase().contains("supplementaries-mod") || fileName.contains("supplementaries-1.16.53")) {
                            MutableComponent link = new TranslatableComponent("message.supplementaries.anti_repost_link");
                            String url = "http://www.curseforge.com/minecraft/mc-mods/supplementaries";
                            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
                            link.setStyle(link.getStyle().withClickEvent(click).setUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));

                            p.sendMessage(new TranslatableComponent("message.supplementaries.anti_repost", link), Util.NIL_UUID);
                            p.sendMessage(new TranslatableComponent("message.supplementaries.anti_repost_2"), Util.NIL_UUID);
                            //player.sendMessage(ForgeHooks.newChatWithLinks(, false), Util.DUMMY_UUID);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    public static void handleSpawnBlockParticlePacket(ClientBoundSpawnBlockParticlePacket message) {
        withLevelDo(l -> {
            if (message.getId() == 0) {
                ParticleUtil.spawnParticlesOnBlockFaces(l, message.getPos(),
                        ModRegistry.SUDS_PARTICLE.get(),
                        UniformInt.of(2, 4), 0.001f, 0.01f, true);

            }
        });
    }

    public static void handleSyncAntiqueInkPacket(ClientBoundSyncAntiqueInk message) {
        withLevelDo(l -> {
            BlockEntity tile = l.getBlockEntity(message.getPos());
            if (tile != null) {
                tile.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP).ifPresent(c -> c.setAntiqueInk(message.getInk()));
            }
        });
    }

    public static void handleSyncTradesPacket(ClientBoundSyncTradesPacket message) {
        withPlayerDo(p -> {
            AbstractContainerMenu container = p.containerMenu;
            if (message.getContainerId() == container.containerId && container instanceof RedMerchantContainerMenu containerMenu) {
                containerMenu.setOffers(new MerchantOffers(message.offers.createTag()));
                containerMenu.setXp(message.getVillagerXp());
                containerMenu.setMerchantLevel(message.getVillagerLevel());
                containerMenu.setShowProgressBar(message.isShowProgress());
                containerMenu.setCanRestock(message.isCanRestock());
            }
        });
    }
}
