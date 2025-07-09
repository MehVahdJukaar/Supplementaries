package net.mehvahdjukaar.supplementaries.reg.forge;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.renderers.forge.LumiseneFluidRenderPropertiesImpl;
import net.mehvahdjukaar.supplementaries.common.items.LumiseneBottleItem;
import net.mehvahdjukaar.supplementaries.common.items.forge.LumiseneBucketItem;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
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

}
