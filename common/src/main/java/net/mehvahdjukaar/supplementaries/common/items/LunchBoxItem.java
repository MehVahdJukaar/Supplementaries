package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.item.ILeftClickReact;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.client.renderers.items.LunchBoxItemRenderer;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class LunchBoxItem extends SelectableContainerItem<LunchBoxItem.Data> implements ICustomItemRendererProvider, ILeftClickReact {

    public LunchBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level level, List<Component> list, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, level, list, pIsAdvanced);
        if (MiscUtils.showsHints(level, pIsAdvanced)) {
            addClientTooltip(list);
        }
        Data data = this.getData(pStack);
        if (data != null) {
            boolean open = data.canEatFrom();
            list.add(open ?
                    Component.translatable("message.supplementaries.lunch_box.tooltip.open") :
                    Component.translatable("message.supplementaries.lunch_box.tooltip.closed"));
        }

    }

    @Environment(EnvType.CLIENT)
    private static void addClientTooltip(List<Component> list) {
        list.add(Component.translatable("message.supplementaries.lunch_box.tooltip",
                        Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage())
                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var data = getData(stack);
        if (data.canEatFrom()) {
            ItemStack food = data.getSelected();
            if (food.isEmpty()) {
                return InteractionResultHolder.fail(stack);
            }
            if (food.isEdible()) {
                if (player.canEat(SuppPlatformStuff.getFoodProperties(food, player).canAlwaysEat())) {
                    player.startUsingItem(hand);
                    return InteractionResultHolder.consume(stack);
                } else {
                    return InteractionResultHolder.fail(stack);
                }
            }

            return InteractionResultHolder.pass(stack);
        }
        return super.use(pLevel, player, hand);
    }

    @Override
    public boolean onLeftClick(ItemStack stack, Player player, InteractionHand hand) {
        var data = getData(stack);
        boolean open = data.canEatFrom();
        data.switchMode();

        if (open) {
            player.playSound(ModSounds.LUNCH_BASKET_CLOSE.get(),
                    0.3F, 1F + player.level().getRandom().nextFloat() * 0.3F);
        } else {
            player.playSound(ModSounds.LUNCH_BASKET_OPEN.get(),
                    0.3F, 1.6F + player.level().getRandom().nextFloat() * 0.3F);
        }
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Nullable
    @ForgeOverride
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
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
            ItemStack selected = data.getSelected();
            //assume it will be decremented by at most 1
            //hacks
            ItemStack copy = selected.copyWithCount(1);
            ItemStack result = copy.finishUsingItem(level, livingEntity);
            if (result.isEmpty()) {
                data.consumeSelected();
            } else if (result != copy) {
                data.consumeSelected();
                ItemStack remaining = data.tryAdding(result);

                if (!remaining.isEmpty() && livingEntity instanceof Player p && !p.getInventory().add(remaining)) {
                    p.drop(remaining, false);
                }
            }
            return stack;
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    protected void playInsertSound(Entity pEntity) {
        pEntity.playSound(ModSounds.LUNCH_BASKET_INSERT.get(), 0.8F,
                0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);

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
        return getLunchBoxSlot(entity).get();
    }

    @NotNull
    public static SlotReference getLunchBoxSlot(LivingEntity entity) {
        return SuppPlatformStuff.getFirstInInventory(entity, i -> i.getItem() instanceof LunchBoxItem);
    }

    public static boolean canAcceptItem(ItemStack toInsert) {
        if (toInsert.getItem().canFitInsideContainerItems()) return false;
        var animation = toInsert.getItem().getUseAnimation(toInsert);
        return animation == UseAnim.DRINK || animation == UseAnim.EAT;
    }

    public interface Data extends AbstractData {

        default boolean canAcceptItem(ItemStack toInsert) {
            return LunchBoxItem.canAcceptItem(toInsert);
        }

        boolean canEatFrom();

        void switchMode();
    }


}

