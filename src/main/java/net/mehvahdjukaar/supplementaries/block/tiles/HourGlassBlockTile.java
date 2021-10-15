package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

import static net.mehvahdjukaar.supplementaries.common.Textures.*;

public class HourGlassBlockTile extends ItemDisplayTile implements TickableBlockEntity {
    public HourGlassSandType sandType = HourGlassSandType.DEFAULT;
    public float progress = 0; //0-1 percentage of progress
    public float prevProgress = 0;
    public int power = 0;
    //client
    private TextureAtlasSprite cachedTexture = null;

    public HourGlassBlockTile() {
        super(ModRegistry.HOURGLASS_TILE.get());
    }

    @Override
    public void updateTileOnInventoryChanged() {
        this.sandType = HourGlassSandType.getHourGlassSandType(this.getDisplayedItem().getItem());
        int p = this.getDirection() == Direction.DOWN ? 1 : 0;
        int l = this.sandType.getLight();
        if (l != this.getBlockState().getValue(HourGlassBlock.LIGHT_LEVEL)) {
            level.setBlock(this.worldPosition, this.getBlockState().setValue(HourGlassBlock.LIGHT_LEVEL, l), 4 | 16);
        }
        this.prevProgress = p;
        this.progress = p;
    }

    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getOrCreateSprite() {
        if (this.cachedTexture == null) {
            this.cachedTexture = this.sandType.getSprite(this.getDisplayedItem(), this.level);
        }
        return this.cachedTexture;
    }

    @Override
    public double getViewDistance() {
        return 48;
    }

    @Override
    public void tick() {

        Direction dir = this.getDirection();
        if (!this.sandType.isEmpty()) {
            this.prevProgress = this.progress;
            if (dir == Direction.UP && this.progress != 1) {
                this.progress = Math.min(this.progress + this.sandType.increment, 1f);
            } else if (dir == Direction.DOWN && this.progress != 0) {
                this.progress = Math.max(this.progress - this.sandType.increment, 0f);
            }
        }

        if (!this.level.isClientSide) {
            int p;
            if (dir == Direction.DOWN) {
                p = (int) ((1 - this.progress) * 15f);
            } else {
                p = (int) ((this.progress) * 15f);
            }
            if (p != this.power) {
                this.power = p;
                this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            }
        }
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        int i = compound.getInt("SandType");
        this.sandType = HourGlassSandType.values()[Math.min(i, HourGlassSandType.values().length)];
        this.progress = compound.getFloat("Progress");
        this.prevProgress = compound.getFloat("PrevProgress");
        this.cachedTexture = null;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        compound.putInt("SandType", this.sandType.ordinal());
        compound.putFloat("Progress", this.progress);
        compound.putFloat("PrevProgress", this.prevProgress);
        return compound;
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.hourglass");
    }


    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty() && !HourGlassSandType.getHourGlassSandType(stack.getItem()).isEmpty();
    }

    //TODO: FIX this so it can only put from top
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


    public enum HourGlassSandType {
        DEFAULT(null, null, 0),
        SAND(SAND_TEXTURE, null, ServerConfigs.block.HOURGLASS_SAND.get()),
        CONCRETE(WHITE_CONCRETE_TEXTURE, null, ServerConfigs.block.HOURGLASS_CONCRETE.get()),
        GUNPOWDER(HOURGLASS_GUNPOWDER, Items.GUNPOWDER, ServerConfigs.block.HOURGLASS_DUST.get()),
        SUGAR(HOURGLASS_SUGAR, Items.SUGAR, ServerConfigs.block.HOURGLASS_SUGAR.get()),
        GLOWSTONE_DUST(HOURGLASS_GLOWSTONE, Items.GLOWSTONE_DUST, ServerConfigs.block.HOURGLASS_GLOWSTONE.get()),
        REDSTONE_DUST(HOURGLASS_REDSTONE, Items.REDSTONE, ServerConfigs.block.HOURGLASS_REDSTONE.get()),
        BLAZE_POWDER(HOURGLASS_BLAZE, Items.BLAZE_POWDER, ServerConfigs.block.HOURGLASS_BLAZE_POWDER.get()),
        FORGE_DUST(HOURGLASS_GUNPOWDER, null, ServerConfigs.block.HOURGLASS_DUST.get()),
        HONEY(HONEY_TEXTURE, Items.HONEY_BOTTLE, ServerConfigs.block.HOURGLASS_HONEY.get()),
        SLIME(SLIME_TEXTURE, Items.SLIME_BALL, ServerConfigs.block.HOURGLASS_SLIME.get());

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
                BakedModel ibakedmodel = itemRenderer.getModel(i, world, null);
                TextureAtlasSprite sprite = ibakedmodel.getParticleIcon();
                if (sprite instanceof MissingTextureAtlasSprite)
                    sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(this.texture);
                return sprite;

            }
            return mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(this.texture);
        }

        public static HourGlassSandType getHourGlassSandType(Item i) {
            if (i instanceof BlockItem) {
                Block b = ((BlockItem) i).getBlock();
                if (b.is(Tags.Blocks.SAND)) return SAND;
                if (b.is(ModTags.CONCRETE_POWDERS)) return CONCRETE;
            }
            for (HourGlassSandType n : HourGlassSandType.values()) {
                if (n.item == i) return n;
            }
            if (i.is(Tags.Items.DUSTS)) return FORGE_DUST;
            return DEFAULT;
        }
    }

}