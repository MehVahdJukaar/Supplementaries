package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.block.util.IMapDisplay;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.inventories.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;


public class NoticeBoardBlockTile extends ItemDisplayTile implements INameable, IMapDisplay {
    private String txt = null;
    private int fontScale = 1;
    private DyeColor textColor = DyeColor.BLACK;
    private List<IReorderingProcessor> cachedPageLines = Collections.emptyList();
    //used to tell renderer when it has to slit new line(have to do it there cause i need fontrenderer function)
    private boolean inventoryChanged = true;
    // private int packedFrontLight =0;
    public boolean textVisible = true; //for culling
    private ITextComponent customName;

    public NoticeBoardBlockTile() {
        super(Registry.NOTICE_BOARD_TILE.get());
    }

    @Override
    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getCustomName() {
        return this.customName;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.notice_board");
    }

    //update blockstate and plays sound
    public void updateBoardBlock(boolean b) {

        BlockState _bs = this.level.getBlockState(this.worldPosition);
        if(_bs.getValue(BlockStateProperties.HAS_BOOK)!=b){
            this.level.setBlock(this.worldPosition, _bs.setValue(BlockStateProperties.HAS_BOOK,b), 2);
            if(b){
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.85F);
            }
            else{
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.50F);
            }
        }
    }

    //hijacking this method to work with hoppers
    @Override
    public void setChanged() {
        this.updateTile();
       //this.updateServerAndClient();
        super.setChanged();
    }

    @Override
    public ItemStack getMapStack(){
        return this.getItem(0);
    }

    public void updateTile() {
        //updateTextVisibility();
        if(this.level != null && !this.level.isClientSide()){
            ItemStack itemstack = getItem(0);
            Item item = itemstack.getItem();
            String s = null;
            this.inventoryChanged = true;
            this.cachedPageLines = Collections.emptyList();

            if (item instanceof  WrittenBookItem) {
                CompoundNBT com = itemstack.getTag();
                if(WrittenBookItem.makeSureTagIsValid(com)){

                    ListNBT listnbt = com.getList("pages", 8).copy();
                    s = listnbt.getString(0);
                }
            }
            else if(item instanceof  WritableBookItem){
                CompoundNBT com = itemstack.getTag();
                if(WritableBookItem.makeSureTagIsValid(com)){

                    ListNBT listnbt = com.getList("pages", 8).copy();
                    s = listnbt.getString(0);
                }
            }


            if (s != null) {
                //this.inventoryChanged = true;
                this.txt = s;
                this.updateBoardBlock(true);
            }
            else {
                this.txt = null;
                this.updateBoardBlock(false);
            }
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (compound.contains("CustomName", 8)) {
            this.customName = ITextComponent.Serializer.fromJson(compound.getString("CustomName"));
        }
        this.txt = compound.getString("Text");
        this.fontScale = compound.getInt("FontScale");
        //TODO: rework this
        this.inventoryChanged = compound.getBoolean("invchanged");
        this.textColor = DyeColor.byName(compound.getString("Color"), DyeColor.BLACK);
        this.textVisible = compound.getBoolean("TextVisible");


        // this.packedFrontLight = compound.getInt("light");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        if (this.txt != null) {
            compound.putString("Text", this.txt);
        }
        compound.putInt("FontScale", this.fontScale);
        compound.putBoolean("invchanged", this.inventoryChanged);
        compound.putString("Color", this.textColor.getName());
        compound.putBoolean("TextVisible", this.textVisible);

        // compound.putInt("light", this.packedFrontLight);

        return compound;
    }


    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return new NoticeBoardContainer(id, player,this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if(!stack.isEmpty()&&this.isEmpty()&&ServerConfigs.cached.NOTICE_BOARDS_UNRESTRICTED)return true;
        return (this.isEmpty()&&((ItemTags.LECTERN_BOOKS!=null&&stack.getItem().is(ItemTags.LECTERN_BOOKS))|| stack.getItem() instanceof FilledMapItem));
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    //TODO: remove some of these
    public DyeColor getTextColor() {
        return this.textColor;
    }

    public boolean setTextColor(DyeColor newColor) {
        if (newColor != this.getTextColor()) {
            this.textColor = newColor;
            return true;
        } else {
            return false;
        }
    }

    public void setFontScale(int s) {
        this.fontScale = s;
    }

    public void setChachedPageLines(List<IReorderingProcessor> l) {
        this.cachedPageLines = l;
    }

    public List<IReorderingProcessor> getCachedPageLines() {
        return this.cachedPageLines;
    }

    public int getFontScale() {
        return this.fontScale;
    }

    public boolean getFlag() {
        if (this.inventoryChanged) {
            this.inventoryChanged = false;
            return true;
        }
        return false;
    }

    public Direction getDirection(){
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    public float getYaw() {
        return -this.getDirection().toYRot();
    }

    public boolean getAxis() {
        Direction d = this.getDirection();
        return d == Direction.NORTH || d == Direction.SOUTH;
    }

    public int getFrontLight() {
        return WorldRenderer.getLightColor(this.level, this.worldPosition.relative(this.getDirection()));
    }

    public String getText() {
        if (this.txt != null) {
            return this.txt;
        } else {
            return "";
        }
    }
}