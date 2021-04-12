package net.mehvahdjukaar.supplementaries.block.tiles;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class FlagBlockTile extends TileEntity implements ITickableTileEntity {

    //client side param
    public final float offset = 3f * (MathHelper.sin(this.worldPosition.getX()) + MathHelper.sin(this.worldPosition.getZ()));
    public float counter = 0;

    @Nullable
    private DyeColor baseColor = DyeColor.WHITE;
    @Nullable
    private ListNBT itemPatterns;
    private boolean receivedData;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public FlagBlockTile() {
        super(Registry.FLAG_TILE.get());
    }

    public FlagBlockTile(DyeColor color) {
        this();
        this.baseColor = color;
    }

    public static ResourceLocation getFlagLocation(BannerPattern pattern){
        return new ResourceLocation(Supplementaries.MOD_ID, "textures/entity/flags/"+ pattern.getFilename()+".png");
    }

    @OnlyIn(Dist.CLIENT)
    public void fromItem(ItemStack stack, DyeColor color) {
        this.itemPatterns = getItemPatterns(stack);
        this.baseColor = color;
        this.patterns = null;
        this.receivedData = true;
    }

    @OnlyIn(Dist.CLIENT)
    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null && this.receivedData) {
            this.patterns = createPatterns(this.getBaseColor(this::getBlockState), this.itemPatterns);
        }

        return this.patterns;
    }

    //static stuff. might remove

    @Nullable
    public static ListNBT getItemPatterns(ItemStack stack) {
        return BannerTileEntity.getItemPatterns(stack);
    }

    public static int getPatternCount(ItemStack stack) {
        return BannerTileEntity.getPatternCount(stack);
    }

    @OnlyIn(Dist.CLIENT)
    public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor color, @Nullable ListNBT nbt) {
        return BannerTileEntity.createPatterns(color, nbt);
    }

    public static void removeLastPattern(ItemStack stack) {
        BannerTileEntity.removeLastPattern(stack);
    }

    //


    public ItemStack getItem(BlockState state) {
        ItemStack itemstack = new ItemStack(FlagBlock.byColor(this.getBaseColor(() -> state)));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            itemstack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }
        return itemstack;
    }

    public DyeColor getBaseColor(Supplier<BlockState> state) {
        if (this.baseColor == null) {
            this.baseColor = ((FlagBlock)state.get().getBlock()).getColor();
        }
        return this.baseColor;
    }

    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        super.save(compoundNBT);
        if (this.itemPatterns != null) {
            compoundNBT.put("Patterns", this.itemPatterns);
        }
        return compoundNBT;
    }

    @Override
    public void load(BlockState state, CompoundNBT compoundNBT) {
        super.load(state, compoundNBT);

        if (this.hasLevel()) {
            this.baseColor = ((FlagBlock)this.getBlockState().getBlock()).getColor();
        } else {
            this.baseColor = null;
        }
        this.itemPatterns = compoundNBT.getList("Patterns", 10);
        this.patterns = null;
        this.receivedData = true;
    }


    @Override
    public double getViewDistance() {
        return 96;
    }

    //TODO: do this in renderer
    @Override
    public void tick() {

        if(!this.level.isClientSide){
            int b = 1;
        }
        else{
            int c = 1;
        }

        if(this.level.isClientSide) {
            //TODO:cache?
            //TODO: make long or float. wind vane too
            this.counter = (this.level.getGameTime()%24000)+offset;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        Direction dir = this.getDirection();
        return new AxisAlignedBB(0.25,0, 0.25, 0.75, 1, 0.75).expandTowards(
                dir.getStepX()*1.35f,0,dir.getStepZ()*1.35f).move(this.worldPosition);
    }



    public Direction getDirection() {
        return this.getBlockState().getValue(FlagBlock.FACING);
    }


}