package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.client.renderers.items.BlackboardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CandyItem extends Item {
    //TODO: use capability here

    private static final FoodProperties CANDIE_FOOD = (new FoodProperties.Builder())
            .nutrition(1).saturationMod(0.2F).fast().alwaysEat().build();
    private static final int SUGAR_PER_CANDY = 10 * 20;
    private static final int EFFECT_THRESHOLD = 80 * 20;

    private static final Map<UUID, Integer> SWEET_TOOTH_COUNTER = new HashMap<>();

    @ApiStatus.Internal
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

    public CandyItem(Properties properties) {
        super(properties.food(CANDIE_FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        increaseSweetTooth(world, entity, SUGAR_PER_CANDY);
        return super.finishUsingItem(stack, world, entity);
    }

    //call for other candies from other mods
    public static void increaseSweetTooth(Level world, LivingEntity entity, int amount) {
        if (!world.isClientSide && entity instanceof Player) {
            UUID id = entity.getUUID();
            int i = SWEET_TOOTH_COUNTER.getOrDefault(id, 0);
            i += amount;
            if (i > EFFECT_THRESHOLD) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400));
            }
            SWEET_TOOTH_COUNTER.put(id, i);
        }
    }
}
