package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.misc.ModSoundType;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Supplier;

public class ModSounds {

    public static void init() {
    }

    ;

    //these are the names in sound.json. not actual location. this is so a sound event can play multiple sounds
    public static final Supplier<SoundEvent> TOM = regSound("block.turntable.cat");
    public static final Supplier<SoundEvent> CLOCK_TICK_1 = regSound("block.clock.tick_1");
    public static final Supplier<SoundEvent> CLOCK_TICK_2 = regSound("block.clock.tick_2");
    public static final Supplier<SoundEvent> BOMB_EXPLOSION = regSound("item.bomb");
    public static final Supplier<SoundEvent> PANCAKE_MUSIC = regSound("music.pancake");
    public static final Supplier<SoundEvent> GUNPOWDER_IGNITE = regSound("block.gunpowder.ignite");
    public static final Supplier<SoundEvent> CRANK = regSound("block.crank");

    public static final Supplier<SoundEvent> BLOCK_ROTATE = regSound("block.rotate");

    public static final Supplier<SoundEvent> WRENCH_ROTATE = regSound("item.wrench.rotate");
    public static final Supplier<SoundEvent> WRENCH_HIT = regSound("item.wrench.hit");
    public static final Supplier<SoundEvent> WRENCH_FAIL = regSound("item.wrench.fail");

    public static final Supplier<SoundEvent> PRESENT_BREAK = regSound("block.present.break");
    public static final Supplier<SoundEvent> PRESENT_PLACE = regSound("block.present.place");
    public static final Supplier<SoundEvent> PRESENT_OPEN = regSound("block.present.open");
    public static final Supplier<SoundEvent> PRESENT_PACK = regSound("block.present.pack");

    public static final Supplier<SoundEvent> SACK_BREAK = regSound("block.sack.break");
    public static final Supplier<SoundEvent> SACK_PLACE = regSound("block.sack.place");
    public static final Supplier<SoundEvent> SACK_OPEN = regSound("block.sack.open");

    public static final Supplier<SoundEvent> ROPE_BREAK = regSound("block.rope.break");
    public static final Supplier<SoundEvent> ROPE_PLACE = regSound("block.rope.place");
    public static final Supplier<SoundEvent> ROPE_SLIDE = regSound("block.rope.slide");
    public static final Supplier<SoundEvent> ROPE_STEP = regSound("block.rope.step");

    public static final Supplier<SoundEvent> BUBBLE_POP = regSound("block.bubble_block.break");
    public static final Supplier<SoundEvent> BUBBLE_PLACE = regSound("block.bubble_block.place");
    public static final Supplier<SoundEvent> BUBBLE_BLOW = regSound("item.bubble_blower");

    public static final Supplier<SoundEvent> CANNON_FIRE = regSound("block.cannon.fire");
    public static final Supplier<SoundEvent> CANNON_IGNITE = regSound("block.cannon.ignite");

    public static final Supplier<SoundEvent> CONFETTI_POPPER = regSound("item.confetti_popper");

    public static final Supplier<SoundEvent> SLIDY_BLOCK_PLACE = regSound("block.slidy_block.place");
    public static final Supplier<SoundEvent> SLIDY_BLOCK_BREAK = regSound("block.slidy_block.break");
    public static final Supplier<SoundEvent> SLIDY_BLOCK_HIt = regSound("block.slidy_block.hit");
    public static final Supplier<SoundEvent> SLIDY_BLOCK_FALL = regSound("block.slidy_block.fall");
    public static final Supplier<SoundEvent> SLIDY_BLOCK_STEP = regSound("block.slidy_block.step");
    public static final Supplier<SoundEvent> SLIDY_BLOCK_SLIDE = regSound("block.slidy_block.slide");



    public static final Supplier<SoundEvent> LUNCH_BASKET_OPEN = regSound("item.lunch_basket.open");
    public static final Supplier<SoundEvent> LUNCH_BASKET_CLOSE = regSound("item.lunch_basket.close");
    public static final Supplier<SoundEvent> LUNCH_BASKET_INSERT = regSound("item.lunch_basket.insert");

    public static final Supplier<SoundEvent> JAR_PLACE = regSound("block.jar.place");
    public static final Supplier<SoundEvent> JAR_BREAK = regSound("block.jar.break");
    public static final Supplier<SoundEvent> JAR_COOKIE = regSound("block.jar.cookie");

    public static final Supplier<SoundEvent> BELLOWS_BLOW = regSound("block.bellows.blow");
    public static final Supplier<SoundEvent> BELLOWS_RETRACT = regSound("block.bellows.retract");

    public static final Supplier<SoundEvent> GLOBE_SPIN = regSound("block.globe.spin");
    public static final Supplier<SoundEvent> FAUCET = regSound("block.faucet.turn");

    public static final Supplier<SoundEvent> SLINGSHOT_CHARGE_0 = regSound("item.slingshot.charge_0");
    public static final Supplier<SoundEvent> SLINGSHOT_CHARGE_1 = regSound("item.slingshot.charge_1");
    public static final Supplier<SoundEvent> SLINGSHOT_CHARGE_2 = regSound("item.slingshot.charge_2");
    public static final Supplier<SoundEvent> SLINGSHOT_CHARGE_3 = regSound("item.slingshot.charge_3");
    public static final Supplier<SoundEvent> SLINGSHOT_SHOOT = regSound("item.slingshot.release");

    public static final RegSupplier<SoundEvent> IMITATE_ENDERMAN = regSound("block.note_block.imitate.enderman");

    public static final ModSoundType SLIDY_BLOCK = new ModSoundType(1.0F, 1.0F,
            SLIDY_BLOCK_BREAK,
            SLIDY_BLOCK_STEP,
            SLIDY_BLOCK_PLACE,
            SLIDY_BLOCK_HIt,
            SLIDY_BLOCK_FALL);

    public static final ModSoundType JAR = new ModSoundType(1.0F, 1.0F,
            JAR_BREAK,
            () -> SoundEvents.GLASS_STEP,
            JAR_PLACE,
            () -> SoundEvents.GLASS_HIT,
            () -> SoundEvents.GLASS_FALL);

    public static final ModSoundType BUBBLE_BLOCK = new ModSoundType(1.0F, 1.0F,
            BUBBLE_POP,
            () -> SoundEvents.HONEY_BLOCK_STEP,
            BUBBLE_PLACE,
            () -> SoundEvents.HONEY_BLOCK_HIT,
            () -> SoundEvents.HONEY_BLOCK_FALL);

    public static final ModSoundType BOOKS = new ModSoundType(1.0F, 1.0F,
            () -> SoundEvents.CHISELED_BOOKSHELF_PICKUP,
            () -> SoundEvents.BOOK_PUT,
            () -> SoundEvents.CHISELED_BOOKSHELF_INSERT,
            () -> SoundEvents.BOOK_PUT,
            () -> SoundEvents.BOOK_PUT);

    public static final ModSoundType PRESENT = new ModSoundType(1.0F, 1.0F,
            PRESENT_BREAK,
            () -> SoundEvents.WOOL_STEP,
            PRESENT_PLACE,
            () -> SoundEvents.WOOL_HIT,
            () -> SoundEvents.WOOL_FALL);

    public static final ModSoundType SACK = new ModSoundType(1.0F, 1.0F,
            SACK_BREAK,
            () -> SoundEvents.WOOL_STEP,
            SACK_PLACE,
            () -> SoundEvents.WOOL_HIT,
            () -> SoundEvents.WOOL_FALL);

    public static final ModSoundType ROPE = new ModSoundType(1.0F, 1.0F,
            ROPE_BREAK,
            ROPE_STEP,
            ROPE_PLACE,
            ROPE_STEP,
            () -> SoundEvents.WOOL_FALL);


    public static RegSupplier<SoundEvent> regSound(String name) {
        return RegHelper.register(Supplementaries.res(name), () -> SoundEvent.createVariableRangeEvent(Supplementaries.res(name)), Registries.SOUND_EVENT);
    }

}
