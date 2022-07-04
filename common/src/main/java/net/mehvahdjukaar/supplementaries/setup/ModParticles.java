package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.moonlight.platform.registry.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.Supplier;


public class ModParticles {

    public static void init(){};
    
    //particles
    public static final Supplier<SimpleParticleType> SPEAKER_SOUND = reg("speaker_sound");
    public static final Supplier<SimpleParticleType> GREEN_FLAME = reg("green_flame");
    public static final Supplier<SimpleParticleType> DRIPPING_LIQUID = reg("dripping_liquid");
    public static final Supplier<SimpleParticleType> FALLING_LIQUID = reg("falling_liquid");
    public static final Supplier<SimpleParticleType> SPLASHING_LIQUID = reg("splashing_liquid");
    public static final Supplier<SimpleParticleType> BOMB_EXPLOSION_PARTICLE = reg("bomb_explosion");
    public static final Supplier<SimpleParticleType> BOMB_EXPLOSION_PARTICLE_EMITTER = reg("bomb_explosion_emitter");
    public static final Supplier<SimpleParticleType> BOMB_SMOKE_PARTICLE = reg("bomb_smoke");
    public static final Supplier<SimpleParticleType> BOTTLING_XP_PARTICLE = reg("bottling_xp");
    public static final Supplier<SimpleParticleType> FEATHER_PARTICLE = reg("feather");
    public static final Supplier<SimpleParticleType> SLINGSHOT_PARTICLE = reg("air_burst");
    public static final Supplier<SimpleParticleType> STASIS_PARTICLE = reg("stasis");
    public static final Supplier<SimpleParticleType> CONFETTI_PARTICLE = reg("confetti");
    public static final Supplier<SimpleParticleType> ROTATION_TRAIL = reg("rotation_trail");
    public static final Supplier<SimpleParticleType> ROTATION_TRAIL_EMITTER = reg("rotation_trail_emitter");
    public static final Supplier<SimpleParticleType> SUDS_PARTICLE = reg("suds");
    public static final Supplier<SimpleParticleType> ASH_PARTICLE = reg("ash");
    public static final Supplier<SimpleParticleType> BUBBLE_BLOCK_PARTICLE = reg("bubble_block");
    
    
    private static Supplier<SimpleParticleType> reg(String string){
        return RegHelper.registerParticle(Supplementaries.res(string));
    }
}
