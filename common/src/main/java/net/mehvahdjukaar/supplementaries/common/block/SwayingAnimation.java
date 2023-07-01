package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Random;
import java.util.function.Function;

public class SwayingAnimation extends SwingAnimation {

    //maximum allowed swing
    protected static float maxSwingAngle = 45f;
    //minimum static swing
    protected static float minSwingAngle = 2.5f;
    //max swing period
    protected static float maxPeriod = 25f;

    protected static float angleDamping = 150f;
    protected static float periodDamping = 100f;


    // lower counter is used by hitting animation
    private int animationCounter = 800 + new Random().nextInt(80);
    private boolean inv = false;


    public SwayingAnimation(Function<BlockState, Vector3f> getRotationAxis) {
        super(getRotationAxis);
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {

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

    @Override
    public float getAngle(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevAngle, this.angle);
    }

    @Override
    public void reset() {
        animationCounter = 800;
    }

    @Override
    public boolean hitByEntity(Entity entity, BlockState state, BlockPos pos) {
        Vec3 mot = entity.getDeltaMovement();
        if (mot.length() > 0.05) {

            Vec3 norm = new Vec3(mot.x, 0, mot.z).normalize();
            Vec3 vec = new Vec3(this.getRotationAxis(state));
            double dot = norm.dot(vec);
            if (dot != 0) {
                this.inv = dot < 0;
            }
            if (Math.abs(dot) > 0.4) {
                if (this.animationCounter > 10) {
                    //TODO: fix this doesnt work because this only works client side
                    Player player = entity instanceof Player p ? p : null;
                    entity.level().playSound(player, pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS, 0.75f, 1.5f);
                }
                this.animationCounter = 0;
            }
        }
        return true;
    }
}
