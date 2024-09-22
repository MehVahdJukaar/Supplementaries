package net.mehvahdjukaar.supplementaries.common.events.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.neoforge.VillagerScareStuff;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public class ServerEventsForge {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ServerEventsForge.class);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onRightClickBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onUseBlockHP(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onRightClickBlockHP(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (ret != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret);
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.isCanceled()) {
            var ret = ServerEvents.onUseItem(event.getEntity(), event.getLevel(), event.getHand());
            if (ret.getResult() != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(ret.getResult());
            }
        }
    }

    @SubscribeEvent
    public static void onAttachTileCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        CapabilityHandler.attachBlockEntityCapabilities(event);
    }

    //TODO: soap tool event
    @SubscribeEvent
    public static void toolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() == ToolActions.HOE_TILL && CommonConfigs.Tweaks.RAKED_GRAVEL.get()) {
            LevelAccessor world = event.getLevel();
            BlockPos pos = event.getPos();
            if (event.getFinalState().is(net.minecraft.world.level.block.Blocks.GRAVEL)) {
                BlockState raked = ModRegistry.RAKED_GRAVEL.get().defaultBlockState();
                if (raked.canSurvive(world, pos)) {
                    event.setFinalState(RakedGravelBlock.getConnectedState(raked, world, pos, event.getContext().getHorizontalDirection()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                NetworkHelper.sendToClientPlayer(player,
                        new ClientBoundSendLoginPacket(UsernameCache.getMap()));
            } catch (Exception exception) {
                Supplementaries.LOGGER.warn("failed to send login message: " + exception);
            }
            ServerEvents.onPlayerLoggedIn(player);
        }
    }

    @SubscribeEvent
    public static void onDataSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ServerEvents.onDataSyncToPlayer(event.getPlayer(), true);
        } else {
            for (var p : event.getPlayerList().getPlayers()) {
                ServerEvents.onDataSyncToPlayer(p, true);
            }
        }
    }

    //for flute and cage. fabric calls directly
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        var res = ServerEvents.onRightClickEntity(event.getEntity(), event.getLevel(),
                event.getHand(), event.getTarget(), null);
        if (res != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(res);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        var level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            ServerEvents.onEntityLoad(event.getEntity(), serverLevel);
        } else {
            ClientEvents.onEntityLoad(event.getEntity(), event.getLevel());
        }
    }


    //TODO: add these on fabric
    //forge only

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (event.side == LogicalSide.SERVER) {
                ServerEvents.serverPlayerTick(event.player);
            } else {
                ServerEvents.clientPlayerTick(event.player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileImpact(final ProjectileImpactEvent event) {
        PearlMarker.onProjectileImpact(event.getProjectile(), event.getRayTraceResult());
    }

    @SubscribeEvent
    public static void noteBlockEvent(final NoteBlockEvent.Play event) {
        SongsManager.recordNoteFromNoteBlock(event.getLevel(), event.getPos());

        if (event.getInstrument() == NoteBlockInstrument.ZOMBIE) {
            VillagerScareStuff.scareVillagers(event.getLevel(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!event.isCanceled() && event.getResult() != Event.Result.DENY) {
            if (ServerEvents.onItemPickup(event.getItem(), event.getEntity())) {
                event.setCanceled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(ServerStoppedEvent event) {
        ServerEvents.onServerStopped();
    }


    private static int counter = 0;
    private static boolean flag = false;

    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (!flag) {
            if (event.phase == TickEvent.Phase.START) {
                if (counter++ > 20) {
                    VibeChecker.checkVibe(event.level);
                    flag = true;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingHurtEvent event) {
        if (event.getEntity() instanceof Cat cat) {

            if (CommonConfigs.Tweaks.BAD_LUCK_CAT.get() &&
                    cat.getVariant() == BuiltInRegistries.CAT_VARIANT.get(CatVariant.ALL_BLACK) &&
                    event.getSource().getEntity() instanceof LivingEntity p) {
                p.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 20 * 60 * 5));
            }
        }
    }


    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {

        if (CommonConfigs.Tweaks.SLIME_OVERLAY.get()) {
            LivingEntity entity = event.getEntity();
            ISlimeable slimed = (ISlimeable) entity;
            int t = slimed.supp$getSlimedTicks();
            if (t > 0) {
                if (entity.isUnderWater()) {
                    slimed.supp$setSlimedTicks(0, true);
                } else slimed.supp$setSlimedTicks(t - 1, false);
            }
        }
    }


}