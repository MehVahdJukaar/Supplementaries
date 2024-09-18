package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class AntiqueInkItem extends Item implements SignApplicator {
    public AntiqueInkItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean tryApplyToSign(Level level, SignBlockEntity signBlockEntity, boolean front, Player player) {
        return toggleAntiqueInkOnSigns(level, player, signBlockEntity.getBlockPos(), signBlockEntity, true);
    }

    public static boolean isEnabled() {
        return PlatHelper.getPlatform().isForge() && CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get();
    }


    public static boolean toggleAntiqueInkOnSigns(Level world, Player player, BlockPos pos, BlockEntity tile, boolean newState) {
        var cap = SuppPlatformStuff.getForgeCap(tile, IAntiqueTextProvider.class);

        boolean success = false;
        if (cap != null) {
            if (cap.hasAntiqueInk() != newState) {
                cap.setAntiqueInk(newState);
                tile.setChanged();
                if (world instanceof ServerLevel serverLevel) {
                    NetworkHelper.sendToAllClientPlayersInRange(serverLevel, pos, 256,
                            new ClientBoundSyncAntiqueInk(pos, newState));
                }
                success = true;
            }
        }
        if (success) {
            if (newState) {
                world.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

    public static void setAntiqueInk(BlockEntity tile, boolean ink) {
        var cap = SuppPlatformStuff.getForgeCap(tile, IAntiqueTextProvider.class);
        if (cap != null) {
            cap.setAntiqueInk(ink);
        }
    }

    public static void setAntiqueInk(ItemStack stack, boolean ink) {

        if (ink) {
            stack.getOrCreateTag().putBoolean("AntiqueInk", true);
            if ((stack.getItem() instanceof WrittenBookItem || stack.getItem() instanceof WritableBookItem)) {
                if (stack.hasTag()) {
                    ListTag listTag = stack.getTag().getList("pages", 8);
                    ListTag newListTag = new ListTag();
                    for (var v : listTag) {
                        MutableComponent comp = Component.Serializer.fromJson(v.getAsString());
                        newListTag.add(StringTag.valueOf(
                                Component.Serializer.toJson(comp.withStyle(comp.getStyle().withFont(ModTextures.ANTIQUABLE_FONT))))
                        );
                    }
                    stack.addTagElement("pages", newListTag);
                }
                if (stack.getItem() == Items.WRITTEN_BOOK) {
                    stack.getOrCreateTag().putInt("generation", 3);
                }
            }
        } else if (stack.hasTag()) {
            stack.getTag().remove("AntiqueInk");
            if (stack.hasTag() && (stack.getItem() instanceof WrittenBookItem || stack.getItem() instanceof WritableBookItem)) {
                ListTag listTag = stack.getTag().getList("pages", 8);
                ListTag newListTag = new ListTag();
                for (var v : listTag) {
                    MutableComponent comp = Component.Serializer.fromJson(v.getAsString());
                    newListTag.add(StringTag.valueOf(
                            Component.Serializer.toJson(comp.withStyle(Style.EMPTY)))
                    );
                }
                stack.addTagElement("pages", newListTag);
            }
        }
    }

    public static boolean hasAntiqueInk(ItemStack stack) {
        var t = stack.getTag();
        if (t != null) {
            return t.contains("AntiqueInk");
        }
        return false;
    }
}



