package net.mehvahdjukaar.supplementaries.integration.forge.quark;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.integration.forge.QuarkCompatImpl;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.mehvahdjukaar.supplementaries.reg.RegUtils;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
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
import org.jetbrains.annotations.Nullable;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.tools.item.PathfindersQuillItem;
import vazkii.quark.content.tools.module.PathfinderMapsModule;

import java.util.*;
import java.util.concurrent.*;

public class AdventurersQuillItem extends PathfindersQuillItem {

    public static final String TAG_STRUCTURE = "targetStructure";
    protected static final String TAG_RADIUS = "searchRadius";
    protected static final String TAG_POS_INDEX = "searchIndex";
    protected static final String TAG_WAITING = "waiting";


    public AdventurersQuillItem() {
        super(ModuleLoader.INSTANCE.getModuleInstance(PathfinderMapsModule.class),
                new Properties().tab(RegUtils.getTab(CreativeModeTab.TAB_TOOLS, "adventurer_map")));

        QuarkCompatImpl.removeStuffFromARLHack();
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowedIn(category)) {
            items.add(new ItemStack(this));
        }
    }

    //leave 1, we use the one below
    @Override
    protected int getIterations() {
        return 1;
    }

    private int getMaxIterations() {
        return 1000;//PathfinderMapsModule.pathfindersQuillSpeed;
    }

    @Override
    protected boolean isNBTValid(ItemStack stack) {
        return true;
    }

    private int getSearchRadius() {
        return AdventurerMapsHandler.SEARCH_RADIUS;
    }

    @Override
    protected ItemStack search(ItemStack stack, ServerLevel level, Player player, int slot) {

        CompoundTag tag = stack.getTag();
        if (tag == null) return ItemStack.EMPTY;

        Holder<Structure> structure = getTargetStructure(tag, level);
        State state = State.get(tag);

        if (structure == null || state == null) return ItemStack.EMPTY;
        BlockPos center = getOrCreateStartPos(tag, player);
        int radius = getSearchRadius();
        int iter = getMaxIterations();
        var s = Stopwatch.createStarted();
        BlockPos pos = findNearestMapStructure(level, structure, radius, center, true, state, iter);
        Supplementaries.LOGGER.warn(state.radius + ":----" + s.elapsed());
        state.save(tag);
        if (pos == null) {
            return ItemStack.EMPTY;
        } else if (pos == center) {
            return stack;
        } else
            return AdventurerMapsHandler.createAdventurerMap(level, pos, structure);
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

    @Nullable
    private Holder<Structure> getTargetStructure(CompoundTag tag, ServerLevel level) {

        String str = tag.getString(TAG_STRUCTURE);
        if (str.isEmpty()) {
            //re-generate for empty one
            str = "igloo";// computeTarget(level, ModTags.ADVENTURE_MAP_DESTINATIONS);
            if (str == null) return null;
            tag.putString(TAG_STRUCTURE, str);
        }

        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
        var structure = reg.getHolder(ResourceKey.create(reg.key(), new ResourceLocation(str)));

        return structure.orElse(null);
    }

    //center pos ==  not done yet. null==failed
    @Nullable
    public BlockPos findNearestMapStructure(ServerLevel level, Holder<Structure> holder, int searchRadius, BlockPos center,
                                            boolean skipKnownStructures, State state, int maxIterations) {
        if (!level.getServer().getWorldData().worldGenSettings().generateStructures()) return null;

        ServerChunkCache source = level.getChunkSource();
        ChunkGenerator gen = source.getGenerator();

        Map<StructurePlacement, Set<Holder<Structure>>> map = new Object2ObjectArrayMap<>();

        for (StructurePlacement structurePlacement : gen.getPlacementsForStructure(holder, source.randomState())) {
            map.computeIfAbsent(structurePlacement, (ss) -> new ObjectArraySet<>()).add(holder);
        }

        if (map.isEmpty()) return null;

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
                        return pair2.getFirst();
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

        outer:
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
                            var s = Stopwatch.createStarted();
                            var pair = getStructureGeneratingAt(holderSet, source, structureManager,
                                    skipKnownStructures, placement, chunkPos, state);
                            Supplementaries.LOGGER.warn("get: " + s.elapsed());
                            if (pair != null) {
                                Optional<BlockPos> left = pair.left();
                                if (left.isPresent()) {
                                    foundPair = left.get();
                                    state.z = -state.radius;
                                    break inner; //found structure for this placement at this lastRadius. might not be the closest one tho
                                } else {
                                    return center;
                                }
                            }
                            if (currentIter > maxIterations) {
                                return center;
                            } else {
                                int aa = 1;
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
                return found;
            }
        }

        return null;
    }

    private static final Set<ChunkPos> BEING_COMPUTED = ConcurrentHashMap.newKeySet();

    //there should only ever be one but more could come with multiple players.
    // using 2 for edge cases where one thread might not be done but result will be ignored
    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    public static Either<BlockPos, ChunkPos> getStructureGeneratingAt(
            Set<Holder<Structure>> structureHoldersSet, ServerChunkCache chunkCache, StructureManager structureManager,
            boolean skipKnownStructures, StructurePlacement placement, ChunkPos chunkPos, State state) {

        for (Holder<Structure> holder : structureHoldersSet) {
            StructureCheckResult structureCheckResult = structureManager.checkStructurePresence(chunkPos, holder.value(), skipKnownStructures);
            if (structureCheckResult == StructureCheckResult.START_NOT_PRESENT) {
                continue;
            }
            if (!skipKnownStructures && structureCheckResult == StructureCheckResult.START_PRESENT) {
                return Either.left(placement.getLocatePos(chunkPos));
            }
            //get cached one
            ChunkAccess chunkAccess = chunkCache.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS, false);

            if (chunkAccess == null) {
                if (state.waiting) {
                    if (!BEING_COMPUTED.contains(chunkPos)) {
                        state.waiting = false;
                    }
                }
                if (!state.waiting) {
                    BEING_COMPUTED.add(chunkPos);
                    EXECUTORS.submit(() -> {
                        //this is where all the expensiveness of this comes from
                        chunkCache.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS, true);
                        BEING_COMPUTED.remove(chunkPos);
                    });
                    state.waiting = true;
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
                return Either.right(chunkPos);
            } else {
                state.waiting = false;
            }

            StructureStart structureStart = structureManager.getStartForStructure(SectionPos.bottomOf(chunkAccess), holder.value(), chunkAccess);
            if (structureStart != null && structureStart.isValid()) {
                if (skipKnownStructures || tryAddReference(structureManager, structureStart)) {
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

    public static ItemStack forStructure(ServerLevel level, TagKey<Structure> tag) {
        ItemStack stack = QuarkCompatImpl.ADVENTURER_QUILL.get().getDefaultInstance();
        String target = computeTarget(level, tag);
        if (target == null) return ItemStack.EMPTY;
        stack.getOrCreateTag().putString(AdventurersQuillItem.TAG_STRUCTURE, target);
        return stack;
    }

    @Nullable
    private static String computeTarget(ServerLevel level, TagKey<Structure> tag) {
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
}
