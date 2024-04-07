package net.mehvahdjukaar.supplementaries.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class ModFluids {

    public static void init() {
    }

    public static final Supplier<FiniteFluid> LUMISENE_FLUID;
    public static final Supplier<FlammableLiquidBlock> LUMISENE_BLOCK;
    public static final Supplier<BucketItem> LUMISENE_BUCKET;

    static{

        LUMISENE_FLUID = registerFluid(ModConstants.LUMISENE_NAME, ModFluids::createLumisene);

        LUMISENE_BLOCK = RegHelper.registerBlock(Supplementaries.res(ModConstants.LUMISENE_NAME),
                () -> new FlammableLiquidBlock(LUMISENE_FLUID,
                        BlockBehaviour.Properties.of()
                                .replaceable()
                                .instabreak()
                                .mapColor(DyeColor.ORANGE)
                                .pushReaction(PushReaction.DESTROY)
                                .liquid()
                                .noCollission()
                                .randomTicks()
                                .noLootTable()
                                .sound(SoundType.EMPTY)
                                .strength(100.0F)));

        LUMISENE_BUCKET = RegHelper.registerItem(Supplementaries.res("lumisene_bucket"),
                ModFluids::createLumiseneBucket);
    }

    @ExpectPlatform
    private static BucketItem createLumiseneBucket() {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static FiniteFluid createLumisene() {
        throw new AssertionError();
    }

    public static <T extends Fluid> Supplier<T> registerFluid(String name, Supplier<T> fluidSupplier) {
        return RegHelper.register(Supplementaries.res(name), fluidSupplier, Registries.FLUID);
    }


}
