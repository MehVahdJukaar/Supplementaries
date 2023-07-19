package net.mehvahdjukaar.supplementaries.block.tiles;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.StatueBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.Credits;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public class StatueBlockTile extends ItemDisplayTile {

    @Nullable
    public GameProfile owner = null;

    //clientside
    private StatuePose pose = StatuePose.STANDING;
    private boolean isWaving = false;
    private BlockState candle = null;

    public StatueBlockTile() {
        super(ModRegistry.STATUE_TILE.get());
    }

    @Override
    public void setCustomName(ITextComponent name) {
        super.setCustomName(name);
        this.updateName();
    }

    public StatuePose getPose() {
        return pose;
    }

    public boolean isWaving() {
        return isWaving;
    }

    public BlockState hasCandle() {
        return candle;
    }

    //skull code
    public void setOwner(@Nullable GameProfile input) {
        if (this.owner == null) {
            this.owner = input;

            this.owner = SkullTileEntity.updateGameprofile(this.owner);
            this.setChanged();
        }
    }

    private void updateName() {

        if (this.hasCustomName()) {

            String name = this.getCustomName().getString().toLowerCase(Locale.ROOT);
            Pair<UUID, String> profile = Credits.INSTANCE.statues().get(name);
            if (profile != null) {
                this.setOwner(new GameProfile(profile.getFirst(), profile.getSecond()));
            }
            //ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
            //if(connection!=null)
            //this.playerInfo = connection.getPlayerInfo(SpecialPlayers.STATUES.get(this.getCustomName().getString().toLowerCase(Locale.ROOT)));
        } else this.owner = null;

    }

    @Override
    public void updateClientVisualsOnLoad() {
        this.updateName();
        ItemStack stack = this.getDisplayedItem();
        this.pose = StatuePose.getPose(stack);
        this.isWaving = this.getBlockState().getValue(StatueBlock.POWERED);
    }

    @Override
    public void updateTileOnInventoryChanged() {
    }


    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.statuette");
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    public enum StatuePose {
        STANDING,
        HOLDING,
        CANDLE,
        SWORD,
        TOOL,
        GLOBE,
        SEPIA_GLOBE;

        public static StatuePose getPose(ItemStack stack) {
            if (stack.isEmpty()) return StatuePose.STANDING;
            Item i = stack.getItem();
            if (CommonUtil.isSword(i)) return SWORD;
            if (CommonUtil.isTool(i)) return TOOL;
            if (i == ModRegistry.GLOBE_ITEM.get()) return GLOBE;
            return StatuePose.HOLDING;
        }

        public boolean isGlobe() {
            return this == GLOBE || this == SEPIA_GLOBE;
        }
    }
}