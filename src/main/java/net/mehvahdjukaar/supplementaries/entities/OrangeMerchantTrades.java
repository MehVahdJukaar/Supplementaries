package net.mehvahdjukaar.supplementaries.entities;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.BasicTrade;

import java.util.List;
import java.util.Random;

public class OrangeMerchantTrades {
    public static final VillagerTrades.ITrade[] TRADES =  new VillagerTrades.ITrade[]{
            new RocketForEmeraldTrade(2),
            new StarForEmeraldTrade(2),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Registry.COPPER_LANTERN.get(), 1),  16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Registry.ROPE_ITEM.get()), 16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Registry.ROPE_ARROW_ITEM.get()), 16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.GUNPOWDER), 16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.TNT), 16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 8), new ItemStack(Registry.BOMB_ITEM.get()), 16, 2, 0.05f),
            new BasicTrade(new ItemStack(Items.EMERALD, 40), new ItemStack(Registry.BOMB_BLUE_ITEM.get()), 16, 2, 0.05f)


    };

    static class RocketForEmeraldTrade implements VillagerTrades.ITrade {
        private final int villagerXp;

        public RocketForEmeraldTrade(int xp) {
            this.villagerXp = xp;
        }

        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, 5);
            CompoundNBT compoundnbt = itemstack.getOrCreateTagElement("Fireworks");
            ListNBT listnbt = new ListNBT();

            int stars = 0;
            do{
                listnbt.add(createRandomFireworkStar(random));
                stars++;
            }while(random.nextFloat()<0.4f && stars<7);

            compoundnbt.putByte("Flight", (byte)(random.nextInt(3)+1));
            compoundnbt.put("Explosions", listnbt);


            return new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.PAPER), itemstack, 12, this.villagerXp, 0.2F);
        }



    }
    static class StarForEmeraldTrade implements VillagerTrades.ITrade {
        private final int villagerXp;

        public StarForEmeraldTrade(int xp) {
            this.villagerXp = xp;
        }

        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_STAR);
            itemstack.addTagElement("Explosion", createRandomFireworkStar(random));
            return new MerchantOffer(new ItemStack(Items.EMERALD, 1), itemstack, 12, this.villagerXp, 0.2F);
        }



    }


    public static CompoundNBT createRandomFireworkStar(Random random){
        CompoundNBT tag = new CompoundNBT();
        tag.putByte("Type", (byte)FireworkRocketItem.Shape.values()
                [random.nextInt(FireworkRocketItem.Shape.values().length)].getId());
        tag.putBoolean("Flicker", random.nextFloat()<0.4f);
        tag.putBoolean("Trail", random.nextFloat()<0.4f);
        List<Integer> list = Lists.newArrayList();
        int colors = 0;
        do{
            list.add(DyeColor.values()[random.nextInt(DyeColor.values().length)].getFireworkColor());
            colors++;
        }while(random.nextFloat()<0.4f && colors < 9);
        tag.putIntArray("Colors", list);



        if(random.nextBoolean()) {
            List<Integer> fadeList = Lists.newArrayList();
            colors = 0;
            do{
                fadeList.add(DyeColor.values()[random.nextInt(DyeColor.values().length)].getFireworkColor());
                colors++;
            }while(random.nextFloat()<0.4f && colors < 9);
            tag.putIntArray("FadeColors", fadeList);
        }

        return tag;
    }
}
