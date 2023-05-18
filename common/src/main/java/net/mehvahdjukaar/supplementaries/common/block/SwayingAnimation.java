package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.CeilingBannerBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.function.Function;

public class SwayingAnimation {

    //maximum allowed swing
    protected static float maxSwingAngle = 45f;
    //minimum static swing
    protected static float minSwingAngle = 2.5f;
    //max swing period
    protected static float maxPeriod = 25f;

    protected static float angleDamping = 150f;
    protected static float periodDamping = 100f;
    private final Function<BlockState, Vec3i> axisFunc;

    //all client stuff
    private float angle = 0;
    private float prevAngle = 0;

    // lower counter is used by hitting animation
    private int animationCounter = 800 + new Random().nextInt(80);
    private boolean inv = false;


    public SwayingAnimation(Function<BlockState, Vec3i> getRotationAxis) {
        this.axisFunc = getRotationAxis;
    }

    public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {

        //TODO: improve physics (water, swaying when it's not exposed to wind)

        this.animationCounter++;

        double timer = this.animationCounter;
        if (pState.getValue(WaterBlock.WATERLOGGED)) timer /= 2d;

        this.prevAngle = this.angle;
        //actually they are the inverse of damping. increase them to have less damping

        float a = minSwingAngle;
        float k = 0.01f;
        if (timer < 800) {
            a = (float) Math.max(maxSwingAngle * Math.exp(-(timer / angleDamping)), minSwingAngle);
            k = (float) Math.max(Math.PI * 2 * (float) Math.exp(-(timer / periodDamping)), 0.01f);
        }

        this.angle = a * Mth.cos((float) ((timer / maxPeriod) - k));
        this.angle *= this.inv ? -1 : 1;
        // this.angle = 90*(float)
        // Math.cos((float)counter/40f)/((float)this.counter/20f);;

    }

    public float getSwingAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevAngle, this.angle);
    }


    public void hitByEntity(Entity entity, BlockState state, BlockPos pos) {
        Vec3 mot = entity.getDeltaMovement();
        if (mot.length() > 0.05) {

            Vec3 norm = new Vec3(mot.x, 0, mot.z).normalize();
            Vec3i dv = this.axisFunc.apply(state);
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
}
