package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBallEntity;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        VibeChecker.assertSameLevel(level, player);
        if (!PlatHelper.isDev() || !player.isCreative()) return super.use(level, player, handIn);
        ItemStack itemstack = player.getItemInHand(handIn);
        if (!level.isClientSide) {

            CannonBallEntity bombEntity = new CannonBallEntity( player);
            float pitch = -10;//player.isSneaking()?0:-20;
            bombEntity.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    pitch, bombEntity.getDefaultShootVelocity(), 1);
            level.addFreshEntity(bombEntity);
        }


        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

}
