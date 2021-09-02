package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.util.PotionNBTHelper;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.block.util.MobHolder;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.fluids.ModSoftFluids;
import net.mehvahdjukaar.supplementaries.items.tabs.JarTab;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JarItem extends CageItem {
    public JarItem(Block blockIn, Properties properties) {
        super(blockIn, properties, 0.625f, 0.875f);
    }

    @Override
    public boolean canItemCatch(Entity e) {
        EntityType<?> type = e.getType();
        if (e instanceof MonsterEntity) return false;
        if (ServerConfigs.cached.JAR_AUTO_DETECT && this.canFitEntity(e)) return true;
        return this.isFirefly(e) || type.is(ModTags.JAR_CATCHABLE) ||
                CapturedMobsHelper.CATCHABLE_FISHES.contains(type.getRegistryName().toString());
    }

    @Override
    public void playCatchSound(PlayerEntity player) {
        player.level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public ItemStack getFullItemStack(Entity entity, ItemStack currentStack) {
        if (!this.isFirefly(entity)) {
            return super.getFullItemStack(entity, currentStack);
        } else {
            return new ItemStack(ModRegistry.FIREFLY_JAR_ITEM.get());
        }
    }

    public boolean isFirefly(Entity e) {
        return e.getType().getRegistryName().getPath().toLowerCase().contains("firefl");
    }

    @Override
    public ActionResultType doInteract(ItemStack stack, PlayerEntity player, Entity entity, Hand hand) {
        //bucket stuff
        if (entity instanceof WaterMobEntity && this.isEntityValid(entity, player)) {
            ItemStack heldItem = player.getItemInHand(hand).copy();

            //hax incoming
            player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
            ActionResultType result = entity.interact(player, hand);
            if (!result.consumesAction()) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                result = entity.interact(player, hand);
            }

            if (result.consumesAction()) {
                ItemStack filledBucket = player.getItemInHand(hand);
                if (filledBucket != heldItem) {
                    ItemStack returnItem = new ItemStack(this);

                    CompoundNBT com = new CompoundNBT();
                    MobHolder.saveBucketToNBT(com, filledBucket, entity.getName().getString(), CapturedMobsHelper.getType(entity).getFishTexture());
                    returnItem.addTagElement("BlockEntityTag", com);

                    player.startUsingItem(hand);

                    Utils.swapItem(player, hand, stack, returnItem, true);
                    return ActionResultType.sidedSuccess(player.level.isClientSide);
                }
            }
            //hax
            player.setItemInHand(hand, heldItem);
            player.startUsingItem(hand);
        }
        //capture mob
        return super.doInteract(stack, player, entity, hand);
    }

    //full jar stuff

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt == null) {
            if (!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips) return;
            tooltip.add(new TranslationTextComponent("message.supplementaries.jar").withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
        } else {
            if (compoundnbt.contains("LootTable", 8)) {
                tooltip.add(new StringTextComponent("???????").withStyle(TextFormatting.GRAY));
            }

            if (compoundnbt.contains("FluidHolder")) {
                CompoundNBT com = compoundnbt.getCompound("FluidHolder");
                SoftFluid s = SoftFluidRegistry.get(com.getString("Fluid"));
                int count = com.getInt("Count");
                if (!s.isEmpty() && count > 0) {

                    CompoundNBT nbt = null;
                    String add = "";
                    if (com.contains("NBT")) {
                        nbt = com.getCompound("NBT");
                        if (nbt.contains("Bottle")) {
                            String bottle = nbt.getString("Bottle").toLowerCase();
                            if (!bottle.equals("regular")) add = "_" + bottle;
                        }
                    }

                    tooltip.add(new TranslationTextComponent("message.supplementaries.fluid_tooltip",
                            new TranslationTextComponent(s.getTranslationKey() + add), count).withStyle(TextFormatting.GRAY));
                    if (nbt != null) {
                        PotionNBTHelper.addPotionTooltip(nbt, tooltip, 1);
                        return;
                    }
                }
            }

            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                int i = 0;
                int j = 0;

                for (ItemStack itemstack : nonnulllist) {
                    if (!itemstack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            IFormattableTextComponent iformattabletextcomponent = itemstack.getHoverName().copy();

                            String s = iformattabletextcomponent.getString();
                            s = s.replace(" Bucket", "");
                            s = s.replace(" Bottle", "");
                            s = s.replace("Bucket of ", "");
                            IFormattableTextComponent str = new StringTextComponent(s);

                            str.append(" x").append(String.valueOf(itemstack.getCount()));
                            tooltip.add(str.withStyle(TextFormatting.GRAY));
                        }
                    }
                }
                if (j - i > 0) {
                    tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                }
            }
        }
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group) && RegistryConfigs.reg.JAR_TAB.get() && group == ModRegistry.JAR_TAB) {
            JarTab.populateTab(items);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("FluidHolder")) {
                CompoundNBT com = compoundnbt.getCompound("FluidHolder");
                SoftFluid s = SoftFluidRegistry.get(com.getString("Fluid"));
                if (s == ModSoftFluids.DIRT) return Rarity.RARE;
            }
        }
        return super.getRarity(stack);
    }

}
