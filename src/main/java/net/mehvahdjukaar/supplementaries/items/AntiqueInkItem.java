package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.capabilities.SupplementariesCapabilities;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AntiqueInkItem extends Item {
    public AntiqueInkItem(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        if(true){
            AtomicBoolean success = new AtomicBoolean(false);
            BlockPos pos = context.getClickedPos();
            TileEntity tile = world.getBlockEntity(pos);
            if(tile != null) {

                LazyOptional<IAntiqueTextProvider> cap = tile.getCapability(SupplementariesCapabilities.ANTIQUE_TEXT_CAP);
                cap.ifPresent(c ->{
                    c.setAntiqueInk(true);
                    success.set(true);
                }
                );

                if(!success.get()) {

                    CompoundNBT tag = tile.save(new CompoundNBT());

                    Optional<CompoundNBT> newTag = modifyAllTextRecursive(tag, Textures.ANTIQUABLE_FONT, tag.copy());
                    newTag.ifPresent(t -> {
                        BlockState state = world.getBlockState(pos);
                        tile.load(state, newTag.get());
                        tile.setChanged();
                        //world.sendBlockUpdated(pos, state, state, 2);
                        success.set(true);
                    });
                }
            }
            if(success.get()) return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.SUCCESS;
    }

    private Optional<CompoundNBT> modifyAllTextRecursive(CompoundNBT tag, ResourceLocation font, CompoundNBT newTag) {
        boolean modified = false;
        Set<String> keys = tag.getAllKeys();
        for (String k : keys) {
            INBT nbt = tag.get(k);
            if (nbt instanceof CompoundNBT) {
                modified = this.modifyAllTextRecursive((CompoundNBT) nbt, font, newTag).isPresent();
            } else if (nbt instanceof StringNBT) {
                String s = nbt.getAsString();
                if (s.contains("{\"text\":")) {
                    IFormattableTextComponent text = ITextComponent.Serializer.fromJson(s);
                    if (text != null) {
                        ITextComponent newText = this.applyFontRecursive(text, font);

                        String newString = ITextComponent.Serializer.toJson(newText);
                        newTag.remove(k);
                        newTag.putString(k, newString);
                        modified = true;
                    }
                }
            }
        }
        if(modified){
            return Optional.of(newTag);
        }
        return Optional.empty();
    }

    private ITextComponent applyFontRecursive(ITextComponent text, ResourceLocation font) {
        if (text instanceof IFormattableTextComponent) {
            ((IFormattableTextComponent) text).setStyle(text.getStyle().withFont(font));
        }
        text.getSiblings().forEach(sib -> applyFontRecursive(sib, font));
        return text;
    }
}
