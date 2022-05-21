package net.mehvahdjukaar.supplementaries.common.network;

import com.mojang.text2speech.Narrator;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.IScreenProvider;
import net.mehvahdjukaar.supplementaries.client.gui.widgets.PlayerSuggestionBoxWidget;
import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.inventories.RedMerchantContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.InstrumentItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
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
        boolean narrator = message.narrator;
        Component str = message.str;
        if (narrator && !ClientConfigs.cached.SPEAKER_BLOCK_MUTE) {
            Narrator.getNarrator().say(str.getString(), true);
        } else {
            withPlayerDo((p) -> p.sendMessage(str, Util.NIL_UUID));
        }
    }

    public static void handleSendBombKnockbackPacket(ClientBoundSendKnockbackPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.id);
            if (e != null) e.setDeltaMovement(e.getDeltaMovement()
                    .add(message.knockbackX, message.knockbackY, message.knockbackZ));

        });
    }

    public static void handleLoginPacket(ClientBoundSendLoginPacket message) {
        withPlayerDo(p -> {
            PlayerSuggestionBoxWidget.USERNAME_CACHE = message.usernameCache;
            if (ClientConfigs.general.ANTI_REPOST_WARNING.get()) {
                try {
                    String fileName = ModList.get().getModFileById(Supplementaries.MOD_ID).getFile().getFileName();
                    if (fileName.contains(".jar")) {
                        if (fileName.contains("-Mod-1")) {
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
            //bubble blow
            switch (message.id) {
                case BUBBLE_BLOW -> {
                    ParticleUtil.spawnParticlesOnBlockFaces(l, new BlockPos(message.pos),
                            ModRegistry.SUDS_PARTICLE.get(),
                            UniformInt.of(2, 4), 0.001f, 0.01f, true);
                }
                case BUBBLE_CLEAN -> {
                    ParticleUtil.spawnParticleOnBlockShape(l, new BlockPos(message.pos),
                            ModRegistry.SUDS_PARTICLE.get(),
                            UniformInt.of(2, 4), 0.01f);
                }
                case DISPENSER_MINECART ->{
                    int j1 = 0;
                    int j2 = 1;
                    int k2 = 0;
                    double d18 = message.pos.x + (double)j1 * 0.6D;
                    double d24 = message.pos.y + (double)j2 * 0.6D;
                    double d28 = message.pos.z + (double)k2 * 0.6D;

                    for(int i3 = 0; i3 < 10; ++i3) {
                        double d4 = l.random.nextDouble() * 0.2D + 0.01D;
                        double d6 = d18 + (double)j1 * 0.01D + (l.random.nextDouble() - 0.5D) * (double)k2 * 0.5D;
                        double d8 = d24 + (double)j2 * 0.01D + (l.random.nextDouble() - 0.5D) * (double)j2 * 0.5D;
                        double d30 = d28 + (double)k2 * 0.01D + (l.random.nextDouble() - 0.5D) * (double)j1 * 0.5D;
                        double d9 = (double)j1 * d4 + l.random.nextGaussian() * 0.01D;
                        double d10 = (double)j2 * d4 + l.random.nextGaussian() * 0.01D;
                        double d11 = (double)k2 * d4 + l.random.nextGaussian() * 0.01D;
                        l.addParticle(ParticleTypes.SMOKE, d6, d8, d30, d9, d10, d11);
                    }
                }
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

    public static void handlePlaySongNotesPacket(ClientBoundPlaySongNotesPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.entityID);
            if (e instanceof Player p && p.getUseItem().getItem() instanceof InstrumentItem instrumentItem) {
                for (int note : message.notes) {
                    if (note > 0) {
                        //always plays a sound for local player. this is becasue this method is calld on client side for other clients aswell
                        //and playsound only plays if the given player is the local one
                        l.playSound(Minecraft.getInstance().player, p.getX(), p.getY(), p.getZ(),
                                instrumentItem.getSound(), SoundSource.PLAYERS,
                                instrumentItem.getVolume(), instrumentItem.getPitch(note));

                        instrumentItem.spawnNoteParticle(l, p, note);
                    }
                }
            }
        });

    }
}
