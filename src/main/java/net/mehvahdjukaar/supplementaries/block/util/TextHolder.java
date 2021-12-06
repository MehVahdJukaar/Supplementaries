package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Function;

public class TextHolder implements IAntiqueTextProvider {


    private final int lines;
    //text
    private final Component[] signText;
    //text that gets rendered
    private final FormattedCharSequence[] renderText;
    private final boolean engraved;
    private DyeColor color = DyeColor.BLACK;
    private boolean hasGlowingText = false;
    private boolean hasAntiqueInk = false;

    public TextHolder(int size) {
        this(size, false);
    }
    public TextHolder(int size, boolean engraved){
        this.lines = size;
        this.renderText = new FormattedCharSequence[size];
        this.signText = new Component[size];
        Arrays.fill(this.signText, TextComponent.EMPTY);
        this.engraved = engraved;
    }

    //removing command source crap
    public void read(CompoundTag compound) {
        if (compound.contains("TextHolder")) {
            CompoundTag com = compound.getCompound("TextHolder");
            this.color = DyeColor.byName(com.getString("Color"), DyeColor.BLACK);
            this.hasGlowingText = com.getBoolean("GlowingText");
            this.hasAntiqueInk = com.getBoolean("AntiqueInk");
            for (int i = 0; i < this.lines; ++i) {
                String s = com.getString("Text" + (i + 1));
                Component mutableComponent = s.isEmpty() ? TextComponent.EMPTY : Component.Serializer.fromJson(s);
                this.signText[i] = mutableComponent;
                this.renderText[i] = null;
            }
        }
    }

    public CompoundTag write(CompoundTag compound) {
        CompoundTag com = new CompoundTag();
        com.putString("Color", this.color.getName());
        com.putBoolean("GlowingText", this.hasGlowingText);
        com.putBoolean("AntiqueInk", this.hasAntiqueInk);
        for (int i = 0; i < this.lines; ++i) {
            String s = Component.Serializer.toJson(this.signText[i]);
            com.putString("Text" + (i + 1), s);
        }
        compound.put("TextHolder", com);
        return compound;
    }

    public int size() {
        return lines;
    }

    public Component getLine(int line) {
        return this.signText[line];
    }

    public void setLine(int line, Component text) {
        Style style = this.hasAntiqueInk ? Style.EMPTY.withFont(Textures.ANTIQUABLE_FONT) : Style.EMPTY;
        text = text.copy().setStyle(style);
        this.signText[line] = text;
        this.renderText[line] = null;
    }

    public Component[] getSignText() {
        return signText;
    }

    @Nullable
    public FormattedCharSequence getRenderText(int line, Function<Component, FormattedCharSequence> f) {
        if ((this.renderText[line] == null) && this.signText[line] != TextComponent.EMPTY) {
            this.renderText[line] = f.apply(this.signText[line]);
        }
        return this.renderText[line];
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

    //should only be called server side
    public InteractionResult playerInteract(Level level, BlockPos pos, Player player, InteractionHand hand, Runnable successCallback) {
        if (player.getAbilities().mayBuild) {
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
                //else return InteractionResult.FAIL;
            } else if (item == ModRegistry.ANTIQUE_INK.get()) {
                if (!this.hasAntiqueInk) {
                    level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.setAntiqueInk(true);
                    success = true;
                }
                //else return InteractionResult.FAIL;
            } else if (item == Items.GLOW_INK_SAC) {
                if (!this.hasGlowingText) {
                    level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    this.hasGlowingText = true;
                    success = true;
                }
                //else return InteractionResult.FAIL;
            } else {
                DyeColor color = DyeColor.getColor(stack);
                if (color != null) {
                    if (this.setTextColor(color)) {
                        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        success = true;
                    }
                    //else return InteractionResult.FAIL;
                }
            }
            if (success) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    successCallback.run();
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
        for (int i = 0; i < this.signText.length; i++) {
            this.setLine(i, this.signText[i]);
        }
    }

    public boolean isEngraved() {
        return engraved;
    }
}