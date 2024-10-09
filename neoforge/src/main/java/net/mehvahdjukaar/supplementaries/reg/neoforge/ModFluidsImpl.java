package net.mehvahdjukaar.supplementaries.reg.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.neoforge.LumiseneFluidRenderPropertiesImpl;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.mehvahdjukaar.supplementaries.common.items.neoforge.LumiseneBucketItem;
import net.mehvahdjukaar.supplementaries.common.items.neoforge.LumiseneBottleItem;
import net.mehvahdjukaar.supplementaries.client.renderers.forge.LumiseneFluidRenderPropertiesImpl;
import net.mehvahdjukaar.supplementaries.common.items.LumiseneBottleItem;
import net.mehvahdjukaar.supplementaries.common.items.forge.LumiseneBucketItem;
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
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.ModFluids.LUMISENE_FAKE_LIGHT_EMISSION;
import static net.mehvahdjukaar.supplementaries.reg.ModFluids.LUMISENE_MAX_LAYERS;

public class ModFluidsImpl {


    public static BucketItem createLumiseneBucket() {
        return new LumiseneBucketItem(ModFluids.LUMISENE_FLUID, new Item.Properties().stacksTo(1)
                .craftRemainder(Items.BUCKET), LUMISENE_MAX_LAYERS);
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

        public @Nullable PathType getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
            return canFluidLog ? super.getBlockPathType(state, level, pos, mob, true) : null;
        }

        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new LumiseneFluidRenderPropertiesImpl());
        }
    });


    public static Supplier<FluidType> registerFluidType(String name, Supplier<FluidType> fluidSupplier) {
        return RegHelper.register(Supplementaries.res(name), fluidSupplier,
                NeoForgeRegistries.Keys.FLUID_TYPES);
    }

}
