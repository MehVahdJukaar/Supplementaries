package net.mehvahdjukaar.supplementaries.forge;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.forge.FiniteLiquidBlock.MAX_LEVEL;

public class ModFluids {


    // what a mess
    public static final Supplier<FluidType> LUMISENE_FLUID_TYPE;
    public static final Supplier<FiniteFluid> LUMISENE_FLUID;
    public static final Supplier<Block> LUMISENE_BLOCK;
    public static final Supplier<BucketItem> LUMISENE_BUCKET;

    static {
        LUMISENE_FLUID = registerFluid(("lumisene"), LumiseneFluid::new);

        LUMISENE_FLUID_TYPE = registerFluidType("lumisene", () -> new FluidType(FluidType.Properties.create()
                .descriptionId("block.supplementaries.lumisene")
                .fallDistanceModifier(1)
                .canExtinguish(false)
                .motionScale(0)
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
                    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("block/water_still");
                    private static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation("block/water_flow");

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

                    @Override
                    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                        int x = pos.getX();
                        int y = pos.getZ();
                        double frequency = 0.1;
                        double phaseShift = 0.0;
                        double amplitude = 127.0;
                        double center = 128.0;
                        int r = (int) (Math.sin(frequency * x + phaseShift) * amplitude + center);
                        int g = (int) (Math.sin(frequency * y + phaseShift) * amplitude + center);
                        int b = (int) (Math.sin(frequency * Math.sqrt(x * x + y * y) + phaseShift) * amplitude + center);

                        return FastColor.ARGB32.color(255, r, g, b);
                    }
                });
            }
        });

        LUMISENE_BLOCK = RegHelper.registerBlock(Supplementaries.res("lumisene"),
                () -> new FlammableLiquidBlock(LUMISENE_FLUID,
                        BlockBehaviour.Properties.of()
                                .replaceable()
                                .pushReaction(PushReaction.DESTROY)
                                .liquid()
                                .noCollission()
                                .randomTicks()
                                .noLootTable()
                                .sound(SoundType.EMPTY)
                                .strength(100.0F)));

        LUMISENE_BUCKET = RegHelper.registerItem(Supplementaries.res("lumisene_bucket"),
                () -> new FiniteFluidBucket(LUMISENE_FLUID, new Item.Properties()
                        .stacksTo(1)
                        .craftRemainder(Items.BUCKET)));

    }


    public static Supplier<FluidType> registerFluidType(String name, Supplier<FluidType> fluidSupplier) {
        return RegHelper.register(Supplementaries.res(name), fluidSupplier,
                ForgeRegistries.Keys.FLUID_TYPES);
    }

    public static <T extends Fluid> Supplier<T> registerFluid(String name, Supplier<T> fluidSupplier) {
        return RegHelper.register(Supplementaries.res(name), fluidSupplier, Registries.FLUID);
    }

    public static void init() {
        ClientHelper.addClientSetup(() -> {
            ItemBlockRenderTypes.setRenderLayer(LUMISENE_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(LUMISENE_FLUID.get(), RenderType.translucent());
        });
    }

    public static void messWithFluidH(BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Float> cir) {
        //  if(fluidState.isEmpty())cir.setReturnValue(1f);
    }

    public static void messWithAvH(BlockAndTintGetter level, Fluid fluid, float g, float h, float i, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        // cir.setReturnValue(Math.max(i,Math.max(g,h)));
    }

    public static class LumiseneFluid extends FiniteFluid {
        public LumiseneFluid() {
            super();
            this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, MAX_LEVEL));
        }

        @Override
        public FluidType getFluidType() {
            return ModFluids.LUMISENE_FLUID_TYPE.get();
        }

        @Override
        protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
            BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Block.dropResources(state, level, pos, blockEntity);
        }

        @Override
        public boolean isSame(Fluid fluid) {
            return fluid == LUMISENE_FLUID.get();
        }

        @Override
        public Item getBucket() {
            return LUMISENE_BUCKET.get();
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
            return true;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 5;
        }

        @Override
        protected float getExplosionResistance() {
            return 0;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState state) {
            return LUMISENE_BLOCK.get().defaultBlockState()
                    .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        protected static int getLegacyLevel(FluidState state) {
            int amount = state.getAmount();
            return MAX_LEVEL - Math.min(amount, MAX_LEVEL);
        }
    }
}