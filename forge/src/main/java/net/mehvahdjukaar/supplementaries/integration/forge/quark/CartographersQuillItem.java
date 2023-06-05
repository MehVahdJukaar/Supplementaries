package net.mehvahdjukaar.supplementaries.integration.forge.quark;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.integration.forge.QuarkCompatImpl;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.mehvahdjukaar.supplementaries.reg.RegUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.jetbrains.annotations.Nullable;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.tools.item.PathfindersQuillItem;
import vazkii.quark.content.tools.module.PathfinderMapsModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CartographersQuillItem extends PathfindersQuillItem {

    public static final String TAG_STRUCTURE = "targetStructure";
    public static final String TAG_SKIP_KNOWN = "skinKnown";
    public static final String TAG_SEARCH_RADIUS = "maxSearchRadius";
    public static final String TAG_ZOOM = "zoomLevel";
    public static final String TAG_DECORATION = "decoration";
    public static final String TAG_NAME = "decoration";

    protected static final String TAG_RADIUS = "searchRadius";
    protected static final String TAG_POS_INDEX = "searchIndex";
    protected static final String TAG_WAITING = "waiting";

    public CartographersQuillItem() {
        super(ModuleLoader.INSTANCE.getModuleInstance(PathfinderMapsModule.class),
                new Properties().stacksTo(1)
                        .tab(RegUtils.getTab(CreativeModeTab.TAB_TOOLS, "adventurer_map")));
        QuarkCompatImpl.removeStuffFromARLHack();

    }

    private static Thread mainThread;

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> comps, TooltipFlag flags) {
        var tag = stack.getTag();
        if (tag != null) {
            if (ItemNBTHelper.getBoolean(stack, TAG_IS_SEARCHING, false))
                comps.add(getSearchingComponent().withStyle(ChatFormatting.BLUE));
        } else
            comps.add(Component.translatable("message.supplementaries.cartographers_quill").withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected String getFailedMessage() {
        return "message.supplementaries.quill_finished";
    }

    @Override
    protected String getFinishedMessage() {
        return "message.supplementaries.quill_failed";
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.category != null) {
            if (group == category || group == CreativeModeTab.TAB_SEARCH) {
                items.add(new ItemStack(this));
            }
        }
    }

    @Override
    public int getIterations() {
        return 500;//PathfinderMapsModule.pathfindersQuillSpeed;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            CompoundTag tag = player.getItemInHand(hand).getOrCreateTag();
            if (!tag.contains(TAG_STRUCTURE)) {
                //re-generate for empty one
                String str = selectRandomTarget(serverLevel, ModTags.ADVENTURE_MAP_DESTINATIONS);
                if (str != null) tag.putString(TAG_STRUCTURE, str);
            }
        }
        return super.use(level, player, hand);
    }

    @Nullable
    @Override
    public ResourceLocation getTarget(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String str = tag.getString(TAG_STRUCTURE);
        return new ResourceLocation(str);
    }

    @Nullable
    private Holder<Structure> getStructureHolder(ServerLevel level, ResourceLocation key) {
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
        var structure = reg.getHolder(ResourceKey.create(reg.key(), key));
        return structure.orElse(null);
    }

    @Override
    public ItemStack createMap(ServerLevel level, BlockPos targetPos, ResourceLocation structure, ItemStack original) {
        CompoundTag tag = original.getOrCreateTag();
        return AdventurerMapsHandler.createStructureMap(level, targetPos, getStructureHolder(level, structure),
                getZoomLevel(tag), getDecoration(tag), getMapName(tag), getColor(tag));
    }

    @Override
    protected InteractionResultHolder<BlockPos> searchConcurrent(ResourceLocation target, ItemStack stack,
                                                                 ServerLevel level, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        Holder<Structure> structure = getStructureHolder(level, target);
        State state = State.get(tag);

        if (structure == null || state == null) return InteractionResultHolder.fail(BlockPos.ZERO);
        BlockPos center = getOrCreateStartPos(tag, player);
        int radius = getSearchRadius(tag);
        boolean skipKnown = getSkipKnown(tag);

        Key key = new Key(GlobalPos.of(level.dimension(), center),
                structure.unwrapKey().get().location(), radius, skipKnown);

        if (COMPUTING.contains(key)) {
            return InteractionResultHolder.pass(BlockPos.ZERO);
        } else if (RESULTS.containsKey(key)) {
            var ret = RESULTS.remove(key);
            //EXECUTORS.submit(() -> RESULTS.remove(key)); //lmao. no lag spikes allowed. write is slow
            if (ret.getResult() == InteractionResult.PASS) {
                //this should never happen
                return InteractionResultHolder.fail(BlockPos.ZERO);
            }
            return ret;
        } else {
            ItemStack dummy = stack.copy();
            EXECUTORS.submit(() -> {
                COMPUTING.add(key);
                RESULTS.put(key, this.searchIterative(target, dummy, level, player, Integer.MAX_VALUE));
                COMPUTING.remove(key);
            });
            return InteractionResultHolder.pass(BlockPos.ZERO);
        }
    }

    @Override
    protected InteractionResultHolder<BlockPos> searchIterative(ResourceLocation target, ItemStack stack,
                                                                ServerLevel level, Player player, int maxIter) {
        CompoundTag tag = stack.getOrCreateTag();
        Holder<Structure> structure = getStructureHolder(level, target);
        State state = State.get(tag);

        if (structure == null || state == null) return InteractionResultHolder.fail(BlockPos.ZERO);
        BlockPos center = getOrCreateStartPos(tag, player);
        int radius = getSearchRadius(tag);
        boolean skipKnown = getSkipKnown(tag);

        return findNearestMapStructure(level, structure, radius, center, skipKnown, state, maxIter);
    }

    @Override
    protected ItemStack search(ItemStack stack, ServerLevel level, Player player, int slot) {
        if (mainThread == null) mainThread = Thread.currentThread();
        return super.search(stack, level, player, slot);
    }

    private BlockPos getOrCreateStartPos(CompoundTag tag, Player player) {
        if (tag.contains(TAG_SOURCE_X) && tag.contains(TAG_SOURCE_Z)) {
            int sourceX = tag.getInt(TAG_SOURCE_X);
            int sourceZ = tag.getInt(TAG_SOURCE_Z);
            return new BlockPos(sourceX, 64, sourceZ);
        } else {
            var pos = player.blockPosition();
            tag.putInt(TAG_SOURCE_X, pos.getX());
            tag.putInt(TAG_SOURCE_Z, pos.getZ());
            return pos;
        }
    }

    private int getSearchRadius(CompoundTag tag) {
        if (tag.contains(TAG_SEARCH_RADIUS)) {
            return tag.getInt(TAG_SEARCH_RADIUS);
        }
        return AdventurerMapsHandler.SEARCH_RADIUS;
    }

    @Nullable
    private String getMapName(CompoundTag tag) {
        if (tag.contains(TAG_NAME)) {
            return tag.getString(TAG_NAME);
        }
        return null;
    }

    private int getColor(CompoundTag tag) {
        if (tag.contains(TAG_COLOR)) {
            return tag.getInt(TAG_COLOR);
        }
        return 0;
    }


    @Nullable
    private ResourceLocation getDecoration(CompoundTag tag) {
        if (tag.contains(TAG_DECORATION)) {
            return new ResourceLocation(tag.getString(TAG_DECORATION));
        }
        return null;
    }

    private int getZoomLevel(CompoundTag tag) {
        if (tag.contains(TAG_ZOOM)) {
            return tag.getInt(TAG_ZOOM);
        }
        return 2;
    }

    private boolean getSkipKnown(CompoundTag tag) {
        if (tag.contains(TAG_SKIP_KNOWN)) {
            return tag.getBoolean(TAG_SKIP_KNOWN);
        }
        return true;
    }


    //center pos ==  not done yet. null==failed
    @Nullable
    public InteractionResultHolder<BlockPos> findNearestMapStructure(ServerLevel level, Holder<Structure> holder, int searchRadius, BlockPos center,
                                                                     boolean skipKnownStructures, State state, int maxIterations) {
        if (!level.getServer().getWorldData().worldGenSettings().generateStructures()) return null;

        ServerChunkCache source = level.getChunkSource();
        ChunkGenerator gen = source.getGenerator();

        Map<StructurePlacement, Set<Holder<Structure>>> map = new Object2ObjectArrayMap<>();

        for (StructurePlacement structurePlacement : gen.getPlacementsForStructure(holder, source.randomState())) {
            map.computeIfAbsent(structurePlacement, (ss) -> new ObjectArraySet<>()).add(holder);
        }

        if (map.isEmpty()) return InteractionResultHolder.fail(BlockPos.ZERO);

        double d = Double.MAX_VALUE;
        StructureManager structureManager = level.structureManager();
        List<Pair<RandomSpreadStructurePlacement, Set<Holder<Structure>>>> list = new ArrayList<>(map.size());

        for (Map.Entry<StructurePlacement, Set<Holder<Structure>>> ent : map.entrySet()) {
            StructurePlacement placement = ent.getKey();
            if (placement instanceof ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
                Pair<BlockPos, Holder<Structure>> pair2 = gen.getNearestGeneratedStructure(ent.getValue(), level, structureManager, center, skipKnownStructures, concentricRingsStructurePlacement);
                if (pair2 != null) {
                    BlockPos blockPos = pair2.getFirst();
                    double e = center.distSqr(blockPos);
                    if (e < d) {
                        return InteractionResultHolder.success(pair2.getFirst());
                    }
                }
            } else if (placement instanceof RandomSpreadStructurePlacement rr) {
                list.add(Pair.of(rr, ent.getValue()));
            }
        }

        if (list.isEmpty()) return null;

        int centerX = SectionPos.blockToSectionCoord(center.getX());
        int centerY = SectionPos.blockToSectionCoord(center.getZ());

        long seed = level.getSeed();

        //state variables

        int currentIter = 0;

        for (; state.radius <= searchRadius; ++state.radius) {

            //for all placements
            BlockPos found = null;
            //save this in state
            for (; state.placementInd < list.size(); ++state.placementInd) {
                var pl = list.get(state.placementInd);
                RandomSpreadStructurePlacement placement = pl.getFirst();
                var holderSet = pl.getSecond();

                BlockPos foundPair = null;

                int spacing = placement.spacing();

                //scan the entire ring
                inner:
                for (; state.x <= state.radius; ++state.x) {
                    boolean onEdgeX = state.x == -state.radius || state.x == state.radius;

                    for (; state.z <= state.radius; ++state.z) {
                        boolean onEdgeY = state.z == -state.radius || state.z == state.radius;
                        if (onEdgeX || onEdgeY) {
                            currentIter++;
                            int testX = centerX + spacing * state.x;
                            int testY = centerY + spacing * state.z;
                            ChunkPos chunkPos = placement.getPotentialStructureChunk(seed, testX, testY);
                            //var s = Stopwatch.createStarted();
                            var pair = getStructureGeneratingAt(holderSet, source, structureManager,
                                    skipKnownStructures, placement, chunkPos, state);
                            //Supplementaries.LOGGER.warn("GenAT: "+s.elapsed());
                            if (pair != null) {
                                Optional<BlockPos> left = pair.left();
                                if (left.isPresent()) {
                                    foundPair = left.get();
                                    state.z = -state.radius;
                                    break inner; //found structure for this placement at this lastRadius. might not be the closest one tho
                                } else {
                                    //we are waiting here
                                    return InteractionResultHolder.pass(BlockPos.ZERO);
                                }
                            }
                            if (currentIter > maxIterations) {
                                return InteractionResultHolder.pass(BlockPos.ZERO);
                            }
                        }
                    }
                    state.z = -state.radius;
                }
                state.x = -state.radius;


                if (foundPair != null) {
                    double f = center.distSqr(foundPair);
                    if (f < d) {
                        d = f;
                        found = foundPair;
                    }
                }
            }
            state.placementInd = 0;
            if (found != null) {
                return InteractionResultHolder.success(found);
            }
        }
        return InteractionResultHolder.fail(BlockPos.ZERO);
    }

    public static Either<BlockPos, ChunkPos> getStructureGeneratingAt(
            Set<Holder<Structure>> structureHoldersSet, ServerChunkCache chunkCache, StructureManager structureManager,
            boolean skipKnownStructures, StructurePlacement placement, ChunkPos chunkPos, State state) {
        var s2 = Stopwatch.createStarted();

        for (Holder<Structure> holder : structureHoldersSet) {
            Structure structure = holder.value();

            //TODO: this operation here is still pretty expensive
            StructureCheckResult structureCheckResult = structureManager.checkStructurePresence(chunkPos, structure, skipKnownStructures);
            if (structureCheckResult == StructureCheckResult.START_NOT_PRESENT) {
                continue;
            }

            if (!skipKnownStructures && structureCheckResult == StructureCheckResult.START_PRESENT) {
                return Either.left(placement.getLocatePos(chunkPos));
            }
            //get cached one
            boolean shouldMultiThread = Thread.currentThread() == mainThread;
            ChunkAccess chunkAccess = chunkCache.getChunk(chunkPos.x, chunkPos.z,
                    ChunkStatus.STRUCTURE_STARTS, !shouldMultiThread);

            if (chunkAccess == null && shouldMultiThread) {
                if (state.waiting) {
                    if (!COMPUTING_CHUNKPOS.contains(chunkPos)) {
                        state.waiting = false;
                    }
                }

                if (!state.waiting) {
                    EXECUTORS.submit(() -> {
                        COMPUTING_CHUNKPOS.add(chunkPos);
                        //this is where all the expensiveness of this comes from
                        chunkCache.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS, true);
                        COMPUTING_CHUNKPOS.remove(chunkPos);
                    });

                    state.waiting = true;
                } else {
                    //it usually never goes here as by the time this runs again the thread has done
                }
                //resets to old pos hack
                if (state.z == -state.radius) {
                    state.z += 1;
                    if (state.x == -state.radius) {
                        state.x += 1;
                        state.radius -= 1;
                    } else state.x -= 1;
                } else {
                    state.z -= 1;
                }
                Supplementaries.LOGGER.warn("E " + s2.elapsed());

                return Either.right(chunkPos);
            } else {
                state.waiting = false;
            }

            StructureStart structureStart = structureManager.getStartForStructure(SectionPos.bottomOf(chunkAccess), structure, chunkAccess);
            if (structureStart != null && structureStart.isValid()) {
                if (!skipKnownStructures || tryAddReference(structureManager, structureStart)) {
                    return Either.left(placement.getLocatePos(structureStart.getChunkPos()));
                }
            }
        }
        return null;
    }


    private static boolean tryAddReference(StructureManager structureManager, StructureStart structureStrart) {
        if (structureStrart.canBeReferenced()) {
            structureManager.addReference(structureStrart);
            return true;
        } else {
            return false;
        }
    }

    public static ItemStack forStructure(ServerLevel level, @Nullable TagKey<Structure> tag, int searchRadius,
                                         boolean skipKnown, int zoom, @Nullable MapDecoration.Type deco,
                                         @Nullable String name, int color) {
        ItemStack stack = forStructure(level, tag);
        var t = stack.getOrCreateTag();
        t.putInt(TAG_SEARCH_RADIUS, searchRadius);
        t.putBoolean(TAG_SKIP_KNOWN, skipKnown);
        t.putInt(TAG_ZOOM, zoom);
        if(deco != null) {
            t.putString(TAG_DECORATION, deco.toString().toLowerCase(Locale.ROOT));
        }
        if (name != null) {
            t.putString(TAG_NAME, name);
        }
        if (color != 0) {
            t.putInt(TAG_COLOR, color);
        }
        return stack;
    }

    public static int getItemColor(ItemStack stack, int layer){
        if(layer == 0)return -1;
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag != null && compoundTag.contains(TAG_COLOR)) {
            int i = compoundTag.getInt(TAG_COLOR);
            return -16777216 | i & 16777215;
        } else {
            return 0;
        }
    }

    public static ItemStack forStructure(ServerLevel level, @Nullable TagKey<Structure> tag) {
        ItemStack stack = QuarkCompatImpl.CARTOGRAPHERS_QUILL.get().getDefaultInstance();
        if(tag != null) {
            //adventurer ones are always random
            String target = selectRandomTarget(level, tag);
            if (target == null) return ItemStack.EMPTY;
            stack.getOrCreateTag().putString(CartographersQuillItem.TAG_STRUCTURE, target);
        }
        return stack;
    }

    @Nullable
    private static String selectRandomTarget(ServerLevel level, TagKey<Structure> tag) {
        Optional<HolderSet.Named<Structure>> taggedStructures = level.registryAccess()
                .registryOrThrow(Registry.STRUCTURE_REGISTRY).getTag(tag);
        if (taggedStructures.isPresent()) {

            List<Holder<Structure>> reachable = new ArrayList<>();
            ServerChunkCache source = level.getChunkSource();
            ChunkGenerator chunkGenerator = source.getGenerator();
            for (var s : taggedStructures.get()) {
                if (!chunkGenerator.getPlacementsForStructure(s, source.randomState()).isEmpty()) {
                    reachable.add(s);
                }
            }
            if (!reachable.isEmpty()) {
                Holder<Structure> selected = reachable.get(level.random.nextInt(reachable.size()));
                return selected.unwrapKey().get().location().toString();
            }
        }
        return null;

    }

    //in the end we dont even need this. iterating over those 3 looks is not what slows this down
    private static final class State {
        private boolean waiting;
        private int radius;
        private int x;
        private int z;
        private int placementInd;

        private State(int lastRadius, int lastX, int lastZ, int index, boolean waiting) {
            this.radius = lastRadius;
            this.x = lastX;
            this.z = lastZ;
            this.placementInd = index;
            this.waiting = waiting;
        }

        public void save(CompoundTag tag) {
            tag.putInt(TAG_RADIUS, radius);
            tag.putInt(TAG_POS_X, x);
            tag.putInt(TAG_POS_Z, z);
            tag.putInt(TAG_POS_INDEX, placementInd);
            tag.putBoolean(TAG_WAITING, waiting);
        }

        @Nullable
        private static State get(CompoundTag tag) {
            int radius = 0;
            if (tag.contains(TAG_RADIUS)) {
                radius = tag.getInt(TAG_RADIUS);
            }
            int x = 0;
            if (tag.contains(TAG_POS_X)) {
                x = tag.getInt(TAG_POS_X);
            }
            int z = 0;
            if (tag.contains(TAG_POS_Z)) {
                z = tag.getInt(TAG_POS_Z);
            }
            int index = 0;
            if (tag.contains(TAG_POS_INDEX)) {
                index = tag.getInt(TAG_POS_INDEX);
            }
            boolean waiting = false;
            if (tag.contains(TAG_WAITING)) {
                waiting = tag.getBoolean(TAG_WAITING);
            }
            if (x > radius || z > radius) return null;
            return new State(radius, x, z, index, waiting);
        }
    }

    private record Key(GlobalPos pos, ResourceLocation structure, int radius, boolean bool) {
    }

    private static final Map<Key, InteractionResultHolder<BlockPos>> RESULTS = new ConcurrentHashMap<>();
    private static final Set<Key> COMPUTING = ConcurrentHashMap.newKeySet();
    private static final Set<ChunkPos> COMPUTING_CHUNKPOS = ConcurrentHashMap.newKeySet();


}
