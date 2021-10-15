package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.world.item.Item.Properties;

public class CandyItem extends Item {

    private static final Map<UUID, Integer> SWEET_TOOTH_COUNTER = new HashMap<>();

    public static void checkSweetTooth(Player entity) {
        UUID id = entity.getUUID();
        Integer i = SWEET_TOOTH_COUNTER.get(id);
        if (i != null) {
            if (i <= 0) {
                SWEET_TOOTH_COUNTER.remove(id);
            } else {
                SWEET_TOOTH_COUNTER.put(id, i - 1);
            }
        }
    }

    private static final FoodProperties CANDIE_FOOD = (new FoodProperties.Builder()).nutrition(1).saturationMod(0.2F).fast().alwaysEat().build();

    private static final int SUGAR_PER_CANDY = 10 * 20;
    private static final int EFFECT_THRESHOLD = 80 * 20;

    public CandyItem(Properties properties) {
        super(properties.food(CANDIE_FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof Player) {
            UUID id = entity.getUUID();
            int i = SWEET_TOOTH_COUNTER.getOrDefault(id,0);
            i += SUGAR_PER_CANDY;
            if (i > EFFECT_THRESHOLD) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400));
            }
            SWEET_TOOTH_COUNTER.put(id, i);
        }
        return super.finishUsingItem(stack, world, entity);
    }
}
