package net.mehvahdjukaar.supplementaries.common.items.components;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class MobContainerView implements TooltipProvider {

    public static final Codec<MobContainerView> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MobContainer.MobData.CODEC.forGetter(v -> v.inner.getData()),
            Codec.FLOAT.fieldOf("width").forGetter(v -> v.inner.getWidth()),
            Codec.FLOAT.fieldOf("height").forGetter(v -> v.inner.getHeight()),
            Codec.BOOL.fieldOf("aquarium").forGetter(v -> v.inner.isAquarium())
    ).apply(instance, MobContainerView::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MobContainerView> STREAM_CODEC = StreamCodec.composite(
            MobContainer.MobData.STREAM_CODEC, v -> v.inner.getData(),
            ByteBufCodecs.FLOAT, v -> v.inner.getWidth(),
            ByteBufCodecs.FLOAT, v -> v.inner.getHeight(),
            ByteBufCodecs.BOOL, v -> v.inner.isAquarium(),
            MobContainerView::new
    );

    private final MobContainer inner;

    private MobContainerView(@NotNull MobContainer.MobData data, float width, float height, boolean aquarium) {
        this.inner = new MobContainer(width, height, aquarium);
        this.inner.setData(Preconditions.checkNotNull(data, "cannot create mob container view with null data"));

    }

    private MobContainerView(MobContainer container) {
        this.inner = container.makeCopy();
    }

    @Nullable
    public MobContainerView copyWithNewUUID(UUID newUUID) {
        if (inner.getData() instanceof MobContainer.MobData.Entity e) {
            return new MobContainerView(e.copyWithNewUUID(newUUID),
                    this.inner.getWidth(), this.inner.getHeight(), this.inner.isAquarium());
        } else return null;
    }

    public static MobContainerView of(MobContainer container) {
        Preconditions.checkNotNull(container.getData(), "cannot create mob container view with null container");
        return new MobContainerView(container.makeCopy());
    }

    public int getFishTexture() {
        return this.inner.getData().getFishTexture();
    }

    public Holder<SoftFluid> getVisualFluid() {
        return this.inner.getData().getVisualFluid();
    }

    public void apply(MobContainer mobContainer) {
        mobContainer.setData(this.inner.getData());
    }

    public Entity getVisualEntity() {
        //TODO: cache
        //   Entity e = CapturedMobCache.getOrCreateCachedMob(id, cmp2);
        //
        return inner.getDisplayedMob();
    }

    public float getRenderScale() {
        return ((MobContainer.MobData.Entity) inner.getData()).getScale();
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        String name = this.inner.getData().getName();
        if (name != null) {
            tooltipAdder.accept(Component.translatable(name).withStyle(ChatFormatting.GRAY));
        }
    }

    public MobContainer.MobData getDataUnsafe() {
        return this.inner.getData();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MobContainerView other) {
            return this.inner.getData().equals(other.inner.getData());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.inner.getData().hashCode();
    }
}
