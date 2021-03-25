package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TextHolder {

    public final int lines;
    //text
    public final ITextComponent[] signText;
    //text that gets rendered
    private final IReorderingProcessor[] renderText;
    public DyeColor textColor = DyeColor.BLACK;


    public TextHolder(int lines){
        this.lines = lines;
        this.renderText = new IReorderingProcessor[lines];
        this.signText = new ITextComponent[lines];
        for(int i=0; i<lines; i++){
            this.signText[i]= new StringTextComponent("");
        }
    }

    //removing command source crap
    public void read(CompoundNBT compound) {
        if(compound.contains("TextHolder")) {
            CompoundNBT com = compound.getCompound("TextHolder");
            this.textColor = DyeColor.byName(com.getString("Color"), DyeColor.BLACK);
            for (int i = 0; i < this.lines; ++i) {
                String s = com.getString("Text" + (i + 1));
                ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
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
                ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
                this.signText[i] = itextcomponent;
            }
        }

    }

    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT com = new CompoundNBT();
        com.putString("Color", this.textColor.getName());
        for (int i = 0; i < this.lines; ++i) {
            String s = ITextComponent.Serializer.toJson(this.signText[i]);
            com.putString("Text" + (i + 1), s);
        }
        compound.put("TextHolder", com);
        return compound;
    }

    //remove these for direct access?

    public ITextComponent getText(int line) {
        return this.signText[line];
    }

    public void setText(int line, ITextComponent text) {
        this.signText[line] = text;
        this.renderText[line] = null;
    }

    @Nullable
    public IReorderingProcessor getRenderText(int line, Function<ITextComponent, IReorderingProcessor> f) {
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