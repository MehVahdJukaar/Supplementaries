package net.mehvahdjukaar.supplementaries.reg;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.components.BlackboardData;
import net.mehvahdjukaar.supplementaries.common.components.MobContainerView;
import net.mehvahdjukaar.supplementaries.common.components.PresentAddress;
import net.mehvahdjukaar.supplementaries.common.components.SoftFluidTankView;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ModComponents {

    public static void init() {

    }

    public static final Supplier<DataComponentType<Unit>> ANTIQUE_INK = register("antique_ink",
            Unit.CODEC, StreamCodec.unit(Unit.INSTANCE));

    public static final Supplier<DataComponentType<SoftFluidTankView>> SOFT_FLUID_CONTENT = register("soft_fluid_content",
            SoftFluidTankView.CODEC, SoftFluidTankView.STREAM_CODEC, true);

    public static final Supplier<DataComponentType<MobContainerView>> MOB_HOLDER_CONTENT = register("mob_holder_content",
            MobContainerView.CODEC, MobContainerView.STREAM_CODEC, true);

    public static final Supplier<DataComponentType<PresentAddress>> ADDRESS = register("address",
            PresentAddress.CODEC, PresentAddress.STREAM_CODEC);

    public static final Supplier<DataComponentType<BlackboardData>> BLACKBOARD = register("blackboard",
            BlackboardData.CODEC, BlackboardData.STREAM_CODEC);


    public static <T> Supplier<DataComponentType<T>> register(String name, Supplier<DataComponentType<T>> factory) {
        return RegHelper.registerDataComponent(Supplementaries.res(name), factory);
    }

    public static <T> Supplier<DataComponentType<T>> register(String name, Codec<T> codec,
                                                              @Nullable StreamCodec<RegistryFriendlyByteBuf, T> streamCodec,
                                                              boolean cache) {
        return RegHelper.registerDataComponent(Supplementaries.res(name), () -> {
            var builder = DataComponentType.<T>builder()
                    .persistent(codec);
            if (streamCodec != null) builder.networkSynchronized(streamCodec);
            if (cache) builder.cacheEncoding();
            return builder.build();
        });
    }

    public static <T> Supplier<DataComponentType<T>> register(String name, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return register(name, codec, streamCodec, false);
    }

    public static <T> Supplier<DataComponentType<T>> register(String name, Codec<T> codec) {
        return register(name, codec, null);
    }


}
