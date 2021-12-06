package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.SwayingBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public abstract class SwayingBlockTile extends BlockEntity {
    public float angle = 0;
    public float prevAngle = 0;
    // lower counter is used by hitting animation
    public int counter = 800 + new Random().nextInt(80);
    public boolean inv = false;

    protected static float maxSwingAngle = 45f;
    protected static float minSwingAngle = 2.5f;
    protected static float maxPeriod = 25f;
    protected static float angleDamping = 150f;
    protected static float periodDamping = 100f;

    //lod stuff
    //protected boolean TESRCalled = false;
    public boolean fancyRenderer = false;
    protected boolean oldRendererState = false;

    public SwayingBlockTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(SwayingBlock.FACING);
    }

    public void setFancyRenderer(boolean fancy) {
        if (ClientConfigs.cached.FAST_LANTERNS) fancy = false;
        if (fancy != this.fancyRenderer) {
            this.oldRendererState = this.fancyRenderer;
            this.fancyRenderer = fancy;
            //model data doesn't like other levels. linked to crashes with other mods
            if (this.level == Minecraft.getInstance().level) {
                this.requestModelDataUpdate();
                //TODO: replace hardcoded int with const.blockFlags
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);

            }
        }
    }

    public boolean shouldRenderFancy() {
        if (oldRendererState != fancyRenderer) {
            //makes tesr wait 1 render cycle,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        so it's in sync with model data refresh
            this.oldRendererState = this.fancyRenderer;
            return oldRendererState;
        }
        return fancyRenderer;
    }

    public boolean hasAnimation() {
        return true;
    }

    public static void clientTick(Level pLevel, BlockPos pPos, BlockState pState, SwayingBlockTile tile) {
        if (tile.hasAnimation()) {

            //TODO: improve physics
            tile.counter++;

            tile.prevAngle = tile.angle;
            //actually they are the inverse of damping. increase them to have less damping

            float a = minSwingAngle;
            float k = 0.01f;
            if (tile.counter < 800) {
                a = (float) Math.max(maxSwingAngle * Math.pow(Math.E, -(tile.counter / angleDamping)), minSwingAngle);
                k = (float) Math.max(Math.PI * 2 * (float) Math.pow(Math.E, -(tile.counter / periodDamping)), 0.01f);
            }

            tile.angle = a * Mth.cos((tile.counter / maxPeriod) - k);
            tile.angle *= tile.inv ? -1 : 1;
            // this.angle = 90*(float)
            // Math.cos((float)counter/40f)/((float)this.counter/20f);;
        }
    }

}
