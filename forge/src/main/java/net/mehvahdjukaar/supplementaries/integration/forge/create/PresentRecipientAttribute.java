package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.mehvahdjukaar.supplementaries.common.items.DispenserMinecartItem;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PresentRecipientAttribute implements ItemAttribute {

    public static final PresentRecipientAttribute EMPTY = new PresentRecipientAttribute(PresentBlockTile.PUBLIC_KEY);

    private final String recipient;

    public PresentRecipientAttribute(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        if (itemStack.getItem() instanceof PresentItem) {
            var t = itemStack.getTagElement("block_entity_tag");
            if (t != null) return t.contains("Recipient");
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        var t = itemStack.getTagElement("block_entity_tag");
        if (t != null) {
            var v = t.getString("Recipient");
            if (!v.isEmpty()) return List.of(new PresentRecipientAttribute(v));
        }
        return List.of();
    }

    @Override
    public String getTranslationKey() {
        return "present_recipient";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[]{this.recipient};
    }

    @Override
    public void writeNBT(CompoundTag compoundTag) {
        compoundTag.putString("Recipient", recipient);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag compoundTag) {
        return compoundTag.contains("Recipient") ? new PresentRecipientAttribute(compoundTag.getString("Recipient")) : EMPTY;

    }
}
