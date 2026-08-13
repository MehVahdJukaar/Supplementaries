# TODO Triage Report

Audit of `TODO.md` (474 lines) + 200 scattered `//TODO`/`HACK`/`FIXME` markers
across `common`, `fabric`, `neoforge` sources.

Goal: cut noise, surface what actually matters for the current 1.21 branch,
and call out items that look stale or already-done so they can be deleted
rather than carried around forever.

---

## 1. Critical / Crashes / Data Loss

These are the things that should be triaged first: they corrupt state,
crash clients, or are reported as outright broken.

- **`copper brazier ceiling crash`**: a literal crash on a vanilla-ish
  block placement. Should be a stack-trace away.
- **`slice map crash`**: map slicing crashes; map code is big surface area,
  worth pinning a repro.
- **`colored map data crash row Data null`**: NPE in colored map data.
- **`cage mob duplication`**: duplication bugs are dangerous on
  multiplayer; classify as critical even if "minor".
- **`bubble blower soap recipe deleting stasis`**: recipe interaction
  destroying enchanted items.
- **`(Quill Maps) ... will duplicate and replace any item in the 1st hotbar
  slot`**: silent inventory clobber from off-hand use.
- **`some bricks ... thrown from a hotbar with more than a single brick will
  make the player throw two at once with one returning as an item and the
  other shattered`**: item dupe path.
- **`SpawnEntityWithPassengersFeature.java:68 //TODO: figure out why this
  deadlocks`**: worldgen deadlock. If this is reachable in vanilla
  worldgen flow it's a hard freeze.
- **`LegacyStructureLocator.java:175 //TODO: this should never be called...
  fix`**: invariant violation already noted by past-you.

## 2. Multiplayer / Server-Only Bugs

A recurring theme. The mod was clearly developed primarily SP and many
features have desync/server-only bugs.

- `netherite doors opening on client for split seconds on server`
- `trapped present desync fabric`
- `spring launcher broken on servers` (also `SpringLauncherArmBlockTile.java`
  and `SpringLauncherHeadBlock.java` flag this in code)
- `WrenchItem.java:37 //TODO: fix server side`
- `hat stand wobble not synced`
- `glowing blackboard persistence`
- `jar insert sound not server side`
- `soap interaction on servers`
- `slingshot blocks desync (anvils)`
- `figure out entities desync` (vague, but recurring)
- `DispenserMinecartEntity.java:316 server doesnt sync xRot`
- `cannon stalling each other when triggered consistently`
- `ServerEvents.java:178 figure out why starting this lags`
- `multiplayer anti player leave sleep stuff` (Sleep Tight)

These deserve their own pass together; many likely share root causes
(missed BE sync, wrong side dispatch, etc.).

## 3. 1.21 Port Debt: "add back" / "for 1.21" / disabled-with-`false`

Stuff that was *intentionally* turned off during the 1.20→1.21 port and
needs re-enabling. These are the highest leverage to address because the
code is mostly written.

- `CompatHandler.java:110`: `FARMERS_DELIGHT = fd && false; //TODO: add back`
- `CommonConfigs.java:1244`: `if (PlatHelper.getPlatform().isForge() &&
  false) { //disabled. TODO: add back`
- `CommonConfigs.java:1084`: `//TODO: fix these`
- `SchematicCannonStuff.java:19` (neoforge): `//TODO: add back`
- `CreateCompatImpl.java:157` (neoforge): `//TODO: add back`
- `QuarkCompatImpl.java:237` (neoforge): `//TODO: add back`
- `InventoryTooltipComponent.java:37` (neoforge): `//TODO: add back`
- `CartographersQuillItem.java:3` (neoforge): `//TODO: add back`
- `CapabilityHandler.java:109` (neoforge): `//TODO: add back`
- `NoticeBoardBlockTile.java:173`: `//TODO: add back`
- `BombEntity.java:353`: `//TODO: add back`
- `SoapWashableHelper.java:187`: `//TODO: add back`
- `LunchBoxBlock.java:121`: `//TODO: 1.21: use loot tables copy nbt stuff`
- `ClientEvents.java:78`: `//TODO: remove in 1.21` *(we're on 1.21, so this
  is now a deletion task, not a future task)*
- `redo all loot tables for containers and copy nbt stuff`
- `add back capabilities`
- `fabric events` / `add fabric missing events` (TODO.md repeats this 3x)

## 4. Half-Implemented Features

Things that exist in the codebase but are explicitly marked unfinished by
the author. Risk of shipping broken-feeling content.

- **Barnacles**: `finish barnacles` (TODO.md) + `BarnaclesMultifaceGrowthFeature.java:88`
- **Plunderers AI**: `PlundererEntity.java:125 go to boat, leave boat,
  switch to captain, shoot cannon`, `PlundererAICommon.java:88-89 improve
  / cannon change`, `2 plunderers using same cannon` (TODO.md)
- **Cannon restraints**: `CannonBlockTile.java:93 this is bugged.
  Restraints don't work properly. Disabled for now.`
- **Bomb explosion behavior**: `BombExplosion.java:39 finish`,
  `BombEntity.java:208/277/329 fix / change all this`
- **Slingshot projectile**: `SlingshotProjectileEntity.java:165/177/239
  finish / rewrite / fix`, plus TODO.md mentions stones-as-first-shot bug,
  enchantments to check, etc.
- **MobContainer**: `MobContainer.java:57 //TODO: finish`
- **ModParticles.java:48 //TODO: finish`
- **Bedbugs (Sleep Tight)**: `finish bedbugs`
- **Gingerbread golem (Snowy Spirit)**: `finish gingerbead golem`
- **Moonlight projectile**: `finish moonlight projectile stuff`

## 5. Code Quality / Tech Debt: "rewrite this" / hacks

Author has flagged these as needing rewrite. Don't touch unless you have
budget; flag them as quarantined.

- `AbstractMobContainerItem.java:281 //TODO rewrite`
- `BellowsBlockTile.java:39 //TODO: this is a mess` plus 4 more TODOs in
  same file
- `WindVaneBlockTile.java:52 //TODO: this is shit, not smooth and bad. redo`
- `SpringLauncherArmBlockTile.java:142/201` (rewrite / use new system)
- `SlingshotProjectileEntity.java:177 //TODO: this is terrible. rewrite`
- `book pile completely broken. rethink` (TODO.md)
- `blue bomb rework` / `blue bomb charge animation and sound (total
  overhaul)`
- `rework spring launcher into gust emitter` (Random Ideas)
- ThreadLocal hacks: `AshLayerBlock.RECURSION_HACK`,
  `MovingSlidyBlockEntity.SUPPRESS_OBSERVER_HACK`,
  `ModServerDynamicResources.TAG_TRANSLATION_HACK`,
  `GravelBricksBlock.SHAPE_HACK`. Each is a code smell but probably load-
  bearing; don't remove without understanding why they exist.

## 6. Polish: Sounds, Particles, Animations

A LOT of the backlog is "needs a proper sound here". Cross-reference with
`NEEDED_SOUNDS.md`. ~25+ items, e.g.:

- `AbstractRopeBlock.java:272/291`: proper rope sound events
- `AwningBlock.java:294`, `NetheriteTrapdoorBlock.java:82`: proper sound
- `CandleHolderBlock.java:49`: extinguish sound
- `HourGlassBlockTile.java:147`: better sound event
- `RopeArrowEntity.java:65/90`, `JarItem.java:65`, `BlazeRodBlock.java:21`
- `IKeyLockable.java:47`: custom sounds
- `ModRegistry.java:885`: blaze sound
- Particles: `BombEntity` trail emitter, `CannonBallEntity:112/141/379`,
  `FeatherParticle:85`, `SugarParticle:19` (this is crap, snap to water),
  many more

These are low-risk individual fixes but would be much faster to batch
with the audio assets that already exist in `NEEDED_SOUNDS.md`.

## 7. Stale / Likely Already Done: Delete From Backlog

Don't carry these around any longer:

- `TODO.md` line 5 `2 plunderers using same cannon`: duplicate of line 3.
- `TODO.md` repeated `check all forge overrides` (lines 62, 69): dup.
- `TODO.md` repeated `anger nearby piglins` (lines 159, 164): dup.
- `TODO.md` repeated `check gunpowder explosion` (lines 114, 172): dup.
- `ClientEvents.java:78 //TODO: remove in 1.21`: we're literally on the
  1.21 branch. Either delete the block or drop the comment.
- `Supplementaries.java:51 //TODO: custom onfig screen reaets config? how
  thats impissible how would it even do that wtf`: venting, not
  actionable. Delete or rewrite.
- `FlowerBoxBlockTile.java:62 //TODO: for 1.22. standardize this darn
  tile`: explicitly deferred to 1.22, don't carry on the 1.21 list.
- `BombEntity.java:329 //TODO: change all this`: too vague to ever act
  on. Either rewrite as a real ticket or delete.
- Several `//TODO` (no description, e.g. `IWindAffected.java:3`,
  `FaucetBlockTile.java:138`, `GenericProjectileBehavior.java:40` (`//TODO;`))
  are content-free. Delete unless an author can resurrect intent.

## 8. Easy Wins: concrete, scoped, low-risk

Targets that look like they'd take <30 min each and don't require redesign:

1. **Delete the stale `//TODO: remove in 1.21` block in
   `ClientEvents.java:78-86`** if its rope-arrow/bubble-blower durability
   tooltip workaround is no longer needed on 1.21 (verify by checking if
   vanilla now emits `item.durability` for these). One-line decision.
2. **Wire up `FARMERS_DELIGHT` compat**: flip the `&& false` at
   `CompatHandler.java:110` and exercise the FD code paths. If it still
   compiles+runs, that's the gate; if not, the resulting failures are
   the real ticket.
3. **`amendments fire charges have fire overlay`**: render flag fix,
   isolated to one item renderer.
4. **`bamboo spike drop and pick block`**: drop list + getCloneItemStack,
   well-bounded.
5. **`flute not working`**: has to be a small dispatch issue; flute is a
   single item.
6. **`turn table on by default`**: config default toggle.
7. **`sherds_tooltip config not working`**: config plumbing; either the
   key is misnamed or the reader is reading the wrong value.
8. **`doormat add item sound`**: already in `NEEDED_SOUNDS.md`; just play
   an existing sound event on insert.
9. **`first slingshot projectile is a stone block`**: likely an init/
   ordering bug, single class.
10. **`recipe conditions not working for negated ones`**: bounded to the
    condition serializer.
11. **`HourGlassBlockTile.java:147` better sound event**: swap call.
12. **Delete content-free TODOs** (group commit, mechanical cleanup):
    `IWindAffected.java:3`, `FaucetBlockTile.java:138`,
    `GenericProjectileBehavior.java:40`, `BucketHelper.java:29`,
    `MobContainer.java:367` (already gated, comment is moot), the venting
    one in `Supplementaries.java:51`.

## 9. Wishlist / Ideas: Park These

The "Random Ideas" section of `TODO.md` (~120 lines) is not a backlog,
it's a brainstorm dump. Recommend:

- Move it out of `TODO.md` into a separate `IDEAS.md` so the actual TODO
  list isn't 80% noise.
- Anything that's been there >1 year and never started should be deleted,
  not preserved.

Examples that should clearly *not* live in a TODO list:

- "Tree chat gpt" (Wise Tree)
- "vampires mummy crypt necromancer curse stuff, skull trap block..."
- "warp fly and warper block"
- "raycon mod" / "airpods portable jukebox mod" / "damage numbers mod" /
  "camera mod": these are *whole separate mods*, not tasks on this one.

## 10. Cross-mod sections (Amendments / SS / HH / SleepTight / Atlases /

       Heartstone / Polytone / Randomium / WiseTree / Labels / MmmMmm /
       AdvFrames)

These look like they were pasted in from other repos' notes. Each
sub-mod's list is short and self-contained; consider:

- Moving them to their own repos' TODO files instead of mixing
  everything into the Supplementaries one.
- At minimum, the **Map Atlases** list (`heartstone icon not in sync`,
  `teammates dont show on atlas`, `death marker off map`,
  `map atlas player icon not getting deleted`) reads like real shipped
  bugs that someone is likely getting bug reports about; promote them.

---

## Summary recommendations

1. **Split the file**: move "Random Ideas" + cross-mod sections out.
   `TODO.md` should be ~50 lines of *this mod's actionable bugs*, not
   474 lines including wishlist.
2. **Cluster the multiplayer/server bugs** (section 2) into a single
   investigation; strong chance they share root causes.
3. **Burn down the "add back" 1.21 port debt** (section 3) before adding
   anything new. That's mostly mechanical and unblocks features that
   already exist on disk.
4. **Delete the stale and content-free TODOs** (section 7); they make
   it impossible to find the real ones.
5. **Triage the "rewrite this" pile** (section 5) honestly: pick one to
   actually rewrite this cycle, leave the others alone.
6. **Quick wins** (section 8) are a good warm-up batch if you want a
   morale-boosting commit before tackling the harder stuff.