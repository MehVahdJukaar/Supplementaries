package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.block.util.ILightable;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Random;

public abstract class LightUpBlock extends Block implements ILightable {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LightUpBlock(Properties properties) {
        super(properties);
    }

    public boolean isLit(BlockState state) {
        return state.getValue(LIT);
    }

    public BlockState toggleLitState(BlockState state, boolean lit) {
        return state.setValue(LIT, lit);
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return this.material.isReplaceable();
    }

    //TODO: remove
    public void onChange(BlockState state, LevelAccessor world, BlockPos pos) {
    }

    @Override
    public boolean lightUp(BlockState state, BlockPos pos, LevelAccessor world, ILightable.FireSound sound) {
        if (!isLit(state)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, toggleLitState(state, true), 11);
                sound.play(world, pos);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean extinguish(BlockState state, BlockPos pos, LevelAccessor world) {
        if (this.isLit(state)) {
            if (!world.isClientSide()) {
                world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
                world.setBlock(pos, toggleLitState(state, false), 11);
            } else {
                Random random = world.getRandom();
                for (int i = 0; i < 10; ++i) {
                    world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25f + random.nextFloat() * 0.5f, pos.getY() + 0.35f + random.nextFloat() * 0.5f, pos.getZ() + 0.25f + random.nextFloat() * 0.5f, 0, 0.005, 0);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!this.isLit(state) && player.getAbilities().mayBuild) {
            ItemStack stack = player.getItemInHand(handIn);
            Item item = stack.getItem();
            if (item instanceof FlintAndSteelItem || stack.is(ModTags.FIRE_SOURCES)) {
                if (lightUp(state, pos, worldIn, FireSound.FLINT_AND_STEEL)) {
                    this.onChange(state, worldIn, pos);
                    stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
            } else if (item instanceof FireChargeItem) {
                if (lightUp(state, pos, worldIn, FireSound.FIRE_CHANGE)) {
                    this.onChange(state, worldIn, pos);
                    stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    if (!player.isCreative()) stack.shrink(1);
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
            } else if (item instanceof PotionItem && PotionUtils.getPotion(stack) == Potions.WATER) {
                if (extinguish(state, pos, worldIn)) {
                    this.onChange(state, worldIn, pos);
                    Utils.swapItem(player, handIn, stack, new ItemStack(Items.GLASS_BOTTLE));
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }


    @SuppressWarnings({"StrongCast", "OverlyStrongTypeCast"})
    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof Projectile projectile) {
            if (projectile.isOnFire()) {
                Entity entity = projectile.getOwner();
                if (entity == null || entity instanceof Player || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity)) {
                    if (lightUp(state, pos, worldIn, FireSound.FLAMING_ARROW)) this.onChange(state, worldIn, pos);
                }
            } else if (projectile instanceof ThrownPotion && PotionUtils.getPotion(((ThrowableItemProjectile) projectile).getItem()) == Potions.WATER) {
                Entity entity = projectile.getOwner();
                boolean flag = entity == null || entity instanceof Player || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(worldIn, entity);
                if (flag && extinguish(state, pos, worldIn)) {
                    this.onChange(state, worldIn, pos);
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState();
        return toggleLitState(state, !flag);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

}