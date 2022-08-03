package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class KeyItem extends Item {

    public KeyItem(Properties properties) {
        super(properties);
    }


    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.VANISHING_CURSE;
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        var l = EnchantedBookItem.getEnchantments(book);
        return l.size() == 1 && l.get(0) == Enchantments.VANISHING_CURSE;
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //only needed for fabric. forge uses better stuff
        if (PlatformHelper.getPlatform().isFabric() && context.getPlayer().isSecondaryUseActive()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if (level.getBlockEntity(pos) instanceof KeyLockableTile t) {
                if (t.tryClearingKey(context.getPlayer(), context.getItemInHand())) {
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return super.useOn(context);
    }
}