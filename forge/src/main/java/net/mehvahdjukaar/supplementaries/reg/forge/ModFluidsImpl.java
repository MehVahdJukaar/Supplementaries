package net.mehvahdjukaar.supplementaries.reg.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.forge.LumiseneFluidRenderPropertiesImpl;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.mehvahdjukaar.supplementaries.common.items.forge.LumiseneBucketItem;
import net.mehvahdjukaar.supplementaries.common.items.forge.LumiseneBottleItem;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModFluidsImpl {

    protected static final int MAX_LAYERS = 16;

    public static final int LUMISENE_FAKE_LIGHT_EMISSION = 11;// quad emissivity and actual light value for the block. Lumisene does not propagate light (like magma blocks)

    public static BucketItem createLumiseneBucket() {
        return new LumiseneBucketItem(ModFluids.LUMISENE_FLUID, new Item.Properties().stacksTo(1)
                .craftRemainder(Items.BUCKET), MAX_LAYERS);
    }

    public static Item createLumiseneBottle() {
        return new LumiseneBottleItem(new Item.Properties().stacksTo(1)
                .craftRemainder(Items.GLASS_BOTTLE));
    }

    public static FiniteFluid createLumisene() {
        return new LumiseneFluid();
    }


    public static final Supplier<FluidType> LUMISENE_FLUID_TYPE = registerFluidType("lumisene", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.supplementaries.lumisene")
            .fallDistanceModifier(1)
            .canExtinguish(false)
            .motionScale(0)
            .lightLevel(LUMISENE_FAKE_LIGHT_EMISSION)
            .supportsBoating(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
            .density(500)
            .viscosity(100)) {

        public @Nullable BlockPathTypes getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
            return canFluidLog ? super.getBlockPathType(state, level, pos, mob, true) : null;
        }

        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new LumiseneFluidRenderPropertiesImpl());
        }
    });


    public static Supplier<FluidType> registerFluidType(String name, Supplier<FluidType> fluidSupplier) {
        return RegHelper.register(Supplementaries.res(name), fluidSupplier,
                ForgeRegistries.Keys.FLUID_TYPES);
    }


    public static class LumiseneFluid extends FiniteFluid {
        public LumiseneFluid() {
            super(MAX_LAYERS, ModFluids.LUMISENE_BLOCK, ModFluids.LUMISENE_BUCKET);
        }


        @Override
        public FluidType getFluidType() {
            return LUMISENE_FLUID_TYPE.get();
        }

        @Override
        protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            super.animateTick(level, pos, state, random);
            if (random.nextInt(12) == 0) {
                BlockState blockState = level.getBlockState(pos);
                if (blockState.getBlock() instanceof FlammableLiquidBlock fb &&
                        !fb.isLitUp(blockState, level, pos)) {
                    level.addParticle(ModParticles.SPARKLE_PARTICLE.get(),
                            pos.getX() + random.nextDouble(),
                            pos.getY() + getOwnHeight(state) + 1 / 32f,
                            pos.getZ() + random.nextDouble(),
                            0, 0, 0);
                }
            }
        }
    }


    public static int getLumiseneFaceLight(BlockAndTintGetter level, BlockPos pos, Operation<Integer> original) {
        if (level.getFluidState(pos).is(ModFluids.LUMISENE_FLUID.get())) {
            int i = level.getBrightness(LightLayer.SKY, pos);
            int j = level.getBrightness(LightLayer.BLOCK, pos);
            int minLight = LUMISENE_FAKE_LIGHT_EMISSION;
            if (j < minLight) {
                j = minLight;
            }

            return i << 20 | j << 4;
        }
        return original.call(level, pos);
    }


}
