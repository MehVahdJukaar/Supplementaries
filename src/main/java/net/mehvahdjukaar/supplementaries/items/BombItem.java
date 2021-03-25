package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class BombItem extends Item {
    private final boolean blue;
    public BombItem(Item.Properties builder) {
        this(builder, false);

    }
    public BombItem(Item.Properties builder, boolean blue) {
        super(builder);
        this.blue = blue;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return blue;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return blue?Rarity.EPIC:Rarity.RARE;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack itemstack = playerIn.getItemInHand(handIn);

        if(false){
            playerIn.displayClientMessage(new StringTextComponent("You wished..."),true);
            return ActionResult.pass(itemstack);
        }
        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldowns().addCooldown(this, 30);
        if (!worldIn.isClientSide) {
            BombEntity bombEntity = new BombEntity(worldIn, playerIn, blue);
            float pitch = -10;//playerIn.isSneaking()?0:-20;
            bombEntity.shootFromRotation(playerIn, playerIn.xRot, playerIn.yRot, pitch, 1.25F, 0.9F);
            worldIn.addFreshEntity(bombEntity);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));
        if (!playerIn.abilities.instabuild) {
            itemstack.shrink(1);

        }

        return ActionResult.sidedSuccess(itemstack, worldIn.isClientSide());
    }
}

