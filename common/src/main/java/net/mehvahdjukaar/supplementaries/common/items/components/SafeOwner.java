package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class SafeOwner implements TooltipProvider {

    private static final MutableComponent BOUND = Component.translatable("message.supplementaries.safe.bound").withStyle(ChatFormatting.GRAY);
    private static final MutableComponent UNBOUND = Component.translatable("message.supplementaries.safe.unbound").withStyle(ChatFormatting.GRAY);

    public static final Codec<SafeOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(s -> Optional.ofNullable(s.owner)),
            Codec.STRING.optionalFieldOf("password").forGetter(s -> Optional.ofNullable(s.password)),
            Codec.STRING.optionalFieldOf("owner").forGetter(s -> Optional.ofNullable(s.ownerName))
    ).apply(instance, SafeOwner::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SafeOwner> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), s -> Optional.ofNullable(s.owner),
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), s -> Optional.ofNullable(s.ownerName),
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), s -> Optional.ofNullable(s.password),
            SafeOwner::new
    );

    private final @Nullable UUID owner;
    private final @Nullable String ownerName;
    private final @Nullable String password;

    SafeOwner(Optional<UUID> owner, Optional<String> ownerName, Optional<String> password) {
        this.owner = owner.orElse(null);
        this.ownerName = ownerName.orElse(null);
        this.password = password.orElse(null);
    }

    // at least one of them must be not empty
    @Nullable
    public static SafeOwner of(@Nullable UUID owner, @Nullable String ownerName, @Nullable String password) {
        if (owner == null && password == null) return null;
        return new SafeOwner(Optional.ofNullable(owner), Optional.ofNullable(ownerName), Optional.ofNullable(password));
    }

    public @Nullable String ownerName() {
        return ownerName;
    }

    public @Nullable UUID owner() {
        return owner;
    }

    public @Nullable String password() {
        return password;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (CommonConfigs.Functional.SAFE_SIMPLE.get()) {
            if (owner != null) {
                if (!owner.equals(Minecraft.getInstance().player.getUUID())) {
                    if (ownerName != null) {
                        tooltipAdder.accept((Component.translatable("message.supplementaries.safe.owner", ownerName))
                                .withStyle(ChatFormatting.GRAY));
                    }
                    return;
                }
            }
            return;
        } else if (password != null) {
            tooltipAdder.accept(BOUND);
            return;
        }
        tooltipAdder.accept(UNBOUND);
    }

    // same as tile
    public boolean canPlayerOpen(Player player) {
        if (player == null || player.isCreative()) return true;
        if (CommonConfigs.Functional.SAFE_SIMPLE.get()) {
            return !this.isNotOwnedBy(player);
        } else {
            return ItemsUtil.getPlayerKeyStatus(player, this.password).isCorrect();
        }
    }

    private boolean isNotOwnedBy(Player player) {
        return owner != null && !owner.equals(player.getUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SafeOwner safeOwner)) return false;
        return Objects.equals(owner, safeOwner.owner) && Objects.equals(ownerName, safeOwner.ownerName) && Objects.equals(password, safeOwner.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, ownerName, password);
    }
}
