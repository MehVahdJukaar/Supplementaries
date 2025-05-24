package net.mehvahdjukaar.supplementaries.common.network;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.screens.widgets.PlayerSuggestionBoxWidget;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlintBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MovingSlidyBlock;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingSlidyBlockEntity;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBallEntity;
import net.mehvahdjukaar.supplementaries.common.entities.IFluteParrot;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.common.inventories.RedMerchantMenu;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.items.SongInstrumentItem;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
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
            Entity e = l.getEntity(message.id());
            if (e != null) e.addDeltaMovement(message.knockback());
        });
    }

    public static void handleLoginPacket(ClientBoundSendLoginPacket message) {
        withPlayerDo(p -> PlayerSuggestionBoxWidget.setUsernameCache(message.usernameCache()));
    }

    public static void handleSpawnBlockParticlePacket(ClientBoundParticlePacket message) {
        withLevelDo(l -> {
            //bubble blow
            final RandomSource ran = l.random;
            switch (message.type) {
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
                case GLOW_ON -> {
                    ParticleUtil.spawnParticleOnBlockShape(l, BlockPos.containing(message.pos),
                            ParticleTypes.GLOW,
                            UniformInt.of(3, 5), 0);
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
                case BUBBLE_EAT -> {
                    var entity = l.getEntity(message.extraData);

                    Vec3 v = message.dir.normalize();
                    double x = entity.getX() + v.x;
                    double y = entity.getEyeY() + v.y - 0.12;
                    double z = entity.getZ() + v.z;
                    for (int j = 0; j < 4; j++) {
                        v = v.scale(0.1 + ran.nextFloat() * 0.1f);
                        double dx = v.x + ((0.5 - ran.nextFloat()) * 0.9);
                        double dy = v.y + ((0.5 - ran.nextFloat()) * 0.06);
                        double dz = v.z + ((0.5 - ran.nextFloat()) * 0.9);

                        l.addParticle(ModParticles.SUDS_PARTICLE.get(), x, y, z, dx, dy, dz);
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
                        double d4 = ran.nextDouble() * 0.2D + 0.01D;
                        double d6 = d18 + j1 * 0.01D + (ran.nextDouble() - 0.5D) * k2 * 0.5D;
                        double d8 = d24 + j2 * 0.01D + (ran.nextDouble() - 0.5D) * j2 * 0.5D;
                        double d30 = d28 + k2 * 0.01D + (ran.nextDouble() - 0.5D) * j1 * 0.5D;
                        double d9 = j1 * d4 + ran.nextGaussian() * 0.01D;
                        double d10 = j2 * d4 + ran.nextGaussian() * 0.01D;
                        double d11 = k2 * d4 + ran.nextGaussian() * 0.01D;
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
                case CONFETTI -> {
                    spawnConfettiParticles(message, l, ran);
                }
                case CONFETTI_EXPLOSION -> {
                    int radius = message.extraData;
                    ParticleUtil.spawnParticleInASphere(l, message.pos.x, message.pos.y + 1, message.pos.z,
                            () -> ran.nextInt(6) == 0 ?
                                    ModParticles.STREAMER_PARTICLE.get() :
                                    ModParticles.CONFETTI_PARTICLE.get(), radius * 40,
                            radius / 9f,
                            0.05f, 0.15f * radius / 3
                    );
                    //same volume as explosion code
                    l.playLocalSound(message.pos.x, message.pos.y, message.pos.z, ModSounds.CONFETTI_POPPER.get(),
                            SoundSource.HOSTILE, 4, ran.nextFloat() * 0.2F + 0.5F, false);
                }
                case FEATHER -> {
                    int amount = message.extraData == null ? 1 : message.extraData;
                    double dy = Mth.clamp((0.03 * message.dir.y / 7f), 0.03, 0.055);
                    for (int i = 0; i < amount; i++) {
                        l.addParticle(ModParticles.FEATHER_PARTICLE.get(),
                                message.pos.x + ran.nextGaussian() * 0.35,
                                message.pos.y,
                                message.pos.z + ran.nextGaussian() * 0.35,
                                ran.nextGaussian() * 0.007,
                                dy * 0.5,
                                ran.nextGaussian() * 0.007
                        );
                    }
                }
                case WRENCH_ROTATION -> {
                    if (ClientConfigs.Items.WRENCH_PARTICLES.get()) {
                        l.addParticle(ModParticles.ROTATION_TRAIL_EMITTER.get(),
                                message.pos.x(), message.pos.y(), message.pos.z(),
                                message.extraData,
                                0.71, -1);
                    }
                }
                case PEARL_TELEPORT -> {
                    // same as teleport
                    Vec3 start = message.pos;
                    Vec3 end = message.dir;
                    for (int j = 0; j < 64; ++j) {
                        double delta = ran.nextDouble();
                        float f = (ran.nextFloat() - 0.5F) * 0.2F;
                        float f1 = (ran.nextFloat() - 0.5F) * 0.2F;
                        float f2 = (ran.nextFloat() - 0.5F) * 0.2F;
                        double dx = Mth.lerp(delta, end.x(), start.x()) + (ran.nextDouble() - 0.5D);
                        double dy = Mth.lerp(delta, end.y(), start.y()) + (ran.nextDouble() - 0.5D) - 0.5f;
                        double dz = Mth.lerp(delta, end.z(), start.z()) + (ran.nextDouble() - 0.5D);
                        l.addParticle(ParticleTypes.PORTAL, dx, dy, dz, f, f1, f2);
                    }
                }
            }
        });
    }

    public static void spawnConfettiParticles(ClientBoundParticlePacket message, Level l, RandomSource ran) {
        float spread = 0.1f;
        var dir = message.dir;
        var pos = message.pos;
        float scale = message.extraData != null ? (message.extraData + 1) * 0.8f : 1;
        for (int j = 0; j < 60; ++j) {

            Vector3f facingDir = randomizeVector(ran, dir, spread)
                    .mul(scale * Mth.nextFloat(ran, 0.3f, 0.7f));
            SimpleParticleType p = ran.nextInt(6) == 0 ?
                    ModParticles.STREAMER_PARTICLE.get() :
                    ModParticles.CONFETTI_PARTICLE.get();
            l.addParticle(p, pos.x, pos.y, pos.z,
                    facingDir.x, facingDir.y, facingDir.z);
        }

        l.playLocalSound(message.pos.x, message.pos.y, message.pos.z, ModSounds.CONFETTI_POPPER.get(),
                SoundSource.PLAYERS, 1.0f, ran.nextFloat() * 0.2F + 0.8F, false);
    }

    public static void handleSetSlidingBlockEntityPacket(ClientBoundSetSlidingBlockEntityPacket m) {
        withLevelDo(l -> {
            // only updates it if client doesnt already have it, means if tile code run from server only
            if (!(l.getBlockEntity(m.pos()) instanceof MovingSlidyBlockEntity)) {

                BlockPos pos = m.pos();
                l.setBlock(m.pos(), m.state(), Block.UPDATE_MOVE_BY_PISTON);
                Direction direction = m.direction();
                MovingSlidyBlockEntity be = MovingSlidyBlock.newMovingBlockEntity(pos, m.state(), m.movedState(), direction);
                l.setBlockEntity(be);
                // dont you ask me why this is here. it makes the animation smooth. no clue why needed
                //be.addOffset(-(float) (double) CommonConfigs.Building.SLIDY_BLOCK_SPEED.get());
            }
        });
    }

    public static void handleSyncHourglassData(ClientBoundSendHourglassDataPacket packet) {
        withLevelDo(l -> {
            HourglassTimesManager.getInstance(l)
                    .setData(packet.hourglassTimes);
            Supplementaries.LOGGER.info("Synced Hourglass data");
        });
    }

    public static void handleSyncBookTypes(ClientBoundSendBookDataPacket packet) {
        withLevelDo(l -> {
            PlaceableBookManager.getInstance(l.registryAccess())
                    .setData(packet.bookTypes);
            Supplementaries.LOGGER.info("Synced Placeable Book Types");
        });
    }

    public static void handleSyncCapturedMobs(ClientBoundSendCapturedMobsPacket packet) {
        withLevelDo(l -> {
            CapturedMobHandler.getInstance(l).acceptData(packet.mobSet, packet.fish);
            Supplementaries.LOGGER.info("Synced Captured Mobs settings");
        });
    }

    public static void handleSyncCannonBoat(ClientBoundUpdateCannonBoatPacket packet) {
        withLevelDo(l -> {
            CannonAccess access = CannonAccess.find(l, packet.target());
            if (access != null) {
                access.getCannon().loadWithComponents(packet.tileTag(), l.registryAccess());
            }
        });

    }


    //triangle distribution?
    private double r(RandomSource random, double a) {
        return a * (random.nextFloat() + random.nextFloat() - 1);
    }

    public static Vector3f randomizeVector(RandomSource random, Vec3 mean, float spread) {
        Vector3f facing = mean.toVector3f();
        Vector3f ort = findOrthogonalVector(facing);
        ort.rotateAxis(random.nextFloat() * Mth.TWO_PI, facing.x, facing.y, facing.z);
        ort.mul((float) (random.nextGaussian() * spread));

        facing.add(ort).normalize();
        return facing;
    }

    // Helper function to find an arbitrary vector orthogonal to the given vector
    private static Vector3f findOrthogonalVector(Vector3f v) {
        if (Math.abs(v.x) > Math.abs(v.y)) {
            return new Vector3f(-v.z, 0, v.x).normalize();
        } else {
            return new Vector3f(0, v.z, -v.y).normalize();
        }
    }

    public static void handleSyncAntiqueInkPacket(ClientBoundSyncAntiqueInk message) {
        withLevelDo(l -> {
            BlockEntity tile = l.getBlockEntity(message.pos());
            if (tile != null) {
                AntiqueInkItem.setAntiqueInk(tile, message.ink());
            }
        });
    }

    public static void handlePlaySongNotesPacket(ClientBoundPlaySongNotesPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.entityID());
            if (e instanceof Player p && p.getUseItem().getItem() instanceof SongInstrumentItem instrumentItem) {
                for (int note : message.notes()) {
                    if (note > 0) {
                        //always plays a sound for local player. this is because this method is called on client side for other clients aswell
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

    public static void handleSyncTradesPacket(ClientBoundSyncTradesPacket message) {
        withPlayerDo(p -> {
            AbstractContainerMenu container = p.containerMenu;
            if (message.containerId == container.containerId && container instanceof RedMerchantMenu containerMenu) {
                containerMenu.setOffers(message.offers);
                containerMenu.setXp(message.villagerXp);
                containerMenu.setMerchantLevel(message.villagerLevel);
                containerMenu.setShowProgressBar(message.showProgress);
                containerMenu.setCanRestock(message.canRestock);
            }
        });
    }

    public static void handleSyncQuiverPacket(SyncEquippedQuiverPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.entityID());
            if (e instanceof IQuiverEntity qe) {
                qe.supplementaries$setQuiver(message.heldQuiver());
            }
        });
    }

    public static void handleSyncPartyCreeper(SyncPartyCreeperPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.entityID());
            if (e instanceof IPartyCreeper le) {
                le.supplementaries$setFestive(message.on());
            }
        });
    }

    public static void handleParrotPacket(ClientBoundFluteParrotsPacket message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.playerId());
            if (e == null) {
                Supplementaries.LOGGER.error("Entity not found for parrot packet");
                return;
            }
            if (message.playing() && e instanceof Player p) {
                BlockPos pos = e.blockPosition();
                List<LivingEntity> list = l.getEntitiesOfClass(LivingEntity.class, (new AABB(pos)).inflate(3.0));

                for (LivingEntity livingEntity : list) {
                    if (livingEntity instanceof IFluteParrot fp) {
                        fp.supplementaries$setPartyByFlute(p);
                    }
                }
            }

            setDisplayParrotsPartying(l, Either.left((Player) e), message.playing());
        });
    }

    public static void setDisplayParrotsPartying(Level level, Either<Player, BlockPos> source, boolean isPartying) {
        BlockPos pos;
        if (source.right().isPresent()) pos = source.right().get();
        else pos = source.left().get().blockPosition();

        List<Player> list = level.getEntitiesOfClass(Player.class, (new AABB(pos)).inflate(3.0));

        for (Player player : list) {
            var l = player.getShoulderEntityLeft();
            if (!l.isEmpty()) l.putBoolean("record_playing", isPartying);
            var r = player.getShoulderEntityRight();
            if (!r.isEmpty()) r.putBoolean("record_playing", isPartying);

        }
        Player p = source.left().orElse(null);

        int r = 3;
        BlockPos.MutableBlockPos mut = pos.mutable();
        for (int x = pos.getX() - r; x < pos.getX() + r; x++) {
            for (int y = pos.getY() - r; y < pos.getY() + r; y++) {
                for (int z = pos.getZ() - r; z < pos.getZ() + r; z++) {
                    BlockPos.MutableBlockPos ppp = mut.set(x, y, z);
                    if (level.getBlockEntity(ppp) instanceof IMobContainerProvider te) {
                        MobContainer container = te.getMobContainer();
                        Entity e = container.getDisplayedMob(level, ppp.immutable());
                        if (p == null && e instanceof LivingEntity le) {
                            le.setRecordPlayingNearby(pos, isPartying);
                        } else if (p != null && e instanceof IFluteParrot fp && isPartying) {
                            fp.supplementaries$setPartyByFlute(p);
                        }
                    }
                }
            }
        }
    }

    public static void handleExplosionPacket(ClientBoundCannonballExplosionPacket packet) {
        withLevelDo(l -> {
            Vec3 pos = packet.pos();
            float power = packet.power();
            List<BlockPos> toBlow = packet.toBlow();
            Vec3 knockback = packet.knockback();
            switch (packet.explosionType()) {
                case CANNONBALL -> {
                    Explosion explosion = new CannonBallExplosion(l, null, pos.x, pos.y, pos.z, power, toBlow);
                    explosion.finalizeExplosion(true);
                    if (l.getEntity(packet.getId()) instanceof CannonBallEntity le && knockback != null) {
                        le.setDeltaMovement(knockback);
                    }
                }
            }
        });
    }

    public static void handleSyncSlimed(ClientBoundSyncSlimedMessage message) {
        withLevelDo(l -> {
            Entity e = l.getEntity(message.id());
            if (e instanceof ISlimeable s) {
                s.supp$setSlimedTicks(message.duration(), false);
            }
        });
    }

    public static void handleCannonControlPacket(ClientBoundControlCannonPacket message) {
        withLevelDo(l -> {
            CannonAccess access = CannonAccess.find(l, message.target());
            if (access != null) {
                CannonController.startControlling(access);
            }
        });
    }


    public static void handleCannonAnimation(ClientBoundCannonAnimationPacket message) {
        withLevelDo(l -> {
            CannonAccess access = CannonAccess.find(l, message.target());
            if (access != null) {
                if(message.fire()) {
                    access.playFiringEffects();
                } else {
                    access.playIgniteEffects();
                }
            }
        });
    }


}
