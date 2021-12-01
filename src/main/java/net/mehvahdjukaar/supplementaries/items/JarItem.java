package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.util.PotionNBTHelper;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.botania.BotaniaCompatRegistry;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JarItem extends AbstractMobContainerItem {

    public JarItem(Block blockIn, Properties properties) {
        super(blockIn, properties, 0.625f, 0.875f, true);
    }

    @Override
    public boolean canItemCatch(Entity e) {
        EntityType<?> type = e.getType();
        if (e instanceof MonsterEntity) return false;
        if (ServerConfigs.cached.JAR_AUTO_DETECT && this.canFitEntity(e)) return true;
        return this.isFirefly(e) || type.is(ModTags.JAR_CATCHABLE) || this.isBoat(e) ||
                CapturedMobsHelper.CATCHABLE_FISHES.contains(type.getRegistryName().toString());
    }

    @Override
    public void playReleaseSound(World world, Vector3d v) {
        world.playSound(null, v.x(), v.y(), v.z(), SoundEvents.CHICKEN_EGG, SoundCategory.PLAYERS, 1, 0.05f);
    }

    @Override
    public void playCatchSound(PlayerEntity player) {
        player.level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public ItemStack captureEntityInItem(Entity entity, ItemStack currentStack, ItemStack bucket) {
        if (this.isFirefly(entity)) {
            return new ItemStack(ModRegistry.FIREFLY_JAR_ITEM.get());
        } else if (this.isBoat(entity)) {
            return new ItemStack(ModRegistry.JAR_BOAT_ITEM.get());
        } else {
            return super.captureEntityInItem(entity, currentStack, bucket);
        }
    }

    public boolean isFirefly(Entity e) {
        return e.getType().getRegistryName().getPath().toLowerCase().contains("firefl");
    }

    public boolean isBoat(Entity e) {
        return e instanceof BoatEntity;
    }

    @Override
    public ActionResultType doInteract(ItemStack stack, PlayerEntity player, Entity entity, Hand hand) {
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
        if (RegistryConfigs.reg.JAR_TAB.get()) {
            if (group == ModRegistry.JAR_TAB) {
                JarTab.populateTab(items);
            }
        } else super.fillItemCategory(group, items);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        CompoundNBT tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) {
            if (tag.contains("FluidHolder")) {
                CompoundNBT com = tag.getCompound("FluidHolder");
                SoftFluid s = SoftFluidRegistry.get(com.getString("Fluid"));
                if (s == ModSoftFluids.DIRT) return Rarity.RARE;
            }
        }
        return super.getRarity(stack);
    }

    //nonsense jar drinking here

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        CompoundNBT tag = stack.getTagElement("BlockEntityTag");
        if (tag != null && entity instanceof PlayerEntity) {
            JarBlockTile temp = new JarBlockTile();
            temp.load(ModRegistry.JAR.get().defaultBlockState(), tag);
            SoftFluidHolder fh = temp.getSoftFluidHolder();
            if (fh.containsFood()) {
                if (fh.tryDrinkUpFluid((PlayerEntity) entity, world)) {
                    CompoundNBT newTag = new CompoundNBT();
                    temp.save(newTag);
                    stack.addTagElement("BlockEntityTag", newTag);
                    return stack;
                }
            }
        }
        return stack;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        if (this.getUseDuration(playerEntity.getItemInHand(hand)) != 0) {
            return DrinkHelper.useDrink(world, playerEntity, hand);
        }
        return super.use(world, playerEntity, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (ServerConfigs.cached.JAR_ITEM_DRINK) {
            CompoundNBT tag = stack.getTagElement("BlockEntityTag");
            if (tag != null) {
                JarBlockTile temp = new JarBlockTile();
                temp.load(ModRegistry.JAR.get().defaultBlockState(), tag);
                SoftFluidHolder fh = temp.getSoftFluidHolder();
                SoftFluid sf = fh.getFluid();
                Item food = sf.getFoodItem();
                return food.getUseDuration(food.getDefaultInstance()) / sf.getFoodDivider();

            }
        }
        return 0;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        if (ServerConfigs.cached.JAR_ITEM_DRINK) {
            return UseAction.DRINK;
        }
        return UseAction.NONE;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (CompatHandler.botania && this == ModRegistry.JAR_ITEM.get()) {
            ActionResultType r = BotaniaCompatRegistry.tryCaptureTater(this, context);
            if (r.consumesAction()) return r;
        }
        return super.useOn(context);
    }
}
