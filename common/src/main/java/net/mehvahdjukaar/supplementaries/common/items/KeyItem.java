package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class KeyItem extends Item {

    public KeyItem(Properties properties) {
        super(properties);
    }

    @ForgeOverride
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.value().effects().has(EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP);
    }

    @ForgeOverride
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //only needed for fabric. forge uses better stuff
        if (PlatHelper.getPlatform().isFabric() && context.getPlayer().isSecondaryUseActive()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            var tile = level.getBlockEntity(pos);
            if (tile instanceof KeyLockableTile t) {
                if (t.tryClearingKey(context.getPlayer(), context.getItemInHand())) {
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else if (tile instanceof SafeBlockTile) { //why is this needed?
                return level.getBlockState(pos).useItemOn(
                                context.getItemInHand(),
                                level, context.getPlayer(), context.getHand(),
                                new BlockHitResult(Vec3.atCenterOf(pos),
                                        Direction.UP, pos, false))
                        .result();
            }
        }
        return super.useOn(context);
    }

    public String getPassword(ItemStack stack) {
        if (stack.get(DataComponents.CUSTOM_NAME) == null) {
            return "Key"; //non translated name
        }
        return stack.getHoverName().getString();
    }


}