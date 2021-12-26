package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PresentItem extends BlockItem implements IColored {

    public PresentItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            CompoundTag t = tag.getCompound("BlockEntityTag");
            if(!t.isEmpty()){
                if(t.contains("Sender")){
                    var c = PresentBlockTile.getSenderMessage(t.getString("Sender"));
                    if(c != null) components.add(c);
                }
                if(t.contains("Recipient")){
                    var c = PresentBlockTile.getRecipientMessage(t.getString("Recipient"));
                    if(c != null) components.add(c);
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
    public  Map<DyeColor, RegistryObject<Item>> getItemColorMap() {
        return ModRegistry.PRESENTS_ITEMS;
    }

    @Override
    public boolean supportsBlankColor() {
        return true;
    }
}
