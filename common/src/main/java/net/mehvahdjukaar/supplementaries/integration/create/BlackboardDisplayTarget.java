package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.BlackboardItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.regex.Pattern;

public class BlackboardDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        BlockEntity te = context.getTargetBlockEntity();
        if (te instanceof BlackboardBlockTile tile && text.size() > 0 && !tile.isWaxed()) {
            var source = context.getSourceBlockEntity();
            if (!parseText(text.get(0).getString(), tile)) {
                ItemStack copyStack = CreateCompat.getDisplayedItem(context, source, i -> i.getItem() instanceof BlackboardItem);
                if (!copyStack.isEmpty() && copyBlackboard(line, context, te, tile, copyStack)) return;
                var pixels = BlackboardBlockTile.unpackPixelsFromStringWhiteOnly(text.get(0).getString());
                tile.setPixels(BlackboardBlockTile.unpackPixels(pixels));
            }
            context.level().sendBlockUpdated(context.getTargetPos(), te.getBlockState(), te.getBlockState(), 2);
            reserve(line, te, context);
        }
    }


    private static final Pattern PATTERN = Pattern.compile("\\((\\d\\d?),(\\d\\d?)\\)->(\\S+)");

    private boolean parseText(String string, BlackboardBlockTile tile) {
        var m = PATTERN.matcher(string);
        if (m.matches()) {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            DyeColor dye = DyeColor.byName(m.group(3), null);
            if (x >= 0 && x <= 15 && y >= 0 && y <= 15 && dye != null) {
                if (dye != DyeColor.WHITE && dye != DyeColor.BLACK && !CommonConfigs.Building.BLACKBOARD_COLOR.get())
                    return false;
                tile.setPixel(x, y, BlackboardBlock.colorToByte(dye));
                return true;
            }
        }
        return false;
    }

    private static boolean copyBlackboard(int line, DisplayLinkContext context, BlockEntity te, BlackboardBlockTile tile, ItemStack stack) {
        CompoundTag cmp = stack.getTagElement("BlockEntityTag");
        if (cmp != null && cmp.contains("Pixels")) {
            tile.setPixels(BlackboardBlockTile.unpackPixels(cmp.getLongArray("Pixels")));
            context.level().sendBlockUpdated(context.getTargetPos(), te.getBlockState(), te.getBlockState(), 2);
            reserve(line, te, context);
            return true;
        }
        return false;
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(1, 32, this);
    }
}
