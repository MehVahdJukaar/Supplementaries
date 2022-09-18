package net.mehvahdjukaar.supplementaries.api;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
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
import org.jetbrains.annotations.Nullable;

/**
 * Author: MehVahdJukaar
 * Used for blocks that can be lit up. Implement for best compatibility. Do not modify
 */
public interface ILightable {

    TagKey<Item> FLINT_AND_STEELS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "tools/flint_and_steel"));

    boolean isLitUp(BlockState state);

    BlockState toggleLitState(BlockState state, boolean lit);

    default boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world, FireSourceType fireSourceType) {
        if (!isLitUp(state)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, toggleLitState(state, true), 3);
                playLightUpSound(world, pos, fireSourceType);
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    default boolean extinguish(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world) {
        if (this.isLitUp(state)) {
            if (!world.isClientSide()) {
                playExtinguishSound(world, pos);
                world.setBlock(pos, toggleLitState(state, false), 3);
            } else {
                spawnSmokeParticles(state, pos, world);
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
            if (entity == null || entity instanceof Player || PlatformHelper.isMobGriefingOn(level, entity)) {
                if (lightUp(projectile, state, pos, level, FireSourceType.FLAMING_ARROW)) {
                    return true;
                }
            }
        } else if (projectile instanceof ThrownPotion potion && PotionUtils.getPotion(potion.getItem()) == Potions.WATER) {
            Entity entity = projectile.getOwner();
            boolean flag = entity == null || entity instanceof Player || PlatformHelper.isMobGriefingOn(level, entity);
            if (flag && extinguish(projectile, state, pos, level)) {
                return true;
            }
        }
        return false;
    }

    //call on use
    default InteractionResult interactWithPlayer(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn) {
        if(player.getAbilities().mayBuild) {
            ItemStack stack = player.getItemInHand(handIn);
            if (!this.isLitUp(state)) {
                Item item = stack.getItem();
                if (item instanceof FlintAndSteelItem || stack.is(FLINT_AND_STEELS)) {
                    if (lightUp(player, state, pos, level, FireSourceType.FLINT_AND_STEEL)) {
                        stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                } else if (item instanceof FireChargeItem) {
                    if (lightUp(player, state, pos, level, FireSourceType.FIRE_CHANGE)) {
                        stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                        if (!player.isCreative()) stack.shrink(1);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }else if(this.canBeExtinguishedBy(stack)){
                if(extinguish(player, state, pos, level)){
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    default boolean canBeExtinguishedBy(ItemStack item){
        return item.getItem() instanceof ShovelItem;
    };

    default void playLightUpSound(LevelAccessor world, BlockPos pos, FireSourceType type){
        type.play(world, pos);
    }

    default void playExtinguishSound(LevelAccessor world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
    }

    default void spawnSmokeParticles(BlockState state, BlockPos pos, LevelAccessor world) {
        RandomSource random = world.getRandom();
        for (int i = 0; i < 10; ++i) {
            //particle offset
            world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25f + random.nextFloat() * 0.5f, pos.getY() + 0.35f + random.nextFloat() * 0.5f, pos.getZ() + 0.25f + random.nextFloat() * 0.5f, 0, 0.005, 0);
        }
    }

    enum FireSourceType {
        FLINT_AND_STEEL,
        FIRE_CHANGE,
        FLAMING_ARROW;

        public void play(LevelAccessor world, BlockPos pos) {
            switch (this) {
                case FIRE_CHANGE ->
                        world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F + 1.0F);
                case FLAMING_ARROW ->
                        world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.4F);
                case FLINT_AND_STEEL ->
                        world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            }
        }
    }

}
