package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractCandleSkullBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;
import vazkii.quark.addons.oddities.util.Influence;
import vazkii.quark.api.IEnchantmentInfluencer;

import java.util.List;

@OptionalMixin("vazkii.quark.api.IEnchantmentInfluencer")
@Mixin(AbstractCandleSkullBlock.class)
public abstract class CompatQuarkSelfCandleSkullMixin implements IEnchantmentInfluencer {

    @Shadow
    public abstract ParticleType<? extends ParticleOptions> getParticle();

    private DyeColor getColor(BlockState s, BlockGetter level, BlockPos pos) {
        if (s.getValue(CandleBlock.LIT)) {
            if (level.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
                BlockState state = tile.getCandle();
                if (state.getBlock() instanceof CandleBlock) {
                    return BlocksColorAPI.getColor(state.getBlock());
                }
            }
        }
        return null;
    }

    @Override
    public float[] getEnchantmentInfluenceColor(BlockGetter world, BlockPos pos, BlockState state) {
        DyeColor color = getColor(state, world, pos);
        return color == null ? null : color.getTextureDiffuseColors();
    }

    @Nullable
    @Override
    public ParticleOptions getExtraParticleOptions(BlockGetter world, BlockPos pos, BlockState state) {
        if (state.getValue(CandleBlock.LIT) && this.getParticle() != ParticleTypes.SMALL_FLAME) {
            return ParticleTypes.SOUL;
        }
        return null;
    }

    @Override
    public double getExtraParticleChance(BlockGetter world, BlockPos pos, BlockState state) {
        return 0.25;
    }

    @Override
    public int getInfluenceStack(BlockGetter world, BlockPos pos, BlockState state) {
        if (state.getValue(CandleBlock.LIT)) {
            return state.getValue(CandleBlock.CANDLES) + 1;
        }
        return 0;
    }

    @Override
    public boolean influencesEnchantment(BlockGetter world, BlockPos pos, BlockState state, Enchantment enchantment) {
        DyeColor color = getColor(state, world, pos);
        if (color == null) return false;
        Influence influence = MatrixEnchantingModule.candleInfluences.get(color);
        List<Enchantment> boosts = (this.getParticle() != ParticleTypes.SMALL_FLAME) ? influence.dampen() : influence.boost();
        return boosts.contains(enchantment);
    }

    @Override
    public boolean dampensEnchantment(BlockGetter world, BlockPos pos, BlockState state, Enchantment enchantment) {
        DyeColor color = getColor(state, world, pos);
        if (color == null) return false;
        Influence influence = MatrixEnchantingModule.candleInfluences.get(color);
        List<Enchantment> dampens = (this.getParticle() != ParticleTypes.SMALL_FLAME) ? influence.boost() : influence.dampen();
        return dampens.contains(enchantment);
    }
}
