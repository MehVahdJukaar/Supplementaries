package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.common.entities.CannonBallEntity;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

class ThrowableBricksBehavior implements ItemUseBehavior {

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.THROWABLE_BRICKS_ENABLED.get();
    }

    @Nullable
    @Override
    public MutableComponent getTooltip() {
        return Component.translatable("message.supplementaries.throwable_brick");
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item.builtInRegistryHolder().is(ModTags.BRICKS);
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.BRICK_THROW.get(), SoundSource.NEUTRAL, 0.5F,
                0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClientSide) {
            CannonBallEntity projectile = new CannonBallEntity(player);
            projectile.setItem(stack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    projectile.getDefaultShootVelocity(), 1.0F);
            world.addFreshEntity(projectile);
        }
        
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}

