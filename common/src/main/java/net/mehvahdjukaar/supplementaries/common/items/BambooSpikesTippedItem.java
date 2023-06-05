package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BambooSpikesTippedItem extends WoodBasedBlockItem implements SimpleWaterloggedBlock {


    public BambooSpikesTippedItem(Block blockIn, Properties builder) {
        super(blockIn, builder, 150);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        PotionUtils.addPotionTooltip(stack, tooltip, BambooSpikesBlockTile.POTION_MULTIPLIER);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }
    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return PotionUtils.getColor(stack);
        //return MathHelper.rgb(106,81,178);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !CommonConfigs.Functional.ONLY_ALLOW_HARMFUL.get();
    }

    public static boolean isPotionValid(Potion potion){
        List<MobEffectInstance> effects = potion.getEffects();
        if(CommonConfigs.Functional.ONLY_ALLOW_HARMFUL.get()){
            for(var e: effects){
                if(e.getEffect().isBeneficial()) return false;
            }
        }
        return !MiscUtils.isTagged(potion, Registry.POTION,ModTags.TIPPED_SPIKES_POTION_BLACKLIST);
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        //freaking bookshelf mod is calling this method before configs are loaded...
        if(CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get()) {
            if (this.allowedIn(group)) {
                items.add(makeSpikeItem(Potions.POISON));
                items.add(makeSpikeItem(Potions.LONG_POISON));
                items.add(makeSpikeItem(Potions.STRONG_POISON));
                for (Potion potion : net.minecraft.core.Registry.POTION) {
                    if (potion == Potions.POISON || potion == Potions.LONG_POISON || potion == Potions.STRONG_POISON)
                        continue;
                    if (!potion.getEffects().isEmpty() && potion != Potions.EMPTY && isPotionValid(potion)) {
                        items.add(makeSpikeItem(potion));
                    }
                }
            }
        }
    }

    public static ItemStack makeSpikeItem(Potion potion) {
        ItemStack stack = new ItemStack(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        PotionUtils.setPotion(stack, potion);
        return stack;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.supplementaries.bamboo_spikes_tipped";
        //return PotionUtils.getPotionTypeFromNBT(stack.getChildTag("BlockEntityTag")).getNamePrefixed(super.getTranslationKey() + ".effect.");
    }

    @Override
    public Component getName(ItemStack stack) {
        Potion p = PotionUtils.getPotion(stack);
        Component arrowName = Component.translatable(p.getName("item.minecraft.tipped_arrow.effect."));
        String s = arrowName.getString();
        if (s.contains("Arrow of ")) {
            return Component.translatable("item.supplementaries.bamboo_spikes_tipped_effect",
                    s.replace("Arrow of ", ""));
        }
        return Component.translatable(this.getDescriptionId(stack));
    }

    @Override
    public ItemStack getDefaultInstance() {
        return makeSpikeItem(Potions.POISON);
    }
}
