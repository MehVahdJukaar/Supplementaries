package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModEnchantments;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

class BubbleBlowerBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    protected BubbleBlowerBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        int charges = stack.getOrDefault(ModComponents.CHARGES.get(), 0);
        if (charges <= 0) {
            return InteractionResultHolder.fail(stack);
        }
        int maxCharges = stack.getOrDefault(ModComponents.MAX_CHARGES.get(), 0);

        ServerLevel level = source.level();
        Direction facing = source.state().getValue(DispenserBlock.FACING);

        // stasis: enchanted blowers deploy a bubble block instead of a puff
        if (EnchantmentHelper.has(stack, ModEnchantments.SPAWN_BUBBLE_BLOCK.get())) {
            BlockPos pos = source.pos().relative(facing);
            BlockState existing = level.getBlockState(pos);
            if (!existing.canBeReplaced()) {
                return InteractionResultHolder.fail(stack);
            }
            BlockState bubble = ModRegistry.BUBBLE_BLOCK.get().defaultBlockState();
            level.setBlockAndUpdate(pos, bubble);
            stack.set(ModComponents.CHARGES.get(),
                    Mth.clamp(charges - CommonConfigs.Tools.BUBBLE_BLOWER_COST.get(), 0, maxCharges));
            return InteractionResultHolder.success(stack);
        }

        // otherwise blow a small burst of bubbles out of the front face
        Vec3 dir = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vec3 p = Vec3.atCenterOf(source.pos()).add(dir.scale(0.55));
        level.sendParticles(ModParticles.SUDS_PARTICLE_EMITTER.get(),
                p.x, p.y, p.z, 0, dir.x, dir.y, dir.z, 1.0);
        stack.set(ModComponents.CHARGES.get(), charges - 1);
        return InteractionResultHolder.success(stack);
    }

    @Override
    protected void playSound(BlockSource source, boolean success) {
        if (success) {
            source.level().playSound(null, source.pos(), ModSounds.BUBBLE_BLOW.get(), SoundSource.BLOCKS, 1.0F, 0.95F);
        } else {
            super.playSound(source, success);
        }
    }
}
