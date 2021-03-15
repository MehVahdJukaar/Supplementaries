package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatuetteBlockTile extends ItemDisplayTile implements ITickableTileEntity {
    private NetworkPlayerInfo playerInfo = null;
    public StatuetteBlockTile() {
        super(Registry.PEDESTAL_TILE.get());
    }

    //hijacking this method to work with hoppers & multiplayer
    @Override
    public void markDirty() {
        //this.updateServerAndClient();
        this.updateTile();
        super.markDirty();
    }

    @Override
    public void setCustomName(ITextComponent name) {
        super.setCustomName(name);

    }

    @Nullable
    public NetworkPlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(UUIDWhitelist.whitelist.get(0));
        }
        return this.playerInfo;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos);
    }

    public void updateTile() {
        //TODO: rewrite this
    }

    //TODO: put yaw inside blockstate so it can be rotated
    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        return compound;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.statuette");
    }

    @Override
    public void tick() {

    }

    private static class UUIDWhitelist{
        protected static final List<UUID> whitelist = new ArrayList<>();
        static{
            whitelist.add(new UUID(0,0));
        }
    }
}