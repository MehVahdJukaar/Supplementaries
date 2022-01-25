package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.IColored;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PresentItem extends BlockItem implements IColored {

    public PresentItem(Block block, Properties properties) {
        super(block, properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> components, ITooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            CompoundNBT t = tag.getCompound("BlockEntityTag");
            if (!t.isEmpty()) {
                if (t.contains("Sender")) {
                    ITextComponent c = PresentBlockTile.getSenderMessage(t.getString("Sender"));
                    if (c != null) components.add(c);
                }
                if (t.contains("Recipient")) {
                    ITextComponent c = PresentBlockTile.getRecipientMessage(t.getString("Recipient"));
                    if (c != null) components.add(c);
                }
            }
        }
    }

    @Override
    public DyeColor getColor() {
        return ((PresentBlock) this.getBlock()).getColor();
    }

    @Nullable
    @Override
    public Map<DyeColor, RegistryObject<Item>> getItemColorMap() {
        return ModRegistry.PRESENTS_ITEMS;
    }

    @Override
    public boolean supportsBlankColor() {
        return true;
    }
}
