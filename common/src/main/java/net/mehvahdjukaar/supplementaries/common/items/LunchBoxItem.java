package net.mehvahdjukaar.supplementaries.common.items;

import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.item.ILeftClickReact;
import net.mehvahdjukaar.moonlight.api.misc.FabricOverride;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RedstoneIlluminatorBlock;
import net.mehvahdjukaar.supplementaries.common.items.components.LunchBaskedContent;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.mixins.LivingEntityAccessor;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
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

public class LunchBoxItem extends SelectableContainerItem<LunchBaskedContent, LunchBaskedContent.Mutable>
        implements ILeftClickReact {

    public LunchBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (MiscUtils.showsHints(tooltipFlag)) {
            addClientTooltip(tooltipComponents);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void addClientTooltip(List<Component> list) {
        list.add(Component.translatable("message.supplementaries.lunch_box.tooltip",
                        Minecraft.getInstance().options.keyAttack.getTranslatedKeyMessage())
                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        VibeChecker.assertSameLevel(level, player);
        ItemStack basket = player.getItemInHand(hand);
        var data = basket.get(getComponentType());
        if (data != null && data.canEatFrom()) {
            ItemStack food = data.getSelected();
            if (food.isEmpty()) {
                return InteractionResultHolder.fail(basket);
            }
            player.setItemInHand(hand, food);
            //takes care of instant uses. Normally doesnt happen with food but we never know
            var result = food.use(level, player, hand);
            ItemStack resItem = result.getObject();
            LunchBaskedContent.Mutable mutable = data.toMutable();
            boolean success = swapWithSelected(player, resItem, mutable, food);
            if (success) {
                basket.set(getComponentType(), mutable.toImmutable());
            }
            ((LivingEntityAccessor) player).setUseItem(basket);
            player.setItemInHand(hand, basket);


            return new InteractionResultHolder<>(result.getResult(), basket);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean onLeftClick(ItemStack stack, Player player, InteractionHand hand) {
        var data = stack.get(getComponentType());
        if (data == null) return false;
        boolean open = data.canEatFrom();
        if (open) {
            player.playSound(ModSounds.LUNCH_BASKET_CLOSE.get(),
                    0.3F, 1F + player.level().getRandom().nextFloat() * 0.3F);
        } else {
            player.playSound(ModSounds.LUNCH_BASKET_OPEN.get(),
                    0.3F, 1.6F + player.level().getRandom().nextFloat() * 0.3F);
        }

        var mutable = data.toMutable();
        mutable.switchMode();
        stack.set(getComponentType(), mutable.toImmutable());
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Nullable
    @ForgeOverride
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        var data = stack.get(getComponentType());
        if (data != null && data.canEatFrom()) {
            return SuppPlatformStuff.getFoodProperties(data.getSelected(), entity);
        }
        return null;
    }

    @FabricOverride
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }


    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        var data = stack.get(getComponentType());
        if (data != null && data.canEatFrom()) {
            return data.getSelected().getUseDuration(livingEntity);
        }
        return super.getUseDuration(stack, livingEntity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        var data = stack.get(getComponentType());
        if (data != null && data.canEatFrom()) {
            return data.getSelected().getUseAnimation();
        }
        RedstoneIlluminatorBlock
        return super.getUseAnimation(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        var data = stack.get(getComponentType());
        if (data != null && data.canEatFrom()) {
            var mutable = data.toMutable();
            ItemStack selected = data.getSelected();
            //assume it will be decremented by at most 1
            //hacks
            ItemStack copy = selected.copyWithCount(1);
            ItemStack result = copy.finishUsingItem(level, livingEntity);
            boolean success = swapWithSelected(livingEntity, result, mutable, selected);

            if (success) stack.set(getComponentType(), mutable.toImmutable());
            return stack;
        }
        return super.finishUsingItem(stack, level, livingEntity);

    }


    private static boolean swapWithSelected(LivingEntity livingEntity, ItemStack result, LunchBaskedContent.Mutable data, ItemStack currentStack) {
        boolean success = false;
        if (result.isEmpty()) {
            data.getSelected().shrink(1);
            success = true;
        } else if (result != currentStack) {
            data.getSelected().shrink(1);
            ItemStack remaining = data.tryAdding(result);
            success = true;

            if (!remaining.isEmpty() && livingEntity instanceof Player p && !p.getInventory().add(remaining)) {
                p.drop(remaining, false);
            }
        }
        return success;
    }


    @Override
    protected void playInsertSound(Entity pEntity) {
        pEntity.playSound(ModSounds.LUNCH_BASKET_INSERT.get(), 0.8F,
                0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);

    }

    @Override
    public DataComponentType<LunchBaskedContent> getComponentType() {
        return ModComponents.LUNCH_BASKET_CONTENT.get();
    }

    @Override
    public int getMaxSlots() {
        return CommonConfigs.Tools.LUNCH_BOX_SLOTS.get();
    }

    @NotNull
    public static ItemStack findActiveLunchBox(LivingEntity entity) {
        return findActiveLunchBoxSlot(entity).get(entity);
    }

    @NotNull
    public static SlotReference findActiveLunchBoxSlot(LivingEntity entity) {
        return SuppPlatformStuff.getFirstInInventory(entity, i -> i.getItem() instanceof LunchBoxItem);
    }

    public static boolean canAcceptItem(ItemStack toInsert) {
        if (!toInsert.getItem().canFitInsideContainerItems()) return false;
        if (toInsert.is(ModTags.LUNCH_BASKET_BLACKLIST)) return false;
        var animation = toInsert.getItem().getUseAnimation(toInsert);
        return animation == UseAnim.DRINK || animation == UseAnim.EAT;
    }


}

