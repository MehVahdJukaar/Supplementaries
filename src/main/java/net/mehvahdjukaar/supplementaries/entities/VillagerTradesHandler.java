package net.mehvahdjukaar.supplementaries.entities;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.village.WandererTradesEvent;

import java.util.List;
import java.util.Random;

public class VillagerTradesHandler {

    private static final float BUY = 0.05f;
    private static final float SELL = 0.2f;

    public static final VillagerTrades.ITrade[] TRADES = new VillagerTrades.ITrade[]{

            itemForEmeraldTrade(Registry.ROPE_ITEM.get(), 2, 1, 10),
            itemForEmeraldTrade(Items.GUNPOWDER, 1, 1, 8),
            itemForEmeraldTrade(Registry.COPPER_LANTERN.get(), 1, 2, 12),
            itemForEmeraldTrade(Registry.BOMB_ITEM.get(), 1, 4, 8),
            new StarForEmeraldTrade(4, 8),
            itemForEmeraldTrade(Items.TNT, 1, 6, 8),
            new RocketForEmeraldTrade(6, 1, 3, 8),
            itemForEmeraldTrade(Registry.ROPE_ARROW_ITEM.get(), 1, 8, 6),
            itemForEmeraldTrade(Registry.BOMB_BLUE_ITEM.get(), 1, Registry.BOMB_ITEM.get(), 1, 40, 3),
    };

    static BasicTrade itemForEmeraldTrade(IItemProvider item, int quantity, int price, int maxTrades) {
        return new BasicTrade(new ItemStack(Items.EMERALD, price), new ItemStack(item, quantity), maxTrades, 1, BUY);
    }

    static BasicTrade itemForEmeraldTrade(IItemProvider item, int quantity, IItemProvider additional, int addQuantity, int price, int maxTrades) {
        return new BasicTrade(new ItemStack(Items.EMERALD, price), new ItemStack(additional, addQuantity), new ItemStack(item, quantity), maxTrades, 1, BUY);
    }

    static class RocketForEmeraldTrade implements VillagerTrades.ITrade {
        private final int maxTrades;
        private final int price;
        private final int paper;
        private final int rockets;

        public RocketForEmeraldTrade(int price, int paper, int rockets, int maxTrades) {
            this.price = price;
            this.maxTrades = maxTrades;
            this.paper = paper;
            this.rockets = rockets;
        }

        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, rockets);
            CompoundNBT compoundnbt = itemstack.getOrCreateTagElement("Fireworks");
            ListNBT listnbt = new ListNBT();

            int stars = 0;
            do {
                listnbt.add(createRandomFireworkStar(random));
                stars++;
            } while (random.nextFloat() < 0.42f && stars < 7);

            compoundnbt.putByte("Flight", (byte) (random.nextInt(3) + 1));
            compoundnbt.put("Explosions", listnbt);

            return new MerchantOffer(new ItemStack(Items.EMERALD, price), new ItemStack(Items.PAPER, paper),
                    itemstack, maxTrades, 1, BUY);
        }


    }

    static class StarForEmeraldTrade implements VillagerTrades.ITrade {
        private final int maxTrades;
        private final int price;

        public StarForEmeraldTrade(int price, int maxTrades) {
            this.price = price;
            this.maxTrades = maxTrades;
        }

        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_STAR);
            itemstack.addTagElement("Explosion", createRandomFireworkStar(random));
            return new MerchantOffer(new ItemStack(Items.EMERALD, price), itemstack, maxTrades, 1, BUY);
        }


    }


    public static CompoundNBT createRandomFireworkStar(Random random) {
        CompoundNBT tag = new CompoundNBT();
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
            for (int i = 0; i < ServerConfigs.cached.GLOBE_TRADES; i++) {
                event.getRareTrades().add(itemForEmeraldTrade(Registry.GLOBE_ITEM.get(),1, 10, 3));
            }
        }
        if (RegistryConfigs.reg.FLAX_ENABLED.get()) {
            for (int i = 0; i < 3; i++) {
                event.getGenericTrades().add(itemForEmeraldTrade(Registry.FLAX_SEEDS_ITEM.get(), 1, 6, 8));
            }
        }
    }
}
