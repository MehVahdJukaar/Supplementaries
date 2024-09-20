package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncAntiqueInk;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        var cap = SuppPlatformStuff.getForgeCap(tile, IAntiquable.class);

        boolean success = false;
        if (cap != null) {
            if (cap.isAntique() != newState) {
                cap.setAntique(newState);
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
        var cap = SuppPlatformStuff.getForgeCap(tile, IAntiquable.class);
        if (cap != null) {
            cap.setAntique(ink);
        }
    }

    public static void setAntiqueInk(ItemStack stack, boolean ink) {

        if (ink) {
            stack.set(ModComponents.ANTIQUE_INK.get(), Unit.INSTANCE);

            WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (written != null) {
                var title = written.title();
                String author = written.author();
                List<Filterable<Component>> pages = written.pages();
                List<Filterable<Component>> newPages = new ArrayList<>(pages.size());
                for (Filterable<Component> page : pages) {
                    Component comp = page.raw();
                    Optional<Component> optional = page.filtered();
                    if (comp instanceof MutableComponent mc) {
                        comp = mc.withStyle(mc.getStyle().withFont(ModTextures.ANTIQUABLE_FONT));
                    }
                    if (optional.isPresent() && optional.get() instanceof MutableComponent mc2) {
                        optional = Optional.of(mc2.withStyle(mc2.getStyle().withFont(ModTextures.ANTIQUABLE_FONT)));
                    }
                    newPages.add(new Filterable<>(comp, optional));
                }
                stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(title, author, 3, newPages, false));
            }

        } else {
            stack.remove(ModComponents.ANTIQUE_INK.get());

            WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (written != null) {
                var title = written.title();
                String author = written.author();
                int generation = written.generation();
                List<Filterable<Component>> pages = written.pages();
                List<Filterable<Component>> newPages = new ArrayList<>(pages.size());
                for (Filterable<Component> page : pages) {
                    Component comp = page.raw();
                    Optional<Component> optional = page.filtered();
                    if (comp instanceof MutableComponent mc) {
                        comp = mc.withStyle(mc.getStyle().withFont(null));
                    }
                    if (optional.isPresent() && optional.get() instanceof MutableComponent mc2) {
                        optional = Optional.of(mc2.withStyle(mc2.getStyle().withFont(null)));
                    }
                    newPages.add(new Filterable<>(comp, optional));
                }
                stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(title, author, generation, newPages, false));
            }
        }
    }

    public static boolean hasAntiqueInk(ItemStack stack) {
        return stack.get(ModComponents.ANTIQUE_INK.get()) != null;
    }
}



