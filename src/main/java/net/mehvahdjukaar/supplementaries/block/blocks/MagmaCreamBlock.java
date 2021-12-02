package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import vazkii.quark.api.IConditionalSticky;

import javax.annotation.Nullable;
import java.util.List;

public class MagmaCreamBlock extends HalfTransparentBlock implements IConditionalSticky {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public MagmaCreamBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float height) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(world, state, pos, entity, height);
        } else {
            entity.causeFallDamage(height, 0.0F, DamageSource.FALL);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (CompatHandler.quark) return;
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add(new TranslatableComponent("message.supplementaries.magma_cream_block").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer().isShiftKeyDown()) {
            return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
        } else return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    //piston push fix
    @Override
    public boolean isSlimeBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        return true;
    }

    @Override
    public void stepOn(Level worldIn,  BlockPos pos, BlockState state, Entity entityIn) {
        if (!entityIn.fireImmune() && entityIn instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entityIn)) {
            entityIn.hurt(DamageSource.HOT_FLOOR, 1.0F);
        }
        double d0 = Math.abs(entityIn.getDeltaMovement().y);
        if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
            double d1 = 0.4D + d0 * 0.2D;
            entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(d1, 1.0D, d1));
        }
        super.stepOn(worldIn, pos, state, entityIn);
    }

    @Override
    public boolean canStickToBlock(Level world, BlockPos pistonPos, BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, Direction moveDir) {
        if (fromState.getBlock() == this) {
            Direction stickDir = fromState.getValue(FACING);
            if (fromPos.relative(stickDir).equals(toPos)) return true;
            else if (fromPos.relative(stickDir.getOpposite()).equals(toPos)) return false;
            else if (toState.getBlock() == this) {
                Direction stickDir2 = toState.getValue(FACING);
                //TODO: fix
                return stickDir2 == stickDir ||
                        toPos.relative(stickDir2).equals(fromPos) && stickDir != stickDir;
            } else return toState.getBlock().isStickyBlock(toState);
        }
        return false;
    }


}
