package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.client.renderers.items.LunchBoxItemRenderer;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LunchBoxItem extends SelectableContainerItem<LunchBoxItem.Data> implements ICustomItemRendererProvider {

    public LunchBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pUsedHand) {
        ItemStack stack = player.getItemInHand(pUsedHand);
        var data = getData(stack);
        if (data.canEatFrom()) {
            ItemStack food = data.getSelected();
            if(food.isEdible()) {
                if (player.canEat(SuppPlatformStuff.getFoodProperties(food, player).canAlwaysEat())) {
                    player.startUsingItem(pUsedHand);
                    return InteractionResultHolder.consume(stack);
                } else {
                    return InteractionResultHolder.fail(stack);
                }
            }

            return InteractionResultHolder.pass(stack);
        }
        return super.use(pLevel, player, pUsedHand);
    }

    @ForgeOverride
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        var data = getData(stack);
        if (data.canEatFrom()) {
            return SuppPlatformStuff.getFoodProperties(data.getSelected(), entity);
        }
        return super.getFoodProperties();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        var data = getData(stack);
        if (data.canEatFrom()) {
            return data.getSelected().getUseDuration();
        }
        return super.getUseDuration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        var data = getData(stack);
        if (data.canEatFrom()) {
            return data.getSelected().getUseAnimation();
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        var data = getData(stack);
        if (data.canEatFrom()) {
            ItemStack eaten = data.getSelected().finishUsingItem(level, livingEntity);
            return stack;
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public Supplier<ItemStackRenderer> getRendererFactory() {
        return LunchBoxItemRenderer::new;
    }

    @Override
    public int getMaxSlots() {
        return CommonConfigs.Tools.LUNCH_BOX_SLOTS.get();
    }

    @Override
    public Data getData(ItemStack stack) {
        return getLunchBoxData(stack);
    }


    @Override
    public @NotNull ItemStack getFirstInInventory(Player player) {
        return getLunchBox(player);
    }

    @ExpectPlatform
    public static Data getLunchBoxData(ItemStack stack) {
        throw new AssertionError();
    }

    @NotNull
    public static ItemStack getLunchBox(LivingEntity entity) {
        return SuppPlatformStuff.getFirstInInventory(entity, i -> i.getItem() instanceof QuiverItem);
    }

    private static boolean canAcceptItem(ItemStack toInsert) {
        var anim = toInsert.getItem().getUseAnimation(toInsert);
        return anim == UseAnim.DRINK || anim == UseAnim.EAT;
    }

    public interface Data extends AbstractData {
        default boolean canAcceptItem(ItemStack toInsert) {
            return LunchBoxItem.canAcceptItem(toInsert);
        }

        boolean canEatFrom();
    }


}

