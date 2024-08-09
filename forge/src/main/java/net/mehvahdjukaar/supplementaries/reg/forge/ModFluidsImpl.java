package net.mehvahdjukaar.supplementaries.reg.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.forge.LumiseneFluidRendererImpl;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.items.forge.FiniteFluidBucket;
import net.mehvahdjukaar.supplementaries.common.items.forge.LumiseneBottleItem;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModFluidsImpl {

    protected static final int MAX_LAYERS = 16;

    public static BucketItem createLumiseneBucket() {
        return new FiniteFluidBucket(ModFluids.LUMISENE_FLUID, new Item.Properties().stacksTo(1)
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
            .lightLevel(4)
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
            consumer.accept(new LumiseneFluidRendererImpl());
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
    }

    public static void messWithAvH(BlockAndTintGetter level, Fluid fluid, float g, float h, float i, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        // cir.setReturnValue(Math.max(i,Math.max(g,h)));
        // if (fluid == ModFluidsImpl.LUMISENE_FLUID.get()) {
    }


    public static int messWithFluidLight(int original, Fluid fluid) {
        if (fluid == ModFluids.LUMISENE_FLUID.get()) {

            return Math.max(200, original);
        }
        return original;
    }


    public static int messWithFluidLight(BlockAndTintGetter level, BlockPos pos, Operation<Integer> original) {
        if (level.getFluidState(pos).is(ModFluids.LUMISENE_FLUID.get())) {
            int i = level.getBrightness(LightLayer.SKY, pos);
            int j = level.getBrightness(LightLayer.BLOCK, pos);
            BlockState state = level.getBlockState(pos);
            int k = 10;//state.getLightEmission(level, pos);
            if (j < k) {
                j = k;
            }

            return i << 20 | j << 4;
        }
        return original.call(level, pos);
    }

    public static int getLumiseneFakeLightEmission() {
        return 15;
    }

}
