package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.PlayerSuggestionBoxWidget;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlintBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.RedMerchantMenu;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.items.InstrumentItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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

    public static void handlePlaySpeakerMessagePacket(ClientBoundPlaySpeakerMessagePacket message) {
        var mode = message.mode;
        Component str = Minecraft.getInstance().isTextFilteringEnabled() ? message.filtered : message.message;
        if (mode == SpeakerBlockTile.Mode.NARRATOR && !ClientConfigs.Blocks.SPEAKER_BLOCK_MUTE.get()) {
            Minecraft.getInstance().getNarrator().narrator.say(str.getString(), true);
        } else if (mode == SpeakerBlockTile.Mode.TITLE) {
            Gui gui = Minecraft.getInstance().gui;
            gui.clear();
            gui.resetTitleTimes();
            gui.setTitle(str);
        } else {
            withPlayerDo((p) -> p.displayClientMessage(str, mode == SpeakerBlockTile.Mode.STATUS_MESSAGE));
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
        withPlayerDo(p -> PlayerSuggestionBoxWidget.setUsernameCache(message.usernameCache));
    }

    public static void handleSpawnBlockParticlePacket(ClientBoundParticlePacket message) {
        withLevelDo(l -> {
            //bubble blow
            switch (message.id) {
                case BUBBLE_BLOW -> {
                    ParticleUtil.spawnParticlesOnBlockFaces(l, BlockPos.containing(message.pos),
                            ModParticles.SUDS_PARTICLE.get(),
                            UniformInt.of(2, 4), 0.001f, 0.01f, true);
                }
                case BUBBLE_CLEAN -> {
                    ParticleUtil.spawnParticleOnBlockShape(l, BlockPos.containing(message.pos),
                            ModParticles.SUDS_PARTICLE.get(),
                            UniformInt.of(2, 4), 0.01f);
                }
                case WAX_ON -> {
                    ParticleUtil.spawnParticleOnBlockShape(l, BlockPos.containing(message.pos),
                            ParticleTypes.WAX_ON,
                            UniformInt.of(3, 5), 0.01f);
                }
                case BUBBLE_CLEAN_ENTITY -> {
                    if (message.extraData != null) {
                        var e = l.getEntity(message.extraData);
                        if (e != null) {
                            ParticleUtil.spawnParticleOnBoundingBox(e.getBoundingBox(), l,
                                    ModParticles.SUDS_PARTICLE.get(), UniformInt.of(2, 4), 0.01f);
                        }
                    }
                }
                case DISPENSER_MINECART -> {
                    int j1 = 0;
                    int j2 = 1;
                    int k2 = 0;
                    double d18 = message.pos.x + j1 * 0.6D;
                    double d24 = message.pos.y + j2 * 0.6D;
                    double d28 = message.pos.z + k2 * 0.6D;

                    for (int i3 = 0; i3 < 10; ++i3) {
                        double d4 = l.random.nextDouble() * 0.2D + 0.01D;
                        double d6 = d18 + j1 * 0.01D + (l.random.nextDouble() - 0.5D) * k2 * 0.5D;
                        double d8 = d24 + j2 * 0.01D + (l.random.nextDouble() - 0.5D) * j2 * 0.5D;
                        double d30 = d28 + k2 * 0.01D + (l.random.nextDouble() - 0.5D) * j1 * 0.5D;
                        double d9 = j1 * d4 + l.random.nextGaussian() * 0.01D;
                        double d10 = j2 * d4 + l.random.nextGaussian() * 0.01D;
                        double d11 = k2 * d4 + l.random.nextGaussian() * 0.01D;
                        l.addParticle(ParticleTypes.SMOKE, d6, d8, d30, d9, d10, d11);
                    }
                }
                case FLINT_BLOCK_IGNITE -> {
                    if (message.extraData != null && message.pos != null) {
                        boolean isIronMoving = message.extraData == 1;
                        BlockPos pos = BlockPos.containing(message.pos);

                        for (var ironDir : Direction.values()) {
                            BlockPos facingPos = pos.relative(ironDir);
                            BlockState facingState = l.getBlockState(facingPos);

                            if (isIronMoving ? facingState.is(ModRegistry.FLINT_BLOCK.get()) :
                                    FlintBlock.canBlockCreateSpark(facingState, l, facingPos, ironDir.getOpposite())) {
                                for (int i = 0; i < 6; i++) {
                                    ParticleUtil.spawnParticleOnFace(l, facingPos,
                                            ironDir.getOpposite(),
                                            ParticleTypes.CRIT, -0.5f, 0.5f, false);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static void handleSyncAntiqueInkPacket(ClientBoundSyncAntiqueInk message) {
        withLevelDo(l -> {
            BlockEntity tile = l.getBlockEntity(message.pos);
            if (tile != null) {
                AntiqueInkItem.setAntiqueInk(tile, message.ink);
            }
        });
    }

    public static void handlePlaySongNotesPacket(ClientBoundPlaySongNotesPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.entityID);
            if (e instanceof Player p && p.getUseItem().getItem() instanceof InstrumentItem instrumentItem) {
                for (int note : message.notes) {
                    if (note > 0) {
                        //always plays a sound for local player. this is because this method is called on client side for other clients aswell
                        //and playground only plays if the given player is the local one
                        l.playSound(Minecraft.getInstance().player, p.getX(), p.getY(), p.getZ(),
                                instrumentItem.getSound(), SoundSource.PLAYERS,
                                instrumentItem.getVolume(), instrumentItem.getPitch(note));

                        instrumentItem.spawnNoteParticle(l, p, note);
                    }
                }
            }
        });

    }

    public static void handleSyncTradesPacket(ClientBoundSyncTradesPacket message) {
        withPlayerDo(p -> {
            AbstractContainerMenu container = p.containerMenu;
            if (message.containerId == container.containerId && container instanceof RedMerchantMenu containerMenu) {
                containerMenu.setOffers(new MerchantOffers(message.offers.createTag()));
                containerMenu.setXp(message.villagerXp);
                containerMenu.setMerchantLevel(message.villagerLevel);
                containerMenu.setShowProgressBar(message.showProgress);
                containerMenu.setCanRestock(message.canRestock);
            }
        });
    }

    public static void handleSyncQuiverPacket(SyncSkellyQuiverPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.entityID);
            if (e instanceof IQuiverEntity qe) {
                qe.supplementaries$setQuiver(message.on ? ModRegistry.QUIVER_ITEM.get().getDefaultInstance() : ItemStack.EMPTY);
            }
        });
    }

}
