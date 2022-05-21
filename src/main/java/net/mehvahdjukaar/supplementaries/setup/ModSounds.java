package net.mehvahdjukaar.supplementaries.setup;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.registries.RegistryObject;

import static net.mehvahdjukaar.supplementaries.setup.RegistryHelper.regSound;

public class ModSounds {

    //these are the names in sound.json. not actual location. this is so a sound event can play multiple sounds
    public static final RegistryObject<SoundEvent> TOM = regSound("block.turntable.cat");
    public static final RegistryObject<SoundEvent> CLOCK_TICK_1 = regSound("block.clock.tick_1");
    public static final RegistryObject<SoundEvent> CLOCK_TICK_2 = regSound("block.clock.tick_2");
    public static final RegistryObject<SoundEvent> BOMB_EXPLOSION = regSound("item.bomb");
    public static final RegistryObject<SoundEvent> PANCAKE_MUSIC = regSound("music.pancake");
    public static final RegistryObject<SoundEvent> GUNPOWDER_IGNITE = regSound("block.gunpowder.ignite");
    public static final RegistryObject<SoundEvent> CRANK = regSound("block.crank");

    public static final RegistryObject<SoundEvent> BLOCK_ROTATE = regSound("block.rotate");

    public static final RegistryObject<SoundEvent> WRENCH_ROTATE = regSound("item.wrench.rotate");
    public static final RegistryObject<SoundEvent> WRENCH_HIT = regSound("item.wrench.hit");
    public static final RegistryObject<SoundEvent> WRENCH_FAIL = regSound("item.wrench.fail");

    public static final RegistryObject<SoundEvent> PRESENT_BREAK = regSound("block.present.break");
    public static final RegistryObject<SoundEvent> PRESENT_PLACE = regSound("block.present.place");
    public static final RegistryObject<SoundEvent> PRESENT_OPEN = regSound("block.present.open");
    public static final RegistryObject<SoundEvent> PRESENT_PACK = regSound("block.present.pack");

    public static final RegistryObject<SoundEvent> SACK_BREAK = regSound("block.sack.break");
    public static final RegistryObject<SoundEvent> SACK_PLACE = regSound("block.sack.place");
    public static final RegistryObject<SoundEvent> SACK_OPEN = regSound("block.sack.open");

    public static final RegistryObject<SoundEvent> ROPE_BREAK = regSound("block.rope.break");
    public static final RegistryObject<SoundEvent> ROPE_PLACE = regSound("block.rope.place");
    public static final RegistryObject<SoundEvent> ROPE_SLIDE = regSound("block.rope.slide");
    public static final RegistryObject<SoundEvent> ROPE_STEP = regSound("block.rope.step");

    public static final RegistryObject<SoundEvent> BUBBLE_POP = regSound("block.bubble_block.break");
    public static final RegistryObject<SoundEvent> BUBBLE_PLACE = regSound("block.bubble_block.place");
    public static final RegistryObject<SoundEvent> BUBBLE_BLOW = regSound("item.bubble_blower");

    public static final RegistryObject<SoundEvent> JAR_PLACE = regSound("block.jar.place");
    public static final RegistryObject<SoundEvent> JAR_BREAK = regSound("block.jar.break");
    public static final RegistryObject<SoundEvent> JAR_COOKIE = regSound("block.jar.cookie");

    public static final RegistryObject<SoundEvent> BELLOWS_BLOW = regSound("block.bellows.blow");
    public static final RegistryObject<SoundEvent> BELLOWS_RETRACT = regSound("block.bellows.retract");

    public static final RegistryObject<SoundEvent> GLOBE_SPIN = regSound("block.globe.spin");
    public static final RegistryObject<SoundEvent> FAUCET = regSound("block.faucet.turn");

    public static final RegistryObject<SoundEvent> SLINGSHOT_CHARGE_0 = regSound("item.slingshot.charge_0");
    public static final RegistryObject<SoundEvent> SLINGSHOT_CHARGE_1 = regSound("item.slingshot.charge_1");
    public static final RegistryObject<SoundEvent> SLINGSHOT_CHARGE_2 = regSound("item.slingshot.charge_2");
    public static final RegistryObject<SoundEvent> SLINGSHOT_CHARGE_3 = regSound("item.slingshot.charge_3");
    public static final RegistryObject<SoundEvent> SLINGSHOT_SHOOT = regSound("item.slingshot.release");

    public static final SoundType JAR = new ForgeSoundType(1.0F, 1.0F,
            JAR_BREAK,
            () -> SoundEvents.GLASS_STEP,
            JAR_PLACE,
            () -> SoundEvents.GLASS_HIT,
            () -> SoundEvents.GLASS_FALL);

    public static final SoundType BUBBLE_BLOCK = new ForgeSoundType(1.0F, 1.0F,
            BUBBLE_POP,
            () -> SoundEvents.HONEY_BLOCK_STEP,
            BUBBLE_PLACE,
            () -> SoundEvents.HONEY_BLOCK_HIT,
            () -> SoundEvents.HONEY_BLOCK_FALL);

    public static final SoundType PRESENT = new ForgeSoundType(1.0F, 1.0F,
            PRESENT_BREAK,
            () -> SoundEvents.WOOL_STEP,
            PRESENT_PLACE,
            () -> SoundEvents.WOOL_HIT,
            () -> SoundEvents.WOOL_FALL);

    public static final SoundType SACK = new ForgeSoundType(1.0F, 1.0F,
            SACK_BREAK,
            () -> SoundEvents.WOOL_STEP,
            SACK_PLACE,
            () -> SoundEvents.WOOL_HIT,
            () -> SoundEvents.WOOL_FALL);

    public static final SoundType ROPE = new ForgeSoundType(1.0F, 1.0F,
            ROPE_BREAK,
            ROPE_STEP,
            ROPE_PLACE,
            ROPE_STEP,
            () -> SoundEvents.WOOL_FALL);

}
