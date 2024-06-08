package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBallEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CannonBallItem extends BlockItem {


    public CannonBallItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {

        if (!PlatHelper.isDev()) return super.use(worldIn, playerIn, handIn);
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        if (!worldIn.isClientSide) {
            CannonBallEntity bombEntity = new CannonBallEntity(worldIn, playerIn);
            float pitch = -10;//playerIn.isSneaking()?0:-20;
            bombEntity.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), pitch, 1.25F, 0.9F);
            worldIn.addFreshEntity(bombEntity);
        }


        return InteractionResultHolder.sidedSuccess(itemstack, worldIn.isClientSide());
    }

}
