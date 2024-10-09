package net.mehvahdjukaar.supplementaries.reg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.mehvahdjukaar.supplementaries.common.fluids.LumiseneFluid;
import net.mehvahdjukaar.supplementaries.common.items.LumiseneBottleItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class ModFluids {

    public static void init() {
    }

    public static final int LUMISENE_MAX_LAYERS = 16;

    public static final int LUMISENE_FAKE_LIGHT_EMISSION = 11;// quad emissivity and actual light value for the block. Lumisene does not propagate light (like magma blocks)


    public static final Supplier<FiniteFluid> LUMISENE_FLUID;
    public static final Supplier<FlammableLiquidBlock> LUMISENE_BLOCK;
    public static final Supplier<BucketItem> LUMISENE_BUCKET;
    public static final Supplier<Item> LUMISENE_BOTTLE;

    public static final DynamicHolder<SoftFluid> LUMISENE_SOFT_FLUID = DynamicHolder.of(
            Supplementaries.res(ModConstants.LUMISENE_NAME), SoftFluidRegistry.KEY);

    static {

        LUMISENE_FLUID = registerFluid(ModConstants.LUMISENE_NAME, LumiseneFluid::new);

        LUMISENE_BLOCK = RegHelper.registerBlock(Supplementaries.res(ModConstants.LUMISENE_NAME),
                () -> new FlammableLiquidBlock(LUMISENE_FLUID,
                        BlockBehaviour.Properties.of()
                                .replaceable()
                                .instabreak()
                                .mapColor(DyeColor.ORANGE)
                                .pushReaction(PushReaction.DESTROY)
                                .liquid()
                                .noCollission()
                                .noLootTable()
                                .hasPostProcess((state, level, pos) -> true)
                                .sound(SoundType.EMPTY)
                                .strength(0, 100.0F),
                        0));

        LUMISENE_BUCKET = RegHelper.registerItem(Supplementaries.res("lumisene_bucket"),
                ModFluids::createLumiseneBucket);

        LUMISENE_BOTTLE = RegHelper.registerItem(Supplementaries.res("lumisene_bottle"),
                () -> new LumiseneBottleItem(new Item.Properties()
                        .stacksTo(1)
                        .craftRemainder(Items.GLASS_BOTTLE)
                        .food(new FoodProperties.Builder()
                                .nutrition(0).saturationMod(0).alwaysEat()
                                .effect(new MobEffectInstance(MobEffects.GLOWING, CommonConfigs.Functional.GLOWING_DURATION.get(), 0), 1)
                                .effect(new MobEffectInstance(ModRegistry.FLAMMABLE.get(), CommonConfigs.Functional.FLAMMABLE_DURATION.get(), 0), 1)
                                .build())
                ));
    }

    @ExpectPlatform
    private static BucketItem createLumiseneBucket() {
        throw new AssertionError();
    }

    public static <T extends Fluid> Supplier<T> registerFluid(String name, Supplier<T> fluidSupplier) {
        return RegHelper.register(Supplementaries.res(name), fluidSupplier, Registries.FLUID);
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
