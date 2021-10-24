package net.mehvahdjukaar.supplementaries.block.tiles;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.StatueBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.common.SpecialPlayers;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class StatueBlockTile extends ItemDisplayTile {
    private static GameProfileCache profileCache;
    private static MinecraftSessionService sessionService;

    public GameProfile playerProfile = null;

    //clientside
    public StatuePose pose = StatuePose.STANDING;
    public boolean isWaving = false;
    public BlockState candle = null;

    public StatueBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.STATUE_TILE.get(), pos, state);
    }

    public static void initializeSessionData(MinecraftServer server) {
        profileCache = server.getProfileCache();
        sessionService = server.getSessionService();
        //PlayerProfileCache.setOnlineMode(server.isServerInOnlineMode());
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.updateName();
    }

    private void updateName() {

        if (this.hasCustomName()) {

            String name = this.getCustomName().getString().toLowerCase();
            UUID id = SpecialPlayers.STATUES.get(name);
            if (id != null) {
                this.playerProfile = this.updateGameProfile(new GameProfile(id, name));
            }
            //ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
            //if(connection!=null)
            //this.playerInfo = connection.getPlayerInfo(SpecialPlayers.STATUES.get(this.getCustomName().getString().toLowerCase()));
        } else this.playerProfile = null;

    }

    @Nullable
    public GameProfile updateGameProfile(@Nullable GameProfile input) {
        if (input != null) {
            if (input.isComplete() && input.getProperties().containsKey("textures")) {
                return input;
            } else if (profileCache != null && sessionService != null) {
                GameProfile gameprofile = profileCache.get(input.getName()).orElseGet(null);
                if (gameprofile != null) {
                    Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), null);
                    if (property == null) {
                        gameprofile = sessionService.fillProfileProperties(gameprofile, true);
                    }
                    return gameprofile;
                }
            }
        }
        return input;
    }

    @Override
    public void updateClientVisualsOnLoad() {
        super.updateClientVisualsOnLoad();
        this.updateName();
        ItemStack stack = this.getDisplayedItem();
        this.pose = StatuePose.getPose(stack);
        this.isWaving = this.getBlockState().getValue(StatueBlock.POWERED);
        if (this.pose == StatuePose.CANDLE) {
            this.candle = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        }
    }

    public void updateTileOnInventoryChanged() {
        boolean flag = (StatuePose.getPose(this.getDisplayedItem()) == StatuePose.CANDLE);
        if (flag != this.getBlockState().getValue(StatueBlock.LIT)) {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(StatueBlock.LIT, flag));
        }
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.statuette");
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    public enum StatuePose {
        STANDING,
        HOLDING,
        CANDLE,
        SWORD,
        TOOL;

        public static StatuePose getPose(ItemStack stack) {
            if (stack.isEmpty()) return StatuePose.STANDING;
            Item i = stack.getItem();
            if (CommonUtil.isSword(i)) return SWORD;
            if (CommonUtil.isTool(i)) return TOOL;
            return (stack.is(ItemTags.CANDLES)) ? StatuePose.CANDLE : StatuePose.HOLDING;
        }
    }
}