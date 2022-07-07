package net.mehvahdjukaar.supplementaries.common.events;


import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RakedGravelBlock;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.entities.goals.EatFodderGoal;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.common.items.CandyItem;
import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.MovableFakePlayer;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.mehvahdjukaar.supplementaries.common.world.songs.FluteSongsReloadListener;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Set;


public class ServerEvents {

    //block placement should stay low in priority to allow other more important mod interaction that use the event
    @EventCalled
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) {
           return ItemsOverrideHandler.tryPerformClickedBlockOverride(player, level, hand, hitResult, false);
        }
        return InteractionResult.PASS;
    }

    @EventCalled
    public static InteractionResult onRightClickBlockHP(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isSpectator()) {
           return ItemsOverrideHandler.tryHighPriorityClickedBlockOverride(player, level, hand, hitResult);
        }
        return InteractionResult.PASS;
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player playerIn = event.getPlayer();

        ItemsOverrideHandler.tryPerformClickedItemOverride(event, playerIn.getItemInHand(event.getHand()));
    }

    //TODO: soap tool event
    @SubscribeEvent
    public static void toolModification(BlockEvent.BlockToolModificationEvent event){
        if(event.getToolAction() == ToolActions.HOE_TILL && ServerConfigs.cached.RAKED_GRAVEL){
            LevelAccessor world = event.getWorld();
            BlockPos pos = event.getPos();
            if (event.getFinalState().is(net.minecraft.world.level.block.Blocks.GRAVEL)) {
                BlockState raked = ModRegistry.RAKED_GRAVEL.get().defaultBlockState();
                if (raked.canSurvive(world, pos)) {
                    event.setFinalState(RakedGravelBlock.getConnectedState(raked, world, pos, event.getContext().getHorizontalDirection()));
                    //world.setBlock(pos, RakedGravelBlock.getConnectedState(raked, world, pos, context.getHorizontalDirection()), 11);
                    //world.playSound(context.getPlayer(), pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    //event.setResult(Event.Result.ALLOW);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onAttachTileCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        CapabilityHandler.attachCapabilities(event);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            CandyItem.checkSweetTooth(event.player);
        }
    }

    private static final boolean FODDER_ENABLED = RegistryConfigs.FODDER_ENABLED.get();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (FODDER_ENABLED) {
            Entity entity = event.getEntity();
            if (entity instanceof Animal animal) {
                EntityType<?> type = event.getEntity().getType();
                if (type.is(ModTags.EATS_FODDER)) {
                    animal.goalSelector.addGoal(3,
                            new EatFodderGoal(animal, 1, 8, 2, 30));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            NetworkHandler.CHANNEL.sendToPlayerClient((ServerPlayer) event.getPlayer(),
                    new ClientBoundSendLoginPacket(UsernameCache.getMap()));
        } catch (Exception exception) {
            Supplementaries.LOGGER.warn("failed to end login message: " + exception);
        }
        GlobeData.sendGlobeData(event);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new FluteSongsReloadListener());
    }

    @SubscribeEvent
    public static void noteBlockEvent(final NoteBlockEvent.Play event) {
        SongsManager.recordNote(event.getWorld(), event.getPos());
    }

    //TODO: remove when forge PR gets approved. using mixin right now
    //antique ink sync
    //@SubscribeEvent
    public static void onPlayerStartTracking(final ChunkWatchEvent.Watch event) {
        ServerLevel serverLevel = event.getWorld();

        //TODO: this is slow af. mixin in ChunkHolder
        ChunkAccess chunk = serverLevel.getChunkSource().getChunk(event.getPos().x, event.getPos().z, ChunkStatus.FULL, false);
        if (chunk != null) {
            Set<BlockPos> positions = chunk.getBlockEntitiesPos();
            LevelAccessor level = event.getWorld();
            for (BlockPos pos : positions) {
                BlockEntity te = level.getBlockEntity(pos);
                if (te != null) {
                    var cap = te.getCapability(CapabilityHandler.ANTIQUE_TEXT_CAP);
                    if (cap.isPresent()) {
                        MinecraftServer server = serverLevel.getServer();
                        var c = cap.orElse(null);
                        boolean a = c.hasAntiqueInk();
                        server.tell(new TickTask(server.getTickCount(), () ->
                                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(event::getPlayer),
                                        new ClientBoundSyncAntiqueInk(pos, a))));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSaplingGrow(SaplingGrowTreeEvent event) {
        if(ServerConfigs.Blocks.PLANTER_BREAKS.get()) {
            LevelAccessor level = event.getWorld();
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos.below());
            if (state.getBlock() instanceof PlanterBlock) {
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos.below(), net.minecraft.world.level.block.Block.getId(state));
                level.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.ROOTED_DIRT.defaultBlockState(), 2);
                level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 0.71f);
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileImpact(final ProjectileImpactEvent event) {
        PearlMarker.onProjectileImpact(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDimensionUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ServerLevel serverLevel)
            MovableFakePlayer.unloadLevel(serverLevel);
    }

    //for flute and cage
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof FluteItem) {
            if (FluteItem.interactWithPet(stack, event.getPlayer(), event.getTarget(), event.getHand())) {
                event.setCancellationResult(InteractionResult.SUCCESS); // we need this for event to be actually cancelled
                event.setCanceled(true);
            }
        } else if (stack.getItem() instanceof AbstractMobContainerItem containerItem) {
            if (!containerItem.isFull(stack)) {
                var res = containerItem.doInteract(stack, event.getPlayer(), event.getTarget(), event.getHand());
                if (res.consumesAction()){
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void reloadConfigsEvent(ModConfigEvent event) {

        if (event.getConfig().getSpec() == ServerConfigs.SERVER_SPEC) {
            //send this configuration to connected clients

            // sendSyncedConfigsToAllPlayers();
            // ServerConfigs.cached.refresh();
        } else if (event.getConfig().getSpec() == ClientConfigs.CLIENT_SPEC){
            CapturedMobsHelper.refreshVisuals();
            GlobeTextureManager.GlobeColors.refreshColorsFromConfig();

        }

    }

}
