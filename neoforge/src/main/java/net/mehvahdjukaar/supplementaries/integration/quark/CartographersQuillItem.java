package net.mehvahdjukaar.supplementaries.integration.quark;

import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.common.worldgen.LocatedStructure;
import net.mehvahdjukaar.supplementaries.common.worldgen.StructureLocator;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.platform.QuarkCompatImpl;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.content.tools.item.PathfindersQuillItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CartographersQuillItem extends PathfindersQuillItem {

    private static final Map<Key, InteractionResultHolder<BlockPos>> RESULTS = new ConcurrentHashMap<>();
    private static final Set<Key> COMPUTING = ConcurrentHashMap.newKeySet();

    public CartographersQuillItem() {
        // Pass null module so Zeta's auto registration is skipped. We register through RegUtils.
        super(null, new Item.Properties().stacksTo(1));
    }

    @Override
    public List<ItemStack> appendItemsToCreativeTab(RegistryAccess access) {
        // Don't inherit Quark's per-biome list - we only have one item.
        return List.of(new ItemStack(this));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flags) {
        ResourceLocation structure = getTarget(stack);
        if (structure == null) {
            components.add(Component.translatable("message.supplementaries.cartographers_quill").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (Optional.ofNullable(stack.get(QuarkDataComponents.IS_SEARCHING)).orElse(false)) {
            components.add(PathfindersQuillItem.getSearchingComponent().withStyle(ChatFormatting.BLUE));
        }
        components.add(Component.translatableWithFallback(
                        "structure." + structure.getNamespace() + "." + structure.getPath(),
                        structure.toString())
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected String getFailMessage() {
        return "message.supplementaries.quill_failed";
    }

    @Override
    protected String getSuccessMessage() {
        return "message.supplementaries.quill_finished";
    }

    @Override
    protected String getRetryMessage() {
        return "message.supplementaries.quill_retry";
    }

    @Override
    protected int getIterations() {
        // unused since we force multithreading, but provide a sane value for the fallback path
        return 32;
    }

    @Override
    protected boolean isMultiThreaded() {
        // The structure search is expensive; always run off-thread to avoid the lag spikes
        // that motivated this feature in the first place.
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && stack.get(ModComponents.QUILL_STRUCTURE.get()) == null) {
            ResourceLocation random = selectRandomTarget(serverLevel, ModTags.ADVENTURE_MAP_DESTINATIONS);
            if (random != null) stack.set(ModComponents.QUILL_STRUCTURE.get(), random);
        }
        return super.use(level, player, hand);
    }

    @Nullable
    @Override
    public ResourceLocation getTarget(ItemStack stack) {
        return stack.get(ModComponents.QUILL_STRUCTURE.get());
    }

    @Nullable
    private static Holder<Structure> getStructureHolder(ServerLevel level, ResourceLocation key) {
        Registry<Structure> reg = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return reg.getHolder(ResourceKey.create(reg.key(), key)).orElse(null);
    }

    @Override
    public ItemStack createMap(ServerLevel level, BlockPos targetPos, ResourceLocation structure, ItemStack original) {
        Holder<Structure> holder = getStructureHolder(level, structure);
        if (holder == null) return ItemStack.EMPTY;

        ItemStack map = AdventurerMapsHandler.createStructureMap(level, targetPos, holder,
                getZoom(original), getDecoration(original), getMapName(original), getColor(original));

        var customName = original.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        if (customName != null) {
            map.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, customName);
        }
        return map;
    }

    @Override
    protected InteractionResultHolder<BlockPos> searchConcurrent(ResourceLocation target, ItemStack stack,
                                                                 ServerLevel level, Player player) {
        BlockPos center = getStartPos(stack, player);
        int radius = getSearchRadius(stack);
        boolean skipKnown = getSkipKnown(stack);

        Key key = new Key(GlobalPos.of(level.dimension(), center), target, radius, skipKnown);

        if (COMPUTING.contains(key)) {
            return InteractionResultHolder.pass(BlockPos.ZERO);
        }
        InteractionResultHolder<BlockPos> cached = RESULTS.remove(key);
        if (cached != null) return cached;

        COMPUTING.add(key);
        EXECUTORS.submit(() -> {
            try {
                RESULTS.put(key, doSearch(target, level, center, radius, skipKnown));
            } finally {
                COMPUTING.remove(key);
            }
        });
        return InteractionResultHolder.pass(BlockPos.ZERO);
    }

    @Override
    protected InteractionResultHolder<BlockPos> searchIterative(ResourceLocation target, ItemStack stack,
                                                                ServerLevel level, Player player, int maxIter) {
        // Synchronous fallback; not used when isMultiThreaded() == true but keep it sane.
        return doSearch(target, level, getStartPos(stack, player), getSearchRadius(stack), getSkipKnown(stack));
    }

    private static InteractionResultHolder<BlockPos> doSearch(ResourceLocation target, ServerLevel level,
                                                              BlockPos center, int radius, boolean skipKnown) {
        Holder<Structure> holder = getStructureHolder(level, target);
        if (holder == null) return InteractionResultHolder.fail(BlockPos.ZERO);

        int maxSearches = CommonConfigs.Tweaks.RANDOM_ADVENTURER_MAPS_MAX_SEARCHES.get();
        LocatedStructure found = StructureLocator.findNearestStructure(level,
                HolderSet.direct(holder), center, radius, skipKnown, maxSearches, false);
        if (found == null) return InteractionResultHolder.fail(BlockPos.ZERO);
        return InteractionResultHolder.success(found.position());
    }

    private static BlockPos getStartPos(ItemStack stack, Player player) {
        Integer sx = stack.get(QuarkDataComponents.TAG_SOURCE_X);
        Integer sz = stack.get(QuarkDataComponents.TAG_SOURCE_Z);
        if (sx != null && sz != null) return new BlockPos(sx, 64, sz);
        return player.blockPosition();
    }

    private static int getSearchRadius(ItemStack stack) {
        Integer r = stack.get(ModComponents.QUILL_SEARCH_RADIUS.get());
        if (r != null) return r;
        return CommonConfigs.Tweaks.RANDOM_ADVENTURER_MAX_SEARCH_RADIUS.get();
    }

    private static boolean getSkipKnown(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.QUILL_SKIP_KNOWN.get())).orElse(true);
    }

    private static int getZoom(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.QUILL_ZOOM.get())).orElse(2);
    }

    @Nullable
    private static ResourceLocation getDecoration(ItemStack stack) {
        return stack.get(ModComponents.QUILL_DECORATION.get());
    }

    @Nullable
    private static String getMapName(ItemStack stack) {
        return stack.get(ModComponents.QUILL_MAP_NAME.get());
    }

    private static int getColor(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.QUILL_COLOR.get())).orElse(0);
    }

    public static ItemStack forStructure(ServerLevel level, @Nullable HolderSet<Structure> targets,
                                         int searchRadius, boolean skipKnown, int zoom,
                                         @Nullable ResourceLocation decoration,
                                         @Nullable String name, int color) {
        ItemStack stack = QuarkCompatImpl.CARTOGRAPHERS_QUILL.get().getDefaultInstance();
        if (targets != null) {
            ResourceLocation target = selectRandomTarget(level, targets);
            if (target == null) return ItemStack.EMPTY;
            stack.set(ModComponents.QUILL_STRUCTURE.get(), target);
        }
        stack.set(ModComponents.QUILL_SEARCH_RADIUS.get(), Math.max(1, searchRadius));
        stack.set(ModComponents.QUILL_SKIP_KNOWN.get(), skipKnown);
        stack.set(ModComponents.QUILL_ZOOM.get(), zoom);
        if (decoration != null) {
            stack.set(ModComponents.QUILL_DECORATION.get(), decoration);
        }
        if (name != null) {
            stack.set(ModComponents.QUILL_MAP_NAME.get(), name);
        }
        if (color != 0) {
            stack.set(ModComponents.QUILL_COLOR.get(), color);
        }
        return stack;
    }

    public static int getItemColor(ItemStack stack, int layer) {
        if (layer == 0) return -1;
        Integer c = stack.get(ModComponents.QUILL_COLOR.get());
        if (c == null) return 0;
        return 0xFF000000 | (c & 0xFFFFFF);
    }

    @Nullable
    private static ResourceLocation selectRandomTarget(ServerLevel level, TagKey<Structure> tag) {
        var targets = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(tag);
        return targets.map(holders -> selectRandomTarget(level, holders)).orElse(null);
    }

    @Nullable
    private static ResourceLocation selectRandomTarget(ServerLevel level, HolderSet<Structure> taggedStructures) {
        var generatorState = level.getChunkSource().getGeneratorState();
        List<Holder<Structure>> reachable = new ArrayList<>();
        for (var s : taggedStructures) {
            if (!generatorState.getPlacementsForStructure(s).isEmpty()) {
                reachable.add(s);
            }
        }
        if (reachable.isEmpty()) return null;
        Holder<Structure> selected = reachable.get(level.random.nextInt(reachable.size()));
        return selected.unwrapKey().map(ResourceKey::location).orElse(null);
    }

    private record Key(GlobalPos pos, ResourceLocation structure, int radius, boolean skipKnown) {
    }
}
