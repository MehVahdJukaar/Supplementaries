package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.entities.BombEntity;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class BombItem extends Item {
    private final boolean blue;
    private final boolean glint;

    public BombItem(Item.Properties builder) {
        this(builder, false, false);

    }

    public BombItem(Item.Properties builder, boolean blue, boolean glint) {
        super(builder);
        this.blue = blue;
        this.glint = glint;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return glint;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return blue ? Rarity.EPIC : Rarity.RARE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {

        ItemStack itemstack = playerIn.getItemInHand(handIn);

        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldowns().addCooldown(this, 30);
        if (!worldIn.isClientSide) {
            BombEntity bombEntity = new BombEntity(worldIn, playerIn, blue);
            float pitch = -10;//playerIn.isSneaking()?0:-20;
            bombEntity.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), pitch, 1.25F, 0.9F);
            worldIn.addFreshEntity(bombEntity);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));
        if (!playerIn.getAbilities().instabuild) {
            itemstack.shrink(1);

        }

        return InteractionResultHolder.sidedSuccess(itemstack, worldIn.isClientSide());
    }
}

