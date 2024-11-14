package net.mehvahdjukaar.supplementaries.common.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import static net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile.PUBLIC_KEY;

public final class PresentAddress implements TooltipProvider {

    private static final Component PUBLIC = Component.translatable("message.supplementaries.present.public").withStyle(ChatFormatting.GRAY);

    public static final Codec<PresentAddress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("recipient").forGetter(PresentAddress::recipient),
            Codec.STRING.fieldOf("sender").forGetter(PresentAddress::sender),
            Codec.STRING.fieldOf("description").forGetter(PresentAddress::description)
    ).apply(instance, PresentAddress::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PresentAddress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PresentAddress::recipient,
            ByteBufCodecs.STRING_UTF8, PresentAddress::sender,
            ByteBufCodecs.STRING_UTF8, PresentAddress::description,
            PresentAddress::new
    );
    private final String recipient;
    private final String sender;
    private final String description;
    @Nullable
    private final Component recipientComp;
    @Nullable
    private final Component senderComp;

    private PresentAddress(String recipient, String sender, String description) {
        this.recipient = recipient;
        this.sender = sender;
        this.description = description;
        this.recipientComp = getRecipientMessage(recipient);
        this.senderComp = getSenderMessage(sender);
    }

    // at least one of them must be not empty
    @Nullable
    public static PresentAddress of(String recipient, String sender, String description) {
        if (recipient.isEmpty() && sender.isEmpty()) return null;
        return new PresentAddress(recipient, sender, description);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        boolean isPacked = false;
        if (senderComp != null) {
            tooltipAdder.accept(senderComp);
            isPacked = true;
        }
        if (recipientComp != null) {
            tooltipAdder.accept(recipientComp);
            isPacked = true;
        }
        if (!isPacked) {
            tooltipAdder.accept(PUBLIC);
        }
    }

    @Nullable
    private static MutableComponent getSenderMessage(String sender) {
        if (sender.isEmpty()) return null;
        return Component.translatable("message.supplementaries.present.from", sender)
                .withStyle(ChatFormatting.GRAY);
    }

    @Nullable
    private static MutableComponent getRecipientMessage(String recipient) {
        if (recipient.isEmpty()) return null;
        if (recipient.equalsIgnoreCase(PUBLIC_KEY)) {
            return Component.translatable("message.supplementaries.present.public")
                    .withStyle(ChatFormatting.GRAY);
        } else {
            return Component.translatable("message.supplementaries.present.to", recipient)
                    .withStyle(ChatFormatting.GRAY);
        }
    }

    public @Nullable String recipient() {
        return recipient;
    }

    public @Nullable String sender() {
        return sender;
    }

    public @Nullable String description() {
        return description;
    }

    public boolean isPublic() {
        return recipient.equalsIgnoreCase(PUBLIC_KEY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PresentAddress that)) return false;
        return Objects.equals(recipient, that.recipient) && Objects.equals(sender, that.sender) && Objects.equals(description, that.description) && Objects.equals(recipientComp, that.recipientComp) && Objects.equals(senderComp, that.senderComp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipient, sender, description, recipientComp, senderComp);
    }
}
