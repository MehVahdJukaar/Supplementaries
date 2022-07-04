package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.moonlight.platform.registry.RegHelper;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.function.Supplier;


public class ModParticles {


    //particles
    public static final Supplier<SimpleParticleType> SPEAKER_SOUND = RegHelper.registerParticle("speaker_sound");
    public static final Supplier<SimpleParticleType> GREEN_FLAME = RegHelper.registerParticle("green_flame");
    public static final Supplier<SimpleParticleType> DRIPPING_LIQUID = RegHelper.registerParticle("dripping_liquid");
    public static final Supplier<SimpleParticleType> FALLING_LIQUID = RegHelper.registerParticle("falling_liquid");
    public static final Supplier<SimpleParticleType> SPLASHING_LIQUID = RegHelper.registerParticle("splashing_liquid");
    public static final Supplier<SimpleParticleType> BOMB_EXPLOSION_PARTICLE = RegHelper.registerParticle("bomb_explosion");
    public static final Supplier<SimpleParticleType> BOMB_EXPLOSION_PARTICLE_EMITTER = RegHelper.registerParticle("bomb_explosion_emitter");
    public static final Supplier<SimpleParticleType> BOMB_SMOKE_PARTICLE = RegHelper.registerParticle("bomb_smoke");
    public static final Supplier<SimpleParticleType> BOTTLING_XP_PARTICLE = RegHelper.registerParticle("bottling_xp");
    public static final Supplier<SimpleParticleType> FEATHER_PARTICLE = RegHelper.registerParticle("feather");
    public static final Supplier<SimpleParticleType> SLINGSHOT_PARTICLE = RegHelper.registerParticle("air_burst");
    public static final Supplier<SimpleParticleType> STASIS_PARTICLE = RegHelper.registerParticle("stasis");
    public static final Supplier<SimpleParticleType> CONFETTI_PARTICLE = RegHelper.registerParticle("confetti");
    public static final Supplier<SimpleParticleType> ROTATION_TRAIL = RegHelper.registerParticle("rotation_trail");
    public static final Supplier<SimpleParticleType> ROTATION_TRAIL_EMITTER = RegHelper.registerParticle("rotation_trail_emitter");
    public static final Supplier<SimpleParticleType> SUDS_PARTICLE = RegHelper.registerParticle("suds");
    public static final Supplier<SimpleParticleType> ASH_PARTICLE = RegHelper.registerParticle("ash");
    public static final Supplier<SimpleParticleType> BUBBLE_BLOCK_PARTICLE = RegHelper.registerParticle("bubble_block");
    
}
