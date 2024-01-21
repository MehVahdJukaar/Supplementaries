package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.misc.ModItemListing;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModVillagerTrades extends SimpleJsonResourceReloadListener {

    public ModVillagerTrades() {
        super(new Gson(), "red_merchant_trades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {

        List<VillagerTrades.ItemListing> trades = new ArrayList<>();
        for (var e : jsons.entrySet()) {
            var j = e.getValue();
            var id = e.getKey();
            VillagerTrades.ItemListing trade = ModItemListing.CODEC.decode(JsonOps.INSTANCE, j)
                    .getOrThrow(false, errorMsg -> Supplementaries.LOGGER.warn("Failed to parse red merchant trade with id {} - error: {}",
                            id, errorMsg)).getFirst();
            trades.add(trade);
        }
    }

    private static final float BUY = 0.05f;
    private static final float SELL = 0.2f;

    //Don't call too early. Lazily initialized
    private static final Supplier<VillagerTrades.ItemListing[]> RED_MERCHANT_TRADES = Suppliers.memoize(() -> {
                VillagerTrades.ItemListing[] listings = ModVillagerTrades.makeRedMerchantTrades();
                if (MiscUtils.FESTIVITY.isChristmas()) {
                    listings = Arrays.stream(listings).map(WrappedListing::new)
                            .toList().toArray(new VillagerTrades.ItemListing[0]);
                }
                return SuppPlatformStuff.fireRedMerchantTradesEvent(listings);
            }
    );

    private static VillagerTrades.ItemListing[] makeRedMerchantTrades() {
        List<VillagerTrades.ItemListing> trades = new ArrayList<>();

        if (CommonConfigs.Functional.ROPE_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.ROPE.get(), 4, 1, 10));
        }
        trades.add(itemForEmeraldTrade(Items.GUNPOWDER, 2, 1, 8));
        var lantern = CompatObjects.COPPER_LANTERN.get() == null ? Blocks.LANTERN : CompatObjects.COPPER_LANTERN.get();
        trades.add(itemForEmeraldTrade(lantern, 1, 1, 12));

        if (CommonConfigs.Tools.BOMB_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.BOMB_ITEM.get(), 1, 4, 8));
            if (CompatHandler.OREGANIZED) {
                trades.add(itemForEmeraldTrade(ModRegistry.BOMB_SPIKY_ITEM.get(), 1, 4, 8));
            }
        }
        trades.add(new StarForEmeraldTrade(2, 8));
        trades.add(new RocketForEmeraldTrade(3, 1, 3, 8));
        trades.add(itemForEmeraldTrade(Items.TNT, 1, 4, 8));

        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
            Item i = ModRegistry.ROPE_ARROW_ITEM.get();
            ItemStack stack = new ItemStack(i);
            stack.setDamageValue(Math.max(0, stack.getMaxDamage() - 16));
            trades.add(itemForEmeraldTrade(stack, 4, 6));
        }
        if (CommonConfigs.Tools.BOMB_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.BOMB_BLUE_ITEM.get(), 1, ModRegistry.BOMB_ITEM.get(), 1, 40, 3));

        }
        return trades.toArray(new VillagerTrades.ItemListing[0]);
    }


    public static VillagerTrades.ItemListing[] getRedMerchantTrades() {
        return RED_MERCHANT_TRADES.get();
    }


    private record WrappedListing(VillagerTrades.ItemListing original) implements VillagerTrades.ItemListing {

        private static final PresentBlockTile DUMMY = new PresentBlockTile(BlockPos.ZERO,
                ModRegistry.PRESENTS.get(null).get().defaultBlockState());

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            MerchantOffer internal = original.getOffer(entity, random);
            if (internal == null) return null;
            DUMMY.setItem(0, internal.getResult());
            DUMMY.setSender(entity.getName().getString());
            DUMMY.setPublic();
            ItemStack stack = DUMMY.getPresentItem(ModRegistry.PRESENTS.get(DyeColor.values()[
                    random.nextInt(DyeColor.values().length)]).get());

            return new MerchantOffer(internal.getBaseCostA(), internal.getCostB(), stack, internal.getUses(),
                    internal.getMaxUses(), internal.getXp(), internal.getPriceMultiplier(), internal.getDemand());
        }
    }


    private static ModItemListing itemForEmeraldTrade(ItemLike item, int quantity, int price, int maxTrades) {
        return itemForEmeraldTrade(new ItemStack(item, quantity), price, maxTrades);
    }

    private static ModItemListing itemForEmeraldTrade(ItemStack itemStack, int price, int maxTrades) {
        return new ModItemListing(new ItemStack(Items.EMERALD, price), itemStack, maxTrades, 1, BUY);
    }

    private static ModItemListing itemForEmeraldTrade(ItemLike item, int quantity, ItemLike additional, int addQuantity, int price, int maxTrades) {
        return new ModItemListing(new ItemStack(Items.EMERALD, price), new ItemStack(additional, addQuantity), new ItemStack(item, quantity), maxTrades, 1, BUY);
    }


    private record RocketForEmeraldTrade(int price, int paper, int rockets,
                                         int maxTrades) implements VillagerTrades.ItemListing {

        @Override
        public MerchantOffer getOffer(Entity entity, RandomSource random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, rockets);
            CompoundTag tag = itemstack.getOrCreateTagElement("Fireworks");
            ListTag listTag = new ListTag();

            int stars = 0;
            List<FireworkRocketItem.Shape> usedShapes = new ArrayList<>();
            do {
                listTag.add(createRandomFireworkStar(random, usedShapes));
                stars++;
            } while (random.nextFloat() < 0.42f && stars < 7);

            tag.putByte("Flight", (byte) (random.nextInt(3) + 1));
            tag.put("Explosions", listTag);

            return new MerchantOffer(new ItemStack(Items.EMERALD, price), new ItemStack(Items.PAPER, paper),
                    itemstack, maxTrades, 1, BUY);
        }
    }

    private record StarForEmeraldTrade(int price, int maxTrades) implements VillagerTrades.ItemListing {

        public MerchantOffer getOffer(Entity entity, RandomSource random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_STAR);
            itemstack.addTagElement("Explosion", createRandomFireworkStar(random, List.of()));
            return new MerchantOffer(new ItemStack(Items.EMERALD, price), itemstack, maxTrades, 1, BUY);
        }
    }

    private static final DyeColor[] VIBRANT_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE, DyeColor.GREEN, DyeColor.RED};

    private static CompoundTag createRandomFireworkStar(RandomSource random, List<FireworkRocketItem.Shape> usedShapes) {
        CompoundTag tag = new CompoundTag();
        ArrayList<FireworkRocketItem.Shape> possible = new ArrayList<>(List.of(FireworkRocketItem.Shape.values()));
        possible.removeAll(usedShapes);
        if (possible.isEmpty()) {
            tag.putByte("Type", (byte) FireworkRocketItem.Shape.values()
                    [random.nextInt(FireworkRocketItem.Shape.values().length)].getId());
        } else {
            tag.putByte("Type", (byte) possible.get(random.nextInt(possible.size())).getId());
        }
        tag.putBoolean("Flicker", random.nextFloat() < 0.42f);
        tag.putBoolean("Trail", random.nextFloat() < 0.42f);
        IntList list = new IntArrayList();
        int colors = 0;
        do {
            list.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
            colors++;
        } while (random.nextFloat() < 0.42f && colors < 9);
        tag.putIntArray("Colors", list);


        if (random.nextBoolean()) {
            IntList fadeList = new IntArrayList();
            colors = 0;
            do {
                fadeList.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
                colors++;
            } while (random.nextFloat() < 0.42f && colors < 9);
            tag.putIntArray("FadeColors", fadeList);
        }

        return tag;
    }


    //runs on init since we need to be early enough to register stuff to forge busses
    public static void init() {

        RegHelper.registerWanderingTraderTrades(2, listings -> {
            if (!CommonConfigs.SPEC.isLoaded()) {
                throw new AssertionError("Common config was not loaded. How is this possible");
            }
            if (CommonConfigs.Building.GLOBE_ENABLED.get()) {
                //adding twice because it's showing up too rarely
                for (int i = 0; i < CommonConfigs.Building.GLOBE_TRADES.get(); i++) {
                    listings.add(itemForEmeraldTrade(ModRegistry.GLOBE_ITEM.get(), 1, 10, 3));
                }
            }
        });
        RegHelper.registerWanderingTraderTrades(1, listings -> {
            if (CommonConfigs.Functional.FLAX_ENABLED.get()) {
                for (int i = 0; i < CommonConfigs.Functional.FLAX_TRADES_WANDERING.get(); i++) {
                    listings.add(itemForEmeraldTrade(ModRegistry.FLAX_SEEDS_ITEM.get(), 1, 6, 8));
                }
            }
        });
        RegHelper.registerVillagerTrades(VillagerProfession.FARMER, 3, itemListings -> {
            if (!CommonConfigs.SPEC.isLoaded()) {
                throw new AssertionError("Common config was not loaded. How is this possible");
            }
            if (CommonConfigs.Functional.FLAX_ENABLED.get())
                for (int i = 0; i < CommonConfigs.Functional.FLAX_TRADES_WANDERING.get(); i++) {
                    itemListings.add(new ModItemListing(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get(), 15), new ItemStack(Items.EMERALD), 16, 2, 0.05f));
                }
        });

        RegHelper.registerVillagerTrades(VillagerProfession.MASON, 1, itemListings -> {
            if (!CommonConfigs.SPEC.isLoaded()) {
                throw new AssertionError("Common config was not loaded. How is this possible");
            }
            if (CommonConfigs.Building.ASH_BRICKS_ENABLED.get() && CommonConfigs.Building.ASH_BRICK_TRADES.get())
                itemListings.add(new ModItemListing(new ItemStack(Items.EMERALD), new ItemStack(ModRegistry.ASH_BRICK_ITEM.get(), 10), 16, 1, 0.05f));
        });

        RegHelper.registerVillagerTrades(VillagerProfession.CARTOGRAPHER, 5, itemListings -> {
            if (!CommonConfigs.SPEC.isLoaded()) {
                throw new AssertionError("Common config was not loaded. How is this possible");
            }
            if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get() && CommonConfigs.Tools.ANTIQUE_INK_TRADES.get())
                itemListings.add(new ModItemListing(new ItemStack(Items.EMERALD, 8),
                        new ItemStack(ModRegistry.ANTIQUE_INK.get()), 16, 30, 0.05f));
        });

        AdventurerMapsHandler.addTradesCallback();
    }
}
