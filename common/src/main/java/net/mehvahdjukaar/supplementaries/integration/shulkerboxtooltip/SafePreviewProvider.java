package net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class SafePreviewProvider extends BlockEntityPreviewProvider {

    public SafePreviewProvider() {
        super(27, true);
    }

    @Override
    public boolean shouldDisplay(PreviewContext context) {
        if (!super.shouldDisplay(context)) {
            return false;
        }
        ItemStack stack = context.stack();
        CompoundTag beTag = stack.getTagElement("BlockEntityTag");
        BlockEntity te = ItemsUtil.loadBlockEntityFromItem(beTag, stack.getItem());
        if (te instanceof SafeBlockTile safe) {
            return safe.canPlayerOpen(context.owner(), false);
        } else {
            return false;
        }
    }

}
