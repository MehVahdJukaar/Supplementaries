package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class SwayingBlockTile extends BlockEntity implements IExtraModelDataProvider {

    public static final ModelDataKey<Boolean> FANCY = BlockProperties.FANCY;

    //maximum allowed swing
    protected static float maxSwingAngle = 45f;
    //minimum static swing
    protected static float minSwingAngle = 2.5f;
    //max swing period
    protected static float maxPeriod = 25f;

    protected static float angleDamping = 150f;
    protected static float periodDamping = 100f;

    //all client stuff
    private float angle = 0;
    private float prevAngle = 0;

    // lower counter is used by hitting animation
    private int animationCounter = 800 + new Random().nextInt(80);
    private boolean inv = false;

    // lod stuff
    protected boolean shouldHaveTESR = false; // current
    protected boolean currentlyHasTESR = false; // old
    private int ticksToSwitchMode = 0;

    public SwayingBlockTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public boolean isAlwaysFast() {
        return ClientConfigs.Blocks.FAST_LANTERNS.get();
    }

    //called when data is actually refreshed
    @Override
    public ExtraModelData getExtraModelData() {
        this.ticksToSwitchMode = 2;
        return ExtraModelData.builder()
                .with(FANCY, this.shouldHaveTESR)
                .build();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void setFancyRenderer(boolean fancy) {
        if (this.isAlwaysFast()) fancy = false;
        if (fancy != this.shouldHaveTESR) {
            this.currentlyHasTESR = this.shouldHaveTESR;
            this.shouldHaveTESR = fancy;
            //model data doesn't like other levels. linked to crashes with other mods
            if (this.level == Minecraft.getInstance().level) {
                this.requestModelReload();
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
            }
            if (!fancy) this.animationCounter = 800;
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean shouldRenderFancy() {
        if (this.currentlyHasTESR != this.shouldHaveTESR && !this.currentlyHasTESR) {
            //makes tesr wait 1 render cycle,
            // so it's in sync with model data refreshVisuals
            this.currentlyHasTESR = true;
        }
        return currentlyHasTESR;
    }

    public static void clientTick(Level pLevel, BlockPos pPos, BlockState pState, SwayingBlockTile tile) {

        if (tile.currentlyHasTESR != tile.shouldHaveTESR && tile.currentlyHasTESR && tile.ticksToSwitchMode > 0) {
            tile.ticksToSwitchMode--;
            if (tile.ticksToSwitchMode == 0) {
                //makes tesr wait 1 render cycle,
                // so it's in sync with model data refreshVisuals
                tile.currentlyHasTESR = false;
            }
        }

        if (tile.shouldRenderFancy()) {

            //TODO: improve physics (water, swaying when it's not exposed to wind)

            tile.animationCounter++;

            double timer = tile.animationCounter;
            if (pState.getValue(WaterBlock.WATERLOGGED)) timer /= 2d;

            tile.prevAngle = tile.angle;
            //actually they are the inverse of damping. increase them to have less damping

            float a = minSwingAngle;
            float k = 0.01f;
            if (timer < 800) {
                a = (float) Math.max(maxSwingAngle * Math.exp(-(timer / angleDamping)), minSwingAngle);
                k = (float) Math.max(Math.PI * 2 * (float) Math.exp(-(timer / periodDamping)), 0.01f);
            }

            tile.angle = a * Mth.cos((float) ((timer / maxPeriod) - k));
            tile.angle *= tile.inv ? -1 : 1;
            // this.angle = 90*(float)
            // Math.cos((float)counter/40f)/((float)this.counter/20f);;
        }
    }

    public float getSwingAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevAngle, this.angle);
    }

    //rotation axis rotate 90 deg
    public abstract Vec3i getNormalRotationAxis(BlockState state);

    public void hitByEntity(Entity entity, BlockState state, BlockPos pos) {
        Vec3 mot = entity.getDeltaMovement();
        if (mot.length() > 0.05) {

            Vec3 norm = new Vec3(mot.x, 0, mot.z).normalize();
            Vec3i dv = this.getNormalRotationAxis(state);
            Vec3 vec = new Vec3(dv.getX(), 0, dv.getZ()).normalize();
            double dot = norm.dot(vec);
            if (dot != 0) {
                this.inv = dot < 0;
            }
            if (Math.abs(dot) > 0.4) {
                if (this.animationCounter > 10) {
                    //TODO: fix this doesnt work because this only works client side
                    Player player = entity instanceof Player p ? p : null;
                    if (state.getBlock() instanceof HangingSignBlock) {
                        //hardcoding goes brr
                        //TODO: replace with proper custom sound & sound event
                        entity.getLevel().playSound(player, pos, SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.5f, 2);
                        entity.getLevel().playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.75f, 2f);

                    } else {
                        entity.getLevel().playSound(player, pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS, 0.75f, 1.5f);

                    }
                }
                this.animationCounter = 0;
            }
        }
    }

    public boolean isFlipped() {
        return false;
    }

}
