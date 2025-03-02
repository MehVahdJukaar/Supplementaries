package net.mehvahdjukaar.supplementaries.integration.create;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PresentRecipientAttribute implements ItemAttribute {
    public static final PresentRecipientAttribute EMPTY = new PresentRecipientAttribute(PresentBlockTile.PUBLIC_KEY);

    String recipient;

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

    @Override
    public void save(CompoundTag compoundTag) {
        compoundTag.putString("recipient", this.recipient);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.recipient = compoundTag.getString("recipient");

    }

    private static String readRecipient(ItemStack itemStack) {
        String name;
        if (itemStack.getItem() instanceof PresentItem) {
            var t = itemStack.getTagElement("BlockEntityTag");
            if (t != null) {
                name = t.getString("Recipient");
                if (!name.equals(PresentBlockTile.PUBLIC_KEY)) {
                    return name;
                }
            }
        }
        return "";
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

    }
}
