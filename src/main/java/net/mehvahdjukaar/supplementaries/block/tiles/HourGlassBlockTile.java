package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

import static net.mehvahdjukaar.supplementaries.common.Textures.*;

public class HourGlassBlockTile extends ItemDisplayTile implements ITickableTileEntity {
    public HourGlassSandType sandType = HourGlassSandType.DEFAULT;
    public float progress = 0; //0-1 percentage of progress
    public float prevProgress = 0;
    public int power = 0;
    //client
    private TextureAtlasSprite cachedTexture = null;
    public HourGlassBlockTile() {
        super(Registry.HOURGLASS_TILE.get());
    }

    //hijacking this method to work with hoppers
    @Override
    public void markDirty() {
        //this.updateServerAndClient();
        this.updateTile();
        super.markDirty();
    }

    public void updateTile(){
        this.sandType = HourGlassSandType.getHourGlassSandType(this.getStackInSlot(0).getItem());
        int p = this.getDirection()==Direction.DOWN?1:0;
        int l = this.sandType.getLight();
        if(l!=this.getBlockState().get(HourGlassBlock.LIGHT_LEVEL)){
            world.setBlockState(this.pos, this.getBlockState().with(HourGlassBlock.LIGHT_LEVEL,l),4|16);
        }
        this.prevProgress=p;
        this.progress=p;
    }


    public TextureAtlasSprite getOrCreateSprite(){
        if(this.cachedTexture==null){
            this.cachedTexture = this.sandType.getSprite(this.getStackInSlot(0).getItem());
        }
        return this.cachedTexture;
    }

    //TODO:!!! tick seems to be called 3 times more often on server side wtf
    @Override
    public void tick() {

        Direction dir = this.getDirection();
        if(!this.sandType.isEmpty()){
            this.prevProgress = this.progress;
            if(dir==Direction.UP && this.progress != 1){
                this.progress = Math.min(this.progress + this.sandType.increment, 1f);
            }
            else if(dir==Direction.DOWN && this.progress != 0){
                this.progress = Math.max(this.progress - this.sandType.increment, 0f);
            }
        }

        if(!this.world.isRemote){
            int p;
            if(dir==Direction.DOWN) {
                p = (int) ((1-this.progress) * 15f);
            }
            else{
                p = (int) ((this.progress) * 15f);
            }
            if(p!=this.power){
                this.power=p;
                this.world.updateComparatorOutputLevel(this.pos,this.getBlockState().getBlock());
            }
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.sandType = HourGlassSandType.values()[compound.getInt("SandType")];
        this.progress = compound.getFloat("Progress");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("SandType", this.sandType.ordinal());
        compound.putFloat("Progress", this.progress);
        return compound;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.hourglass");
    }


    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return this.isEmpty() && !HourGlassSandType.getHourGlassSandType(stack.getItem()).isEmpty();
    }

    //TODO: FIX this so it can only put from top
    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getSizeInventory()).toArray();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
        if(direction==Direction.UP) {
            return this.isItemValidForSlot(0,stack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        Direction dir = this.getBlockState().get(HourGlassBlock.FACING);
        return (dir==Direction.UP && this.progress==1)||(dir==Direction.DOWN && this.progress==0);
    }

    public Direction getDirection(){
        return this.getBlockState().get(HourGlassBlock.FACING);
    }


    public enum HourGlassSandType {
        DEFAULT(null,null,0),
        SAND(SAND_TEXTURE,"minecraft:sand", 60),
        RED_SAND(RED_SAND_TEXTURE,"minecraft:red_sand", 60),
        WHITE_CONCRETE(WHITE_CONCRETE_TEXTURE,"minecraft:white_concrete_powder", 90),
        ORANGE_CONCRETE(ORANGE_CONCRETE_TEXTURE,"minecraft:orange_concrete_powder", 90),
        LIGHT_BLUE_CONCRETE(LIGHT_BLUE_CONCRETE_TEXTURE,"minecraft:light_blue_concrete_powder", 90),
        YELLOW_CONCRETE(YELLOW_CONCRETE_TEXTURE,"minecraft:yellow_concrete_powder", 90),
        LIME_CONCRETE(LIME_CONCRETE_TEXTURE,"minecraft:lime_concrete_powder", 90),
        GREEN_CONCRETE(GREEN_CONCRETE_TEXTURE,"minecraft:green_concrete_powder",90),
        PINK_CONCRETE(PINK_CONCRETE_TEXTURE,"minecraft:pink_concrete_powder", 90),
        GRAY_CONCRETE(GRAY_CONCRETE_TEXTURE,"minecraft:gray_concrete_powder", 90),
        LIGHT_GRAY_CONCRETE(LIGHT_GRAY_CONCRETE_TEXTURE,"minecraft:light_gray_concrete_powder", 90),
        CYAN_CONCRETE(CYAN_CONCRETE_TEXTURE,"minecraft:cyan_concrete_powder", 90),
        PURPLE_CONCRETE(PURPLE_CONCRETE_TEXTURE,"minecraft:purple_concrete_powder", 90),
        BLUE_CONCRETE(BLUE_CONCRETE_TEXTURE,"minecraft:blue_concrete_powder", 90),
        BROWN_CONCRETE(BROWN_CONCRETE_TEXTURE,"minecraft:brown_concrete_powder", 90),
        RED_CONCRETE(RED_CONCRETE_TEXTURE,"minecraft:red_concrete_powder", 90),
        BLACK_CONCRETE(BLACK_CONCRETE_TEXTURE,"minecraft:black_concrete_powder", 90),
        MAGENTA_CONCRETE(MAGENTA_CONCRETE_TEXTURE,"minecraft:magenta_concrete_powder", 90),
        GUNPOWDER(HOURGLASS_GUNPOWDER,"minecraft:gunpowder", 150),
        SUGAR(HOURGLASS_SUGAR,"minecraft:sugar", 40),
        GLOWSTONE_DUST(HOURGLASS_GLOWSTONE,"minecraft:glowstone_dust", 120),
        REDSTONE_DUST(HOURGLASS_REDSTONE,"minecraft:redstone", 200),
        BLAZE_POWDER(HOURGLASS_BLAZE,"minecraft:blaze_powder", 100),
        FORGE_DUST(HOURGLASS_GUNPOWDER,"minecraft:gunpowder", 150);

        public final ResourceLocation texture;
        public final String name;
        public final float increment;

        HourGlassSandType(ResourceLocation texture, String name, int t){
            this.texture = texture;
            this.name = name;
            this.increment =1f/(float)t;
        }
        public boolean isEmpty(){return this==DEFAULT;}

        public int getLight(){
            if(this==GLOWSTONE_DUST)return 9;
            if(this==BLAZE_POWDER)return 6;
            return 0;
        }

        public TextureAtlasSprite getSprite(Item i){
            ResourceLocation reg = i.getRegistryName();
            if(this==SAND||this==FORGE_DUST){
                ResourceLocation texture;
                TextureAtlasSprite sprite;
                if(this==SAND) {
                    texture = new ResourceLocation(reg.getNamespace(), "block/" + reg.getPath());
                    sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
                }
                else{
                    if(reg.getNamespace().equals("thermal")){
                        texture = new ResourceLocation(reg.getNamespace(), "item/dusts/" + reg.getPath());
                        sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
                    }
                    else {
                        texture = new ResourceLocation(reg.getNamespace(), "item/" + reg.getPath());
                        sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
                        if (sprite instanceof MissingTextureSprite) {
                            texture = new ResourceLocation(reg.getNamespace(), "items/" + reg.getPath());
                            sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
                        }
                    }
                }
                if(sprite instanceof MissingTextureSprite)sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(this.texture);
                return sprite;
            }
            return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(this.texture);
        }

        public boolean isSand(){
            return this==SAND;
        }

        public static HourGlassSandType getHourGlassSandType(Item i){
            if(i instanceof BlockItem && ((BlockItem) i).getBlock().isIn(Tags.Blocks.SAND))return SAND;
            String name = i.getRegistryName().toString();
            for (HourGlassSandType n : HourGlassSandType.values()){
                if(name.equals(n.name)){
                    return n;
                }
            }
            if(name.equals("astralsorcery:stardust"))return FORGE_DUST;
            if(i.isIn(Tags.Items.DUSTS))return FORGE_DUST;
            return DEFAULT;
        }
    }

}