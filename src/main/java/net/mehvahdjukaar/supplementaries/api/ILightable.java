package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Author: MehVahdJukaar
 * Used for blocks that can be lit up. Implement for best compatibility. Do not modify
 */
public interface ILightable {

    TagKey<Item> FLINT_AND_STEELS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "tools/flint_and_steel"));

    boolean isLit(BlockState state);

    BlockState toggleLitState(BlockState state, boolean lit);

    default boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world, ILightable.FireSound sound) {
        if (!isLit(state)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, toggleLitState(state, true), 3);
                sound.play(world, pos);
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    default boolean extinguish(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world) {
        if (this.isLit(state)) {
            if (!world.isClientSide()) {
                world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
                world.setBlock(pos, toggleLitState(state, false), 3);
            } else {
                Random random = world.getRandom();
                for (int i = 0; i < 10; ++i) {
                    world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25f + random.nextFloat() * 0.5f, pos.getY() + 0.35f + random.nextFloat() * 0.5f, pos.getZ() + 0.25f + random.nextFloat() * 0.5f, 0, 0.005, 0);
                }
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    //true on state change
    default boolean interactWithProjectile(Level level, BlockState state, Projectile projectile, BlockPos pos) {
        if (projectile.isOnFire()) {
            Entity entity = projectile.getOwner();
            if (entity == null || entity instanceof Player || ForgeEventFactory.getMobGriefingEvent(level, entity)) {
                if (lightUp(projectile, state, pos, level, FireSound.FLAMING_ARROW)) {
                    return true;
                }
            }
        } else if (projectile instanceof ThrownPotion potion && PotionUtils.getPotion(potion.getItem()) == Potions.WATER) {
            Entity entity = projectile.getOwner();
            boolean flag = entity == null || entity instanceof Player || ForgeEventFactory.getMobGriefingEvent(level, entity);
            if (flag && extinguish(projectile, state, pos, level)) {
                return true;
            }
        }
        return false;
    }

    default InteractionResult interactWithPlayer(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn) {
        if (!this.isLit(state) && player.getAbilities().mayBuild) {
            ItemStack stack = player.getItemInHand(handIn);
            Item item = stack.getItem();
            if (item instanceof FlintAndSteelItem || stack.is(FLINT_AND_STEELS)) {
                if (lightUp(player, state, pos, worldIn, FireSound.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
            } else if (item instanceof FireChargeItem) {
                if (lightUp(player, state, pos, worldIn, FireSound.FIRE_CHANGE)) {
                    stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    if (!player.isCreative()) stack.shrink(1);
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    enum FireSound {
        FLINT_AND_STEEL,
        FIRE_CHANGE,
        FLAMING_ARROW;

        public void play(LevelAccessor world, BlockPos pos) {
            switch (this) {
                case FIRE_CHANGE -> world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F + 1.0F);
                case FLAMING_ARROW -> world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.4F);
                case FLINT_AND_STEEL -> world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            }
        }
    }

}
