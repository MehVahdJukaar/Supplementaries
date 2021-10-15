package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TextHolder {

    public final int size;
    //text
    public final Component[] signText;
    //text that gets rendered
    private final FormattedCharSequence[] renderText;
    public DyeColor textColor = DyeColor.BLACK;


    public TextHolder(int size){
        this.size = size;
        this.renderText = new FormattedCharSequence[size];
        this.signText = new Component[size];
        for(int i = 0; i< size; i++){
            this.signText[i]= new TextComponent("");
        }
    }

    //removing command source crap
    public void read(CompoundTag compound) {
        if(compound.contains("TextHolder")) {
            CompoundTag com = compound.getCompound("TextHolder");
            this.textColor = DyeColor.byName(com.getString("Color"), DyeColor.BLACK);
            for (int i = 0; i < this.size; ++i) {
                String s = com.getString("Text" + (i + 1));
                Component itextcomponent = Component.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
                this.signText[i] = itextcomponent;
                this.renderText[i] = null;
            }
        }


        //remove in the future
        if(compound.contains("Color"))
            this.textColor = DyeColor.byName(compound.getString("Color"), DyeColor.BLACK);
        for(int i = 0; i < 2; ++i) {
            if(compound.contains("Text" + (i + 1))) {
                String s = compound.getString("Text" + (i + 1));
                Component itextcomponent = Component.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
                this.signText[i] = itextcomponent;
            }
        }

    }

    public CompoundTag write(CompoundTag compound) {
        CompoundTag com = new CompoundTag();
        com.putString("Color", this.textColor.getName());
        for (int i = 0; i < this.size; ++i) {
            String s = Component.Serializer.toJson(this.signText[i]);
            com.putString("Text" + (i + 1), s);
        }
        compound.put("TextHolder", com);
        return compound;
    }

    //remove these for direct access?

    public Component getText(int line) {
        return this.signText[line];
    }

    public void setText(int line, Component text) {
        this.signText[line] = text;
        this.renderText[line] = null;
    }

    @Nullable
    public FormattedCharSequence getRenderText(int line, Function<Component, FormattedCharSequence> f) {
        if (this.renderText[line] == null && this.signText[line] != null) {
            this.renderText[line] = f.apply(this.signText[line]);
        }
        return this.renderText[line];
    }

    public boolean setTextColor(DyeColor newColor) {
        if (newColor != this.textColor) {
            this.textColor = newColor;
            return true;
        }
        return false;
    }

}