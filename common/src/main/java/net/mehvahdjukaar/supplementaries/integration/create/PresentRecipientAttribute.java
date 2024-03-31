package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PresentRecipientAttribute implements ItemAttribute {
    public static final PresentRecipientAttribute EMPTY = new PresentRecipientAttribute(PresentBlockTile.PUBLIC_KEY);

    String recipient;

    public PresentRecipientAttribute(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return readRecipient(itemStack).equals(recipient);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        String name = readRecipient(itemStack);
        List<ItemAttribute> atts = new ArrayList<>();
        if(name.length() > 0) {
            atts.add(new PresentRecipientAttribute(name));
        }
        return atts;
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
    public void writeNBT(CompoundTag compoundTag) {
        compoundTag.putString("recipient", this.recipient);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag compoundTag) {
        return new PresentRecipientAttribute(compoundTag.getString("recipient"));

    }
    private String readRecipient(ItemStack itemStack) {
        String name;
        if (itemStack.getItem() instanceof PresentItem) {
            var t = itemStack.getTagElement("BlockEntityTag");
            if (t != null){
                name = t.getString("Recipient");
                if (name != PresentBlockTile.PUBLIC_KEY)
                {
                    return name;
                }
            }
        }
        return "";
    }
}
