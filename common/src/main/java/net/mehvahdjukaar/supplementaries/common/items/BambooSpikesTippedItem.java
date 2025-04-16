package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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

    @ForgeOverride
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
        return !CommonConfigs.Functional.ONLY_ALLOW_HARMFUL_INFINITE.get();
    }

    public static boolean isPotionValid(Potion potion) {
        List<MobEffectInstance> effects = potion.getEffects();
        if (CommonConfigs.Functional.ONLY_ALLOW_HARMFUL.get()) {
            for (var e : effects) {
                if (e.getEffect().isBeneficial()) return false;
            }
        }
        return !BuiltInRegistries.POTION.wrapAsHolder(potion).is(ModTags.TIPPED_SPIKES_POTION_BLACKLIST);
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @ForgeOverride
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }


    @Override
    public String getDescriptionId(ItemStack stack) {
        Potion p = PotionUtils.getPotion(stack);
        return p.getName("item.supplementaries.bamboo_spikes_tipped.effect.");
    }

    public static boolean isPotionValid(PotionContents potion) {
        if (!potion.hasEffects()) return false;
        Boolean alternativeMode = CommonConfigs.Functional.ONLY_ALLOW_HARMFUL_INFINITE.get();
        if (alternativeMode) {
            for (var e : potion.getAllEffects()) {
                if (e.getEffect().value().isBeneficial()) return false;
            }
        }
        Optional<Holder<Potion>> holder = potion.potion();
        return holder.isEmpty() || !holder.get().is(alternativeMode ?
                ModTags.TIPPED_SPIKES_POTION_BLACKLIST : ModTags.TIPPED_SPIKES_FINITE_POTION_BLACKLIST);
    }
    @Override
    public ItemStack getDefaultInstance() {
        return makeSpikeItem(Potions.POISON);
    }

    public static ItemStack makeSpikeItem(Potion potion) {
        ItemStack stack = new ItemStack(ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        PotionUtils.setPotion(stack, potion);
        return stack;
    }
}
