package net.mehvahdjukaar.supplementaries.entities.trades;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VillagerTradesHandler {

    private static final float BUY = 0.05f;
    private static final float SELL = 0.2f;

    public static final VillagerTrades.ItemListing[] TRADES;

    static {
        List<VillagerTrades.ItemListing> trades = new ArrayList<>();

        if (RegistryConfigs.reg.ROPE_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.ROPE_ITEM.get(), 4, 1, 10));
        }
        trades.add(itemForEmeraldTrade(Items.GUNPOWDER, 2, 1, 8));
        if (RegistryConfigs.reg.COPPER_LANTERN_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.COPPER_LANTERN.get(), 1, 1, 12));
        }
        if (RegistryConfigs.reg.BOMB_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.BOMB_ITEM.get(), 1, 3, 8));
        }
        trades.add(new StarForEmeraldTrade(2, 8));
        trades.add(new RocketForEmeraldTrade(3, 1, 3, 8));
        trades.add(itemForEmeraldTrade(Items.TNT, 1, 4, 8));

        if (RegistryConfigs.reg.ROPE_ARROW_ENABLED.get()) {
            Item i = ModRegistry.ROPE_ARROW_ITEM.get();
            ItemStack stack = new ItemStack(i);
            stack.setDamageValue(Math.max(0, stack.getMaxDamage() - 16));
            trades.add(itemForEmeraldTrade(stack, 4, 6));
        }
        if (RegistryConfigs.reg.BOMB_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.BOMB_BLUE_ITEM.get(), 1, ModRegistry.BOMB_ITEM.get(), 1, 40, 3));
        }

        TRADES = trades.toArray(new VillagerTrades.ItemListing[0]);
    }

    static BasicTrade itemForEmeraldTrade(ItemLike item, int quantity, int price, int maxTrades) {
        return itemForEmeraldTrade(new ItemStack(item, quantity), price, maxTrades);
    }

    static BasicTrade itemForEmeraldTrade(ItemStack itemStack, int price, int maxTrades) {
        return new BasicTrade(new ItemStack(Items.EMERALD, price), itemStack, maxTrades, 1, BUY);
    }

    static BasicTrade itemForEmeraldTrade(ItemLike item, int quantity, ItemLike additional, int addQuantity, int price, int maxTrades) {
        return new BasicTrade(new ItemStack(Items.EMERALD, price), new ItemStack(additional, addQuantity), new ItemStack(item, quantity), maxTrades, 1, BUY);
    }

    record RocketForEmeraldTrade(int price, int paper, int rockets,
                                 int maxTrades) implements VillagerTrades.ItemListing {

        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, rockets);
            CompoundTag tag = itemstack.getOrCreateTagElement("Fireworks");
            ListTag listTag = new ListTag();

            int stars = 0;
            do {
                listTag.add(createRandomFireworkStar(random));
                stars++;
            } while (random.nextFloat() < 0.42f && stars < 7);

            tag.putByte("Flight", (byte) (random.nextInt(3) + 1));
            tag.put("Explosions", listTag);

            return new MerchantOffer(new ItemStack(Items.EMERALD, price), new ItemStack(Items.PAPER, paper),
                    itemstack, maxTrades, 1, BUY);
        }


    }

    record StarForEmeraldTrade(int price, int maxTrades) implements VillagerTrades.ItemListing {

        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_STAR);
            itemstack.addTagElement("Explosion", createRandomFireworkStar(random));
            return new MerchantOffer(new ItemStack(Items.EMERALD, price), itemstack, maxTrades, 1, BUY);
        }
    }


    private static CompoundTag createRandomFireworkStar(Random random) {
        CompoundTag tag = new CompoundTag();
        tag.putByte("Type", (byte) FireworkRocketItem.Shape.values()
                [random.nextInt(FireworkRocketItem.Shape.values().length)].getId());
        tag.putBoolean("Flicker", random.nextFloat() < 0.42f);
        tag.putBoolean("Trail", random.nextFloat() < 0.42f);
        List<Integer> list = Lists.newArrayList();
        int colors = 0;
        do {
            list.add(DyeColor.values()[random.nextInt(DyeColor.values().length)].getFireworkColor());
            colors++;
        } while (random.nextFloat() < 0.42f && colors < 9);
        tag.putIntArray("Colors", list);


        if (random.nextBoolean()) {
            List<Integer> fadeList = Lists.newArrayList();
            colors = 0;
            do {
                fadeList.add(DyeColor.values()[random.nextInt(DyeColor.values().length)].getFireworkColor());
                colors++;
            } while (random.nextFloat() < 0.42f && colors < 9);
            tag.putIntArray("FadeColors", fadeList);
        }

        return tag;
    }

    public static void registerWanderingTraderTrades(WandererTradesEvent event) {

        if (RegistryConfigs.reg.GLOBE_ENABLED.get()) {
            //adding twice cause it's showing up too rarely
            for (int i = 0; i < ServerConfigs.cached.GLOBE_TRADES; i++) {
                event.getRareTrades().add(itemForEmeraldTrade(ModRegistry.GLOBE_ITEM.get(), 1, 10, 3));
            }
        }
        if (RegistryConfigs.reg.FLAX_ENABLED.get()) {
            for (int i = 0; i < 2; i++) {
                event.getGenericTrades().add(itemForEmeraldTrade(ModRegistry.FLAX_SEEDS_ITEM.get(), 1, 6, 8));
            }
        }
    }

    public static void registerVillagerTrades(VillagerTradesEvent event){
        if (RegistryConfigs.reg.FLAX_ENABLED.get()) {
            if (event.getType().equals(VillagerProfession.FARMER)) {
                event.getTrades().get(3).add(new BasicTrade(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get(), 15), new ItemStack(net.minecraft.world.item.Items.EMERALD), 16, 2, 0.05f));
            }
        }
        AdventurerMapsHandler.loadCustomTrades();
        AdventurerMapsHandler.addTrades(event);
    }
}
