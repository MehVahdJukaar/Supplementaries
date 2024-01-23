package net.mehvahdjukaar.supplementaries.integration.forge.create;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.mehvahdjukaar.supplementaries.common.items.DispenserMinecartItem;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PresentRecipientAttribute implements ItemAttribute {
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
            if 
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
        return new Object[]{this.recipient};
    }

    @Override
    public void writeNBT(CompoundTag compoundTag) {
        compoundTag.putString("Recipient", recipient);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag compoundTag) {
        return new PresentRecipientAttribute(compoundTag.getString("Recipient"));

    }
    private String readRecipient(ItemStack stack) {
        String name;
        if (itemStack.getItem() instanceof PresentItem) {
            var t = itemStack.getTagElement("block_entity_tag");
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
