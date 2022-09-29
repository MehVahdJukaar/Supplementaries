package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StatueBlock;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

public class StatueBlockTile extends ItemDisplayTile {

    @Nullable
    public GameProfile owner = null;

    //clientside
    public StatuePose pose = StatuePose.STANDING;
    public boolean isWaving = false;
    public BlockState candle = null;

    public StatueBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.STATUE_TILE.get(), pos, state);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.updateName();
    }

    //skull code
    public void setOwner(@Nullable GameProfile input) {
        if (this.owner == null) {
            if (input == null || !(input.isComplete() && input.getProperties().containsKey("textures"))) {
                synchronized (this) {
                    this.owner = input;
                }

                SkullBlockEntity.updateGameprofile(this.owner, (gameProfile) -> {
                    this.owner = gameProfile;
                    //this.setChanged();
                });
            }
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
        if (this.pose == StatuePose.CANDLE) {
            Block b = ((BlockItem) stack.getItem()).getBlock();
            if (!(b instanceof CandleBlock)) {
                b = Blocks.CANDLE;
            }
            this.candle = b.defaultBlockState().setValue(CandleBlock.LIT, true);
        }
    }

    @Override
    public void updateTileOnInventoryChanged() {
        boolean flag = (StatuePose.getPose(this.getDisplayedItem()) == StatuePose.CANDLE);
        if (flag != this.getBlockState().getValue(StatueBlock.LIT)) {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(StatueBlock.LIT, flag));
        }
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
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.statuette");
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
            if (i == ModRegistry.GLOBE_SEPIA_ITEM.get()) return SEPIA_GLOBE;
            return (stack.is(ItemTags.CANDLES)) ? StatuePose.CANDLE : StatuePose.HOLDING;
        }

        public boolean isGlobe() {
            return this == GLOBE || this == SEPIA_GLOBE;
        }
    }
}