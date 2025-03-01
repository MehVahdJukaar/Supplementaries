package net.mehvahdjukaar.supplementaries.integration.create;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import io.netty.buffer.ByteBuf;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.components.PresentAddress;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PresentRecipientAttribute implements ItemAttribute {

    public static final MapCodec<PresentRecipientAttribute> CODEC =
            Codec.STRING.xmap(PresentRecipientAttribute::new, att -> att.recipient)
                    .fieldOf("recipient");
    public static final StreamCodec<ByteBuf, PresentRecipientAttribute> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PresentRecipientAttribute::new, att -> att.recipient);

private final String recipient;

    public PresentRecipientAttribute(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack, Level level) {
        return readRecipient(itemStack).equals(recipient);
    }

    @Override
    public ItemAttributeType getType() {
        return CreateCompat.PRESENT_ATTRIBUTE.get();
    }

    @Override
    public String getTranslationKey() {
        return "present_recipient";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{recipient};
    }

    private static String readRecipient(ItemStack itemStack) {
        PresentAddress address = itemStack.get(ModComponents.ADDRESS.get());
        if (address != null && address.isPublic()) {
            return address.recipient();
        }
        return PresentBlockTile.PUBLIC_KEY;
    }

    public static class Type implements ItemAttributeType {
        @Override
        public @NotNull ItemAttribute createAttribute() {
            return new PresentRecipientAttribute(PresentBlockTile.PUBLIC_KEY);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(ItemStack itemStack, Level level) {
            String name = readRecipient(itemStack);
            List<ItemAttribute> atts = new ArrayList<>();
            if (!name.isEmpty()) {
                atts.add(new PresentRecipientAttribute(name));
            }
            return atts;
        }

        @Override
        public MapCodec<? extends ItemAttribute> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
