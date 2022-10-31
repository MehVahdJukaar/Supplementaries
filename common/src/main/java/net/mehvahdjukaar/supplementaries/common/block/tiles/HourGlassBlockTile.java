package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

import static net.mehvahdjukaar.supplementaries.reg.ModTextures.*;

public class HourGlassBlockTile extends ItemDisplayTile {
    private HourGlassSandType sandType = HourGlassSandType.DEFAULT;
    private float progress = 0; //0-1 percentage of progress
    private float prevProgress = 0;
    private int power = 0;
    //client
    private TextureAtlasSprite cachedTexture = null;

    public HourGlassBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.HOURGLASS_TILE.get(), pos, state);
    }

    @Override
    public void updateTileOnInventoryChanged() {
        this.sandType = HourGlassSandType.getHourGlassSandType(this.getDisplayedItem().getItem());
        int p = this.getDirection() == Direction.DOWN ? 1 : 0;
        int l = this.sandType.getLight();
        if (l != this.getBlockState().getValue(HourGlassBlock.LIGHT_LEVEL)) {
            if (this.level != null)
                level.setBlock(this.worldPosition, this.getBlockState().setValue(HourGlassBlock.LIGHT_LEVEL, l), 4 | 16);
        }
        this.prevProgress = p;
        this.progress = p;
    }

    public HourGlassSandType getSandType() {
        return sandType;
    }

    public float getProgress(float partialTicks) {
       return Mth.lerp(partialTicks, this.prevProgress, this.progress);
    }

    public TextureAtlasSprite getOrCreateSprite() {
        if (this.cachedTexture == null) {
            this.cachedTexture = this.sandType.getSprite(this.getDisplayedItem(), this.level);
        }
        return this.cachedTexture;
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, HourGlassBlockTile tile) {
        Direction dir = pState.getValue(HourGlassBlock.FACING);
        if (!tile.sandType.isEmpty()) {
            tile.prevProgress = tile.progress;
            if (dir == Direction.UP && tile.progress != 1) {
                tile.progress = Math.min(tile.progress + tile.sandType.increment, 1f);
            } else if (dir == Direction.DOWN && tile.progress != 0) {
                tile.progress = Math.max(tile.progress - tile.sandType.increment, 0f);
            }
        }

        if (!pLevel.isClientSide) {
            int p;
            if (dir == Direction.DOWN) {
                p = (int) ((1 - tile.progress) * 15f);
            } else {
                p = (int) ((tile.progress) * 15f);
            }
            if (p != tile.power) {
                tile.power = p;
                pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
            }
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        int i = compound.getInt("SandType");
        this.sandType = HourGlassSandType.values()[Math.min(i, HourGlassSandType.values().length)];
        this.progress = compound.getFloat("Progress");
        this.prevProgress = compound.getFloat("PrevProgress");
        this.cachedTexture = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("SandType", this.sandType.ordinal());
        tag.putFloat("Progress", this.progress);
        tag.putFloat("PrevProgress", this.prevProgress);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.hourglass");
    }


    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty() && !HourGlassSandType.getHourGlassSandType(stack.getItem()).isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (direction == Direction.UP) {
            return this.canPlaceItem(0, stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        Direction dir = this.getBlockState().getValue(HourGlassBlock.FACING);
        return (dir == Direction.UP && this.progress == 1) || (dir == Direction.DOWN && this.progress == 0);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(HourGlassBlock.FACING);
    }

    public int getPower() {
        return power;
    }

    public enum HourGlassSandType {
        DEFAULT(null, null, 0),
        SAND(SAND_TEXTURE, null, CommonConfigs.Blocks.HOURGLASS_SAND.get()),
        CONCRETE(WHITE_CONCRETE_TEXTURE, null, CommonConfigs.Blocks.HOURGLASS_CONCRETE.get()),
        GUNPOWDER(HOURGLASS_GUNPOWDER, Items.GUNPOWDER, CommonConfigs.Blocks.HOURGLASS_DUST.get()),
        SUGAR(ModTextures.SUGAR, Items.SUGAR, CommonConfigs.Blocks.HOURGLASS_SUGAR.get()),
        GLOWSTONE_DUST(HOURGLASS_GLOWSTONE, Items.GLOWSTONE_DUST, CommonConfigs.Blocks.HOURGLASS_GLOWSTONE.get()),
        REDSTONE_DUST(HOURGLASS_REDSTONE, Items.REDSTONE, CommonConfigs.Blocks.HOURGLASS_REDSTONE.get()),
        BLAZE_POWDER(HOURGLASS_BLAZE, Items.BLAZE_POWDER, CommonConfigs.Blocks.HOURGLASS_BLAZE_POWDER.get()),
        FORGE_DUST(HOURGLASS_GUNPOWDER, null, CommonConfigs.Blocks.HOURGLASS_DUST.get()),
        HONEY(HONEY_TEXTURE, Items.HONEY_BOTTLE, CommonConfigs.Blocks.HOURGLASS_HONEY.get()),
        SLIME(SLIME_TEXTURE, Items.SLIME_BALL, CommonConfigs.Blocks.HOURGLASS_SLIME.get()),
        ASH(ModTextures.ASH, ModRegistry.ASH_BLOCK.get().asItem(), CommonConfigs.Blocks.HOURGLASS_DUST.get());

        @Nullable
        public final ResourceLocation texture;
        @Nullable
        public final Item item;
        public final float increment;

        HourGlassSandType(ResourceLocation texture, Item item, int t) {
            this.texture = texture;
            this.item = item;
            this.increment = 1f / (float) t;
        }

        public boolean isEmpty() {
            return this == DEFAULT;
        }

        public int getLight() {
            if (this == GLOWSTONE_DUST) return 9;
            if (this == BLAZE_POWDER) return 6;
            return 0;
        }

        public TextureAtlasSprite getSprite(ItemStack i, Level world) {
            Minecraft mc = Minecraft.getInstance();
            if (this == FORGE_DUST || this == SAND || this == CONCRETE) {
                ItemRenderer itemRenderer = mc.getItemRenderer();
                BakedModel model = itemRenderer.getModel(i, world, null, 0);
                TextureAtlasSprite sprite = model.getParticleIcon();
                if (sprite instanceof MissingTextureAtlasSprite)
                    sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(this.texture);
                return sprite;

            }
            return mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(this.texture);
        }

        public static HourGlassSandType getHourGlassSandType(Item i) {
            if (i instanceof BlockItem bi) {
                Block b = bi.getBlock();
                if (i.builtInRegistryHolder().is(ModTags.SANDS)) return SAND;
                if (b.builtInRegistryHolder().is(ModTags.CONCRETE_POWDERS)) return CONCRETE;
            }
            for (HourGlassSandType n : HourGlassSandType.values()) {
                if (n.item == i) return n;
            }
            if (i.builtInRegistryHolder().is(ModTags.DUSTS)) return FORGE_DUST;
            return DEFAULT;
        }
    }

}