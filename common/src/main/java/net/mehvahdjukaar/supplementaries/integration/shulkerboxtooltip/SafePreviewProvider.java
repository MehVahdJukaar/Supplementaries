package net.mehvahdjukaar.supplementaries.integration.shulkerboxtooltip;

import com.google.common.base.Suppliers;
import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.SafeItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class SafePreviewProvider extends BlockEntityPreviewProvider {

    private static final Supplier<SafeBlockTile> DUMMY_SAFE_TILE = Suppliers.memoize(() -> new SafeBlockTile(BlockPos.ZERO,
        ModRegistry.SAFE.get().defaultBlockState()));

    public SafePreviewProvider() {
        super(27, true);
    }

    @Override
    public boolean shouldDisplay(PreviewContext context) {
        if (!super.shouldDisplay(context)) {
            return false;
        }
        ItemStack stack = context.stack();
        if (stack.getItem() instanceof SafeItem) {
            DUMMY_SAFE_TILE.get().applyComponentsFromItemStack(stack);
            return DUMMY_SAFE_TILE.get().canPlayerOpen(context.owner(), false);
        }
        return false;
    }

}
