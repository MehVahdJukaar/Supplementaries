package net.mehvahdjukaar.supplementaries.forge;

import biomesoplenty.common.block.BloodFluid;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModFluids {


    // what a mess
    public static final Supplier<FluidType> LUMISENE_TYPE;
    public static final Supplier<FlowingFluid> FLOWING_LUMISENE;
    public static final Supplier<FlowingFluid> STILL_LUMISENE;
    public static final Supplier<LumiseneBlock> LUMISENE_BLOCK;

    static  {
        FLOWING_LUMISENE = registerFluid(F.Flowing::new, "flowing_lumisene");
        STILL_LUMISENE = registerFluid(BloodFluid.Source::new, "lumisene");
        LUMISENE_TYPE = registerFluidType(() -> new FluidType(FluidType.Properties.create()
                .descriptionId("block.supplementaries.lumisene")
                .fallDistanceModifier(0.0F)
                .canExtinguish(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                .density(3000)
                .viscosity(6000)) {
            public @Nullable BlockPathTypes getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
                return canFluidLog ? super.getBlockPathType(state, level, pos, mob, true) : null;
            }

            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    private static final ResourceLocation UNDERWATER_TEXTURE = Supplementaries.res("textures/block/lumisene_underwater.png");
                    private static final ResourceLocation STILL_TEXTURE = Supplementaries.res("block/lumisene_still");
                    private static final ResourceLocation FLOWING_TEXTURE = Supplementaries.res("block/lumisene_flow");

                    public ResourceLocation getStillTexture() {
                        return STILL_TEXTURE;
                    }

                    public ResourceLocation getFlowingTexture() {
                        return FLOWING_TEXTURE;
                    }

                    public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                        return UNDERWATER_TEXTURE;
                    }

                    public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                        return new Vector3f(0.407F, 0.121F, 0.137F);
                    }

                    public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                        RenderSystem.setShaderFogStart(0.125F);
                        RenderSystem.setShaderFogEnd(1.5F);
                    }
                });
            }
        }, "lumisene");

     LUMISENE_BLOCK = RegHelper.registerBlock(Supplementaries.res("lumisene"),
             ()-> new LumiseneBlock(STILL_LUMISENE,
                     BlockBehaviour.Properties.of()
                             .replaceable().pushReaction(PushReaction.DESTROY)
                             .liquid().noCollission()
                             .randomTicks().noLootTable()
                             .sound(SoundType.EMPTY).strength(100.0F)));

    }

    public static <T extends Fluid> Supplier<T> registerFluid(Supplier<T> fluidSupplier, String name) {
        return RegHelper.registerFluid(Supplementaries.res(name), fluidSupplier);
    }

    public static Supplier<FluidType> registerFluidType(Supplier<FluidType> fluidSupplier, String name) {
        return RegHelper.register(Supplementaries.res( name), fluidSupplier,
                ForgeRegistries.Keys.FLUID_TYPES);
    }

    public static void init() {
    }

    public static class F extends LumiseneFluid{


        @Override
        public Fluid getFlowing() {
            return FLOWING_LUMISENE.get();
        }

        @Override
        public Fluid getSource() {
            return STILL_LUMISENE.get();
        }

        @Override
        protected boolean canConvertToSource(Level arg) {
            return false;
        }

        @Override
        protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {

        }

        @Override
        protected int getSlopeFindDistance(LevelReader level) {
            return 5;
        }

        @Override
        protected int getDropOff(LevelReader level) {
            return 0;
        }

        @Override
        public Item getBucket() {
            return null;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
            return false;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 1;
        }

        @Override
        protected float getExplosionResistance() {
            return 0;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState state) {
            return null;
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return 0;
        }

        public static class Source extends F {
            public Source() {
            }

            public int getAmount(FluidState state) {
                return 8;
            }

            public boolean isSource(FluidState state) {
                return true;
            }
        }

        public static class Flowing extends F {
            public Flowing() {
            }

            protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
                super.createFluidStateDefinition(builder);
                builder.add(LEVEL);
            }

            public int getAmount(FluidState p_76480_) {
                return (Integer)p_76480_.getValue(LEVEL);
            }

            public boolean isSource(FluidState p_76478_) {
                return false;
            }
        }
    }
}