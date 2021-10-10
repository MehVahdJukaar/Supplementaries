package net.mehvahdjukaar.supplementaries.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CandyItem extends Item {

    private static final Map<UUID, Integer> SWEET_TOOTH_COUNTER = new HashMap<>();

    public static void checkSweetTooth(PlayerEntity entity) {
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

    private static final Food CANDIE_FOOD = (new Food.Builder()).nutrition(1).saturationMod(0.2F).fast().alwaysEat().build();

    private static final int SUGAR_PER_CANDY = 10 * 20;
    private static final int EFFECT_THRESHOLD = 80 * 20;

    public CandyItem(Properties properties) {
        super(properties.food(CANDIE_FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof PlayerEntity) {
            UUID id = entity.getUUID();
            int i = SWEET_TOOTH_COUNTER.getOrDefault(id,0);
            i += SUGAR_PER_CANDY;
            if (i > EFFECT_THRESHOLD) {
                entity.addEffect(new EffectInstance(Effects.CONFUSION, 400));
            }
            SWEET_TOOTH_COUNTER.put(id, i);
        }
        return super.finishUsingItem(stack, world, entity);
    }
}
