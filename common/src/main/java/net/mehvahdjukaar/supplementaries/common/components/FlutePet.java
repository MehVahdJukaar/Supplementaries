package net.mehvahdjukaar.supplementaries.common.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.UUID;
import java.util.function.Consumer;

public record FlutePet(Component name, UUID uuid) implements TooltipProvider {

    public static final Codec<FlutePet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(FlutePet::name),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(FlutePet::uuid)
    ).apply(instance, FlutePet::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlutePet> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_STREAM_CODEC, FlutePet::name,
            UUIDUtil.STREAM_CODEC, FlutePet::uuid,
            FlutePet::new
    );

    public static FlutePet of(Entity target) {
        Component name = target.getName();
        UUID id = target.getUUID();
        return new FlutePet(name, id);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        tooltipAdder.accept(name.copy().withStyle(ChatFormatting.GRAY));
    }
}
