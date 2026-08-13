# Custom Sound Wishlist

Candidate blocks / items / entities across **Supplementaries, Snowy Spirit, Vista, Haunted Harvest, Sleep Tight**
that would benefit from custom sounds, either because they're **silent** while doing something, or because they
**reuse a vanilla sound that doesn't fit**.

Grouped by severity (impact × how wrong it currently is). Things that are genuinely fine with vanilla sounds
(plain crops like corn, plain decorative wood/stone variants, plain food, blocks that already have a fitting
custom sound) are deliberately excluded.

Legend: 🔇 = no sound today · 🔀 = wrong/generic vanilla sound today

---

## ⭐ TIER 1: Flagship / iconic, high ROI

These are signature, front-and-center features that are currently silent or clearly mismatched.

- 🔇 **Supplementaries · Wind Vane**: continuously rotates for wind/weather, totally silent. → soft creaking/squeaking
  metal weathervane loop, pitch tied to spin speed. *(user-requested)*
- 🔇 **Snowy Spirit · Snow Globe**: right-click flips the snowing state + snow visuals, no sound. The mod's most
  emblematic block, silent. → wind-up music-box chime / gentle snow-shaker rattle.
- 🔀 **Supplementaries · Safe**: key-locked vault opens/closes with the plain `IRON_TRAPDOOR_OPEN/CLOSE`. Biggest
  mismatch on a flagship block. → heavy safe-door swing + latch/combination clunk.
- 🔇 **Vista · Viewfinder focus / shutter**: left/right-click through the camera locks/unlocks the view (the shutter
  gesture), silent. → focus-lock / half-press shutter click (distinct lock vs unlock). *(user-requested: camera)*
- 🔀 **Vista · Viewfinder zoom**: scroll-to-zoom plays `UI_BUTTON_CLICK`, with a literal `//TODO: proper sound here` in
  the code. → lens zoom whir / zoom-ring tick.
- 🔇 **Haunted Harvest · Villager jump-scare ("spook")**: trick reaction spawns particles + panics the villager, no
  audio. Signature Halloween moment. → ghostly "boo" / scare stinger.
- 🔇 **Haunted Harvest · Candy handoff (trick-or-treat reward)**: villager throws candy to the player; the `WITCH_THROW`
  line is commented out → silent. Core reward loop. → candy toss / wrapper rustle.
- 🔀 **Haunted Harvest · Popcorn popping**: `popCorn()` uses `FIREWORK_ROCKET_BLAST`; kernels sound like fireworks. →
  dedicated kernel-pop burst.
- 🔀 **Snowy Spirit · Gingerbread Golem (Gingy)**: no `getHurt/getDeath/getAmbient` overrides → generic mob combat SFX on
  an iconic character. → dry gingerbread crunch/snap (reuse the existing gingerbread `hit`/`break` base).
- 🔇/🔀 **Sleep Tight · Bedbugs**: the `bedbug.ambient/hurt/death` events exist but are just **remapped silverfish**
  samples; burrowing into a bed uses `WOOL_HIT`/`WOOL_BREAK`. Lowest-effort win (registry already done, just swap .ogg +
  burrow loop). → real insect chitter/squish + fabric-burrow loop. *(user-requested: bedbugs)*

---

## 🟧 TIER 2: High impact

Prominent interactions, silent or noticeably wrong.

- 🔇 **Supplementaries · Cannon aiming**: only fire/ignite have sounds; elevating/rotating the barrel to aim is silent. →
  mechanical ratchet/gear tick while adjusting.
- 🔀 **Supplementaries · Spring Launcher**: launches entities with `PISTON_EXTEND`/`PISTON_CONTRACT`. → springy "boing" /
  coil release.
- 🔀 **Supplementaries · Hourglass flip**: flipping reuses the item-frame rotate sound (`BLOCK_ROTATE`). → glass clink +
  sand-shift.
- 🔇 **Supplementaries · Flag**: cloth flag waves in wind, no ambient sound. → gentle cloth-flap loop when wind-blown.
- 🔇 **Supplementaries · Cage**: captures a live mob, block + tile both silent. → snap/clang capture + rattle when
  occupied.
- 🔇 **Supplementaries · Goblet**: drink interaction consumes liquid silently. → sip/gulp.
- 🔇 **Sleep Tight · Wake-up encounter ("Alarmed")**: a hostile mob is spawned next to the sleeper and they wake; only a
  chat message. Signature scary moment. → tense sting / heartbeat / gasp.
- 🔇 **Vista · TV power on/off**: redstone/placement toggles the CRT (`POWER_STATE`), no sound (only the vol-0.01 static
  ambient). → CRT power-on thunk + degauss / power-off collapse.
- 🔇 **Vista · Viewfinder enter/exit view**: right-click to look through, shift to exit; silent. → electronic/mechanical
  viewfinder power-up/down.
- 🔇 **Vista · Hollow Cassette → feed link**: binds the tape to a camera feed, no success feedback. → "link confirmed"
  bind chime (lodestone-style).

---

## 🟨 TIER 3: Medium

Worth doing, less front-and-center.

- 🔀 **Snowy Spirit · Glow Lights (string lights)**: remove = `BEEHIVE_SHEAR`, place = `AMETHYST_CLUSTER_HIT`. →
  glass-bulb tinkle / soft plastic click.
- 🔇 **Snowy Spirit · Gingerbread Giant (Mongo) jump/land**: rideable charge-jump mount, no jump/land sound. → heavy
  gingerbread creak/thump (reuse gingerbread `fall`/`hit`).
- 🔀 **Haunted Harvest · Candy bag fill**: dropping candy in uses `CROP_PLANTED` (sounds like planting seeds). → paper
  rustle / candy drop.
- 🔀 **Haunted Harvest · Splattered egg**: thrown-egg splat uses `HONEY_BLOCK_PLACE`. → wet egg splat.
- 🔀 **Haunted Harvest · Costume equip (paper bag / carved pumpkin on head)**: default `ARMOR_EQUIP_GENERIC` metallic
  clink. → papery / hollow-thunk equip.
- 🔀 **Haunted Harvest · Villager lights jack-o-lantern**: reuses the wood-`TORCH` **break** sound to ignite the
  candle. → flame ignite / flint-&-steel whoosh.
- 🔇 **Sleep Tight · Bedbugs spawn from bed on wake**: infestation spawn, silent. → skittering/rustle bed cue.
- 🔇 **Sleep Tight · Night bag deploy / pack-up**: unroll uses only default wool place; pack-up is fully silent (
  `removeBlock` no sound). → fabric unroll + roll-up rustle.
- 🔇 **Sleep Tight · Hammock swing**: pendulum animation, no creak. → rope creak on swing.
- 🔀 **Sleep Tight · Dream Essence consumes phantom**: phantom absorbed → generic amethyst break. → "phantom slurp /
  absorbed" sound.
- 🔀 **Vista · Picture Tape slideshow**: `PictureTapeVideoSource` returns null frame sound (cassette source has one). →
  slide-projector / photo-advance click on frame change.

---

## 🟩 TIER 4: Minor / nice-to-have

Low priority; do in bulk if a sound pass happens.

- 🔀 **Supplementaries · Lock/Padlock**: `IRON_TRAPDOOR` toggle → small padlock click/latch.
- 🔀 **Supplementaries · Bamboo Spikes**: impale uses `HONEY_BLOCK_FALL` → stab thud (squelch if poison-coated).
- 🔀 **Supplementaries · Turntable adjust**: speed step uses `COMPARATOR_CLICK` → mechanical dial/ratchet. (Platform
  rotation itself is fine.)
- 🔀 **Supplementaries · Speaker config**: writing to it uses `INK_SAC_USE` → button/dial click.
- 🔇 **Supplementaries · Cog / Relayer**: redstone gears, silent on power change → faint mechanical tick (arguably fine
  as-is).
- 🔀 **Snowy Spirit · Gumdrop Button**: press/release uses slime-block SFX → squishy candy "boop".
- 🔀 **Snowy Spirit · Eggnog**: drink/eat both use `HONEY_DRINK` → distinct creamy gulp (only if bundling drink sounds).
- 🔇 **Snowy Spirit · Candy Cane (food)**: default eat sound → hard-candy crunch (borderline "plain food").
- 🔀 **Haunted Harvest · Bedbug eggs on bed** *(Sleep Tight)* / **egg-crack reuse**: see bedbug notes above.
- 🔇 **Haunted Harvest · Candy from candy bag / trick-or-treat "ask"**: eating relies on generic eat; the "ask" is
  silent → optional crinkle+munch / childlike villager vocal.
- 🔇 **Sleep Tight · Fall out of hammock**: fall damage, no thud → thud / oof.
- 🔇 **Sleep Tight · Home bed established / Invigorated gained**: silent reward states → cozy chime / positive shimmer.
- 🔀 **Sleep Tight · Cure infested bed**: raw `SILVERFISH_DEATH`; should route through the existing `BEDBUG_DEATH` custom
  event for consistency (asset-only once bedbug sounds are swapped).
- 🔇 **Vista · Picture Tape GUI / Wave Gate GUI**: inserting photos / opening config, silent → soft paper shuffle /
  electronic beep.

---

## Notes for whoever implements

- **Haunted Harvest has NO sound system yet**: no `ModSounds`, no `sounds.json`, no `assets/hauntedharvest/sounds/`.
  Everything there is greenfield; a `ModSounds` registry needs to be created (hook in `reg/ModRegistry.java`).
  `spookVillager`/`throwCandy` are duplicated in `GiveCandyToPlayers` + `GiveCandyToBabies`; add to both or refactor.
  The `WITCH_THROW` commented lines are the exact insertion points.
- **Sleep Tight bedbugs** are the cheapest high-value win: the `bedbug.*` events + subtitles already exist and are
  wired; just replace the silverfish .ogg samples with real ones, and consolidate the two raw `SILVERFISH_DEATH` call
  sites onto `BEDBUG_DEATH`.
- **Vista** ships an orphaned `cassette_insert_long.ogg` that isn't referenced in `sounds.json`.
- **By design, not mismatches** (leave alone): Supplementaries deliberately shares the item-frame `BLOCK_ROTATE` sound across sign posts, wrench, and turntable rotation (only the hourglass flip stands out); pumpkin carving in Haunted Harvest correctly uses `PUMPKIN_CARVE`; the Snowy Spirit sled already has full custom wind + on-snow looping sounds.
- **Skinwalker** (Haunted Harvest) has no ambient/hurt/death but appears unregistered/WIP (no `EntityType`, no spawns);
  verify before investing.
