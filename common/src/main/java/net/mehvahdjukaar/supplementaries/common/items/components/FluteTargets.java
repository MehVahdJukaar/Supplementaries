package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record FluteTargets(Set<Pet> pets) implements TooltipProvider {

    public static final Codec<FluteTargets> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Pet.CODEC.listOf().fieldOf("pets").forGetter(f -> f.pets.stream().toList())
    ).apply(instance, (a) -> new FluteTargets(Set.copyOf(a))));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluteTargets> STREAM_CODEC = StreamCodec.composite(
            Pet.STREAM_CODEC.apply(ByteBufCodecs.list()), f -> f.pets.stream().toList(),
            a -> new FluteTargets(Set.copyOf(a))
    );


    public static FluteTargets of(Entity target) {
        return new FluteTargets(Set.of(Pet.of(target)));
    }

    public FluteTargets andAdd(Entity target) {
        //create new instance with both
        List<Pet> newPets = new ArrayList<>(List.copyOf(this.pets));
        newPets.add(Pet.of(target));
        return new FluteTargets(Set.copyOf(newPets));
    }

    public int size() {
        return this.pets.size();
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        for (Pet pet : this.pets) {
            pet.addToTooltip(context, tooltipAdder, tooltipFlag);
        }
    }

    public Collection<Entity> getEntities(ServerLevel serverLevel) {
        return this.pets.stream()
                .map(pet -> serverLevel.getEntity(pet.uuid))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public record Pet(Component name, UUID uuid) implements TooltipProvider {
        public static Pet of(Entity entity) {
            return new Pet(entity.getName(), entity.getUUID());
        }

        public static final Codec<Pet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(Pet::name),
                Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(Pet::uuid)
        ).apply(instance, Pet::new));


        public static final StreamCodec<RegistryFriendlyByteBuf, Pet> STREAM_CODEC = StreamCodec.composite(
                ComponentSerialization.TRUSTED_STREAM_CODEC, Pet::name,
                UUIDUtil.STREAM_CODEC, Pet::uuid,
                Pet::new
        );

        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
            tooltipAdder.accept(name.copy().withStyle(ChatFormatting.GRAY));
        }
    }
}
