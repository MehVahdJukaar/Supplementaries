package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.AbstractBlock.Properties;
import net.minecraftforge.fml.ModList;
import vazkii.quark.api.IConditionalSticky;

public class MagmaCreamBlock extends BreakableBlock implements IConditionalSticky {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final boolean hasQuark = ModList.get().isLoaded("quark");
    public MagmaCreamBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }


    @Override
    public void fallOn(World world, BlockPos pos, Entity entity, float height) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(world, pos, entity, height);
        } else {
            entity.causeFallDamage(height, 0.0F);
        }

    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if(hasQuark)return;
        if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)return;
        tooltip.add(new TranslationTextComponent("message.supplementaries.magma_cream_block").withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if(context.getPlayer().isShiftKeyDown()) {
            return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
        }
        else return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    /*
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if(worldIn.getBlockState(pos).get(FACING)==Direction.UP)
            super.onFallenUpon(worldIn,pos,entityIn,fallDistance);
    }

    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        BlockState state = entityIn.world.getBlockState(entityIn.getPosition());
        if(state.getBlock()==this&&state.get(FACING)==Direction.UP)
            super.onLanded(worldIn,entityIn);
    }*/

    /*
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if(worldIn.getBlockState(pos).get(FACING)==Direction.UP)
            super.onFallenUpon(worldIn,pos,entityIn,fallDistance);
    }*/

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
    public void stepOn(World worldIn, BlockPos pos, Entity entityIn) {
        if (!entityIn.fireImmune() && entityIn instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entityIn)) {
            entityIn.hurt(DamageSource.HOT_FLOOR, 1.0F);
        }
        double d0 = Math.abs(entityIn.getDeltaMovement().y);
        if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
            double d1 = 0.4D + d0 * 0.2D;
            entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(d1, 1.0D, d1));
        }
        super.stepOn(worldIn, pos, entityIn);
    }

    @Override
    public boolean canStickToBlock(World world, BlockPos pistonPos, BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, Direction moveDir) {
        if(fromState.getBlock()==this) {
            Direction stickDir = fromState.getValue(FACING);
            if(fromPos.relative(stickDir).equals(toPos))return true;
            else if(fromPos.relative(stickDir.getOpposite()).equals(toPos))return false;
            else if (toState.getBlock()==this){
                Direction stickDir2 = toState.getValue(FACING);
                return stickDir2==stickDir ||
                        toPos.relative(stickDir2).equals(fromPos) && stickDir!=stickDir;
            }
            else return toState.getBlock().isStickyBlock(toState);
        }
        return false;
    }


}
