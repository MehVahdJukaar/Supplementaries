package net.mehvahdjukaar.supplementaries.common.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class TextHolder implements IAntiqueTextProvider {

    private final int lines;
    //text
    private final Component[] textLines;
    //text that gets rendered
    private final FormattedCharSequence[] renderText;
    private final int maxWidth;
    private DyeColor color = DyeColor.BLACK;
    private boolean hasGlowingText = false;
    private boolean hasAntiqueInk = false;

    public TextHolder(int size, int maxWidth) {
        this.lines = size;
        this.maxWidth = maxWidth;
        this.renderText = new FormattedCharSequence[size];
        this.textLines = new Component[size];
        Arrays.fill(this.textLines, CommonComponents.EMPTY);
    }

    public int getMaxLineCharacters() {
        return (int) (getMaxLineVisualWidth() / 6f);
    }

    public int getMaxLineVisualWidth() {
        return maxWidth;
    }

    //removing command source crap
    public void load(CompoundTag compound) {
        if (compound.contains("TextHolder")) {
            CompoundTag com = compound.getCompound("TextHolder");
            this.color = DyeColor.byName(com.getString("Color"), DyeColor.BLACK);
            this.hasGlowingText = com.getBoolean("GlowingText");
            this.hasAntiqueInk = com.getBoolean("AntiqueInk");
            for (int i = 0; i < this.lines; ++i) {
                String s = com.getString("Text" + (i + 1));
                Component mutableComponent = s.isEmpty() ? CommonComponents.EMPTY : Component.Serializer.fromJson(s);
                this.textLines[i] = mutableComponent;
                this.renderText[i] = null;
            }
        }
    }

    public CompoundTag save(CompoundTag compound) {
        CompoundTag com = new CompoundTag();
        com.putString("Color", this.color.getName());
        com.putBoolean("GlowingText", this.hasGlowingText);
        com.putBoolean("AntiqueInk", this.hasAntiqueInk);
        for (int i = 0; i < this.lines; ++i) {
            String s = Component.Serializer.toJson(this.textLines[i]);
            com.putString("Text" + (i + 1), s);
        }
        compound.put("TextHolder", com);
        return compound;
    }

    public int size() {
        return lines;
    }

    public Component getLine(int line) {
        return this.textLines[line];
    }

    public void setLine(int line, Component text) {
        MutableComponent t = text.copy();
        if (this.hasAntiqueInk) {
            t.setStyle(text.getStyle().withFont(ModTextures.ANTIQUABLE_FONT));
        }
        this.textLines[line] = t;
        this.renderText[line] = null;
    }

    @Deprecated
    public void setLine(int line, Component text, Style style) {
        setLine(line, text);
    }

    public Component[] getTextLines() {
        return textLines;
    }

    public DyeColor getColor() {
        return color;
    }

    public boolean setTextColor(DyeColor newColor) {
        if (newColor != this.color) {
            this.color = newColor;
            return true;
        }
        return false;
    }

    public boolean hasGlowingText() {
        return hasGlowingText;
    }

    public void setGlowingText(boolean glowing) {
        this.hasGlowingText = glowing;
    }

    //TODO: make server sided & send block updated
    //should only be called server side
    public InteractionResult playerInteract(Level level, BlockPos pos, Player player, InteractionHand hand, BlockEntity tile) {
        if (Utils.mayBuild(player,hit.getBlockPos())) {
            ItemStack stack = player.getItemInHand(hand);
            Item item = stack.getItem();
            boolean success = false;
            if (item == Items.INK_SAC) {
                if (this.hasGlowingText || this.hasAntiqueInk) {
                    level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.setAntiqueInk(false);
                    this.hasGlowingText = false;
                    success = true;
                }
            } else if (item == ModRegistry.ANTIQUE_INK.get()) {
                if (!this.hasAntiqueInk) {
                    level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.setAntiqueInk(true);
                    success = true;
                }
            } else if (item == Items.GLOW_INK_SAC) {
                if (!this.hasGlowingText) {
                    level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.hasGlowingText = true;
                    success = true;
                }
            } else {
                DyeColor dyeColor = ForgeHelper.getColor(stack);
                if (dyeColor != null) {
                    if (this.setTextColor(dyeColor)) {
                        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        success = true;
                    }
                }
            }
            if (success) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    tile.setChanged();
                    level.sendBlockUpdated(pos, tile.getBlockState(), tile.getBlockState(), 3);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hasAntiqueInk() {
        return this.hasAntiqueInk;
    }

    @Override
    public void setAntiqueInk(boolean hasInk) {
        this.hasAntiqueInk = hasInk;
        for (int i = 0; i < this.textLines.length; i++) {
            this.setLine(i, this.textLines[i]);
        }
    }

    //TODO: finish notice boards dye thing
    public void clearEffects() {
        this.setTextColor(DyeColor.BLACK);
        this.setAntiqueInk(false);
        this.setGlowingText(false);
    }

    public boolean isEmpty() {
        return Arrays.stream(this.textLines).allMatch(s -> s.getString().isEmpty());
    }

    public void clear() {
        Arrays.fill(this.textLines, CommonComponents.EMPTY);
        this.clearEffects();
    }

    //client stuff

    @Environment(EnvType.CLIENT)
    @Nullable
    public FormattedCharSequence getAndPrepareTextForRenderer(Font font, int line) {
        if ((this.renderText[line] == null) && this.textLines[line] != CommonComponents.EMPTY) {
            List<FormattedCharSequence> list = font.split(this.textLines[line], this.getMaxLineVisualWidth());
            this.renderText[line] = list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        }
        return this.renderText[line];
    }

    @Environment(EnvType.CLIENT)
    public TextUtil.RenderTextProperties getRenderTextProperties(int combinedLight, Supplier<Boolean> shouldShowGlow) {
        return new TextUtil.RenderTextProperties(this.getColor(), this.hasGlowingText(), combinedLight,
                this.hasAntiqueInk() ? Style.EMPTY.withFont(ModTextures.ANTIQUABLE_FONT) : Style.EMPTY, shouldShowGlow);
    }

    @Environment(EnvType.CLIENT)
    public TextUtil.RenderTextProperties getGUIRenderTextProperties() {
        return getRenderTextProperties(LightTexture.FULL_BRIGHT, () -> true);
    }


}