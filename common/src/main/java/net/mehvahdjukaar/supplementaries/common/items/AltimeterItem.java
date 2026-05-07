package net.mehvahdjukaar.supplementaries.common.items;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AltimeterItem extends Item {

    public AltimeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        VibeChecker.assertSameLevel(level, player);
        if (level.isClientSide && ClientConfigs.Items.DEPTH_METER_CLICK.get()) {
            Position pp = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) Vec3.atLowerCornerOf(player.blockPosition()));

            player.displayClientMessage(Component.translatable("message.supplementaries.altimeter", pp.y()), true);
            player.swing(usedHand);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return super.isEnabled(enabledFeatures);
    }
}
