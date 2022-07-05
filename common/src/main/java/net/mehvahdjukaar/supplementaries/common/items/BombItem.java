package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class BombItem extends Item {
    private final BombEntity.BombType type;
    private final boolean glint;

    public BombItem(Properties builder) {
        this(builder, BombEntity.BombType.NORMAL, false);
    }

    public BombItem(Properties builder, BombEntity.BombType type, boolean glint) {
        super(builder);
        this.type = type;
        this.glint = glint;
    }

    public BombEntity.BombType getType() {
        return type;
    }

    @Override
    protected boolean allowedIn(CreativeModeTab pCategory) {
        if(this.type == BombEntity.BombType.SPIKY && !Registry.ITEM.getTagOrEmpty(TagKey.create(Registry.ITEM_REGISTRY,
                new ResourceLocation("forge:ingots/lead"))).iterator().hasNext()){
            return false;
        }
        return super.allowedIn(pCategory);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        super.fillItemCategory(pCategory, pItems);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return glint;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return type== BombEntity.BombType.BLUE ? Rarity.EPIC : Rarity.RARE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {

        ItemStack itemstack = playerIn.getItemInHand(handIn);

        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldowns().addCooldown(this, 30);
        if (!worldIn.isClientSide) {
            BombEntity bombEntity = new BombEntity(worldIn, playerIn, type);
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

