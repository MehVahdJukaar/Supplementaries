package net.mehvahdjukaar.supplementaries.items;


import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.util.CapturedMobsHelper;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TintedJarItem extends JarItem {
    public TintedJarItem(Block blockIn, Properties properties) {
        super(blockIn, properties);
    }

    @Override
    public boolean canItemCatch(Entity e) {
        EntityType<?> type = e.getType();
        if (ServerConfigs.cached.JAR_AUTO_DETECT && this.canFitEntity(e)) return true;
        return type.is(ModTags.TINTED_JAR_CATCHABLE) ||
                CapturedMobsHelper.CATCHABLE_FISHES.contains(type.getRegistryName().toString());
    }

    //prevents fireflies
    @Override
    public boolean isFirefly(Entity e) {
        return false;
    }

    //soul catching
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();

        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();
        if (player.isOnGround() && EnchantmentHelper.hasSoulSpeed(player) && world.getBlockState(pos).is(BlockTags.SOUL_SPEED_BLOCKS)) {

            BlockPos p = new BlockPos(player.getX(), player.getBoundingBox().minY - 0.5000001D, player.getZ());
            //Vector3d motion = player.getMotion();
            //boolean b = Math.abs(motion.x)+Math.abs(motion.z)>0.01;
            if (Math.abs(p.getX() - pos.getX()) < 2 && Math.abs(p.getZ() - pos.getZ()) < 2 && pos.getY() == p.getY()) {
                if (!world.isClientSide) {
                    Utils.swapItem(player, context.getHand(), context.getItemInHand(), new ItemStack(ModRegistry.SOUL_JAR_ITEM.get()));
                    //TODO: sound here
                    player.level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 1, 1);
                    player.level.playSound(null, player.blockPosition(), SoundEvents.SOUL_SAND_BREAK, SoundCategory.BLOCKS, 1f, 1.3f);
                    player.level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundCategory.BLOCKS, 0.8f, 1.5f);
                    return ActionResultType.CONSUME;
                }
                return ActionResultType.SUCCESS;
            }
        }
        return super.useOn(context);
    }

}
