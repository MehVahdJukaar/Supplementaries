package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.AltimeterItemRenderer;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class AltimeterItem extends Item implements ICustomItemRendererProvider {
    private static final String TAG_CALCULATED = "quark:altimeter_calculated";

    public AltimeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide && ClientConfigs.Items.DEPTH_METER_CLICK.get()) {
            player.displayClientMessage(Component.translatable("message.supplementaries.altimeter", player.blockPosition().getY()), true);
            player.swing(usedHand);
        }
        return super.use(level, player, usedHand);
    }


    @Override
    public Supplier<ItemStackRenderer> getRendererFactory() {
        return AltimeterItemRenderer::new;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if(CompatHandler.QUARK && QuarkCompat.hasCompassNerf()) {
            CompoundTag tag = stack.getOrCreateTag();
            if(!tag.contains(TAG_CALCULATED)){
                tag.putBoolean(TAG_CALCULATED, true);
            }
        }
    }

    public static boolean isInInventory(ItemStack stack){
        return !CompatHandler.QUARK || (QuarkCompat.hasCompassNerf() && stack.hasTag() && stack.getTag().getBoolean(TAG_CALCULATED));
    }
}
