# Cooperative Pistons — Architecture

NeoForge-only mixin feature that lets `N` pistons facing the same direction combine
their push limits to `N × 12` blocks when they share a structure (typically connected
via slime/honey). Works for both extension and retraction.

## Components

| File                                                             | Purpose                                                                                                                                                                                                                                                                                                                                |
|------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `common/.../misc/PistonCooperationTracker.java`                  | Per-tick static registry. Records which pistons are attempting to extend/retract this tick and their direction.                                                                                                                                                                                                                        |
| `common/.../utils/ICooperativePiston.java`                       | Interface injected into `PistonStructureResolver` so callers can hand it a cooperator set + combined push limit.                                                                                                                                                                                                                       |
| `neoforge/.../mixins/neoforge/PistonStructureResolverMixin.java` | Implements `ICooperativePiston`. Wraps `addBlockLine` calls inside `resolve()` to also build chains for cooperator startPositions; modifies the three `BIPUSH 12` limit checks in `addBlockLine` to use the combined limit; extends the `BlockPos.equals(pistonPos)` checks to also recognise cooperator body positions as boundaries. |
| `neoforge/.../mixins/neoforge/PistonBaseBlockMixin.java`         | Wraps `checkIfExtend`'s `resolve()` call to drive the extension protocol. Injects into the retraction-branch `blockEvent` call to register retraction attempts. Wraps the `new PistonStructureResolver(...)` in `moveBlocks` to inject cooperator data into the actual-movement resolver and pre-retract cooperator heads.             |

## Lifecycle

Two distinct flows. Key insight: in 1.21.1, `neighborChanged` calls `checkIfExtend`
synchronously (no scheduled tick), but block events posted via `level.blockEvent`
fire one tick LATER via `runBlockEvents`. So `checkIfExtend` runs in tick T;
`moveBlocks` (called from `triggerEvent`, called from the block event) runs in T+1.

### Extension (tick T → T+1)

```
Tick T:
  Each piston's neighborChanged → checkIfExtend → supp$wrapCheckIfExtendResolve:
    1. markAttempting(pos, dir, extending=true, T)
    2. Run vanilla resolve. If succeeds → markPosted, return true (vanilla posts event).
    3. If fails → getCooperators(pos, dir, true)
    4. If cooperators empty → return false (no event)
    5. Build coopResolver, set cooperators + limit (N*12), call resolve()
    6. On success: manually post events for cooperators that didn't post yet, return true

Tick T+1:
  runBlockEvents fires each piston's event → triggerEvent → moveBlocks →
  supp$wrapMoveBlocksResolver:
    1. getCooperators(pos, facing, extending=true) — reads T's data
    2. Create resolver via original.call
    3. If cooperators present: set cooperators + limit on resolver

  Cooperative resolve in moveBlocks reads cooperator data; PistonStructureResolverMixin
  handlers do the real work (limit modification, extended pistonPos check, extra chains
  for cooperator startPositions).
```

After the first piston's `moveBlocks` runs and moves the shared structure, the
second piston's startPos becomes AIR (vacated). The second piston's resolve trivially
succeeds with empty `toPush` and just extends its arm into the air.

### Retraction (tick T → T+1)

Retraction's `checkIfExtend` never creates a resolver — it posts a block event with
id=1 or 2 directly. We register pistons via `@Inject` on the retraction-branch
`blockEvent` call (`ordinal=1`).

```
Tick T:
  Each piston's neighborChanged → checkIfExtend (retraction branch) →
  supp$registerRetraction:
    markAttempting(pos, dir, extending=false, T)
  Then vanilla posts the retraction event.

Tick T+1:
  Each piston's event fires. triggerEvent for retraction:
    - Sets body to MOVING_PISTON.
    - Checks block 2 ahead. If pushable → calls moveBlocks.
      → supp$wrapMoveBlocksResolver:
          1. getCooperators(pos, facing, extending=false)
          2. For each cooperator: supp$preRetractCooperator() — see below.
          3. Create resolver, set cooperators + limit.

  The first piston's moveBlocks does the actual cooperative pull. The second piston's
  triggerEvent sees its `block 2 ahead` is now MOVING_PISTON (filled by the cooperative
  move), so it skips moveBlocks entirely and just removes its head — handled by vanilla.
```

## Tracker Design (`CooperativePistonData`)

- `WorldSavedData` instance, one per `ServerLevel` (`perLevel=true`). Fixes cross-dimension
  isolation: each dimension has an independent tracker.
- Each entry is `AttemptInfo(direction, extending, tick)`. Entries carry their own registration
  tick and expire after `MAX_AGE = 20` ticks (purged lazily in `markAttempting`).
  `getCooperators` also age-filters inline so a rescheduled `moveBlocks` (running several
  ticks after `checkIfExtend`) can still find its cooperation group.
- The `extending` flag still filters cooperators so extension data from an earlier tick
  cannot leak into a retraction's `moveBlocks`.
- `postedPistons` (dedup guard for `checkIfExtend` event posting) is transient — not
  included in the codec, so it resets on world reload. That is fine: it is only consulted
  within the same tick as `markAttempting`.
- Codec serialises `attemptingPistons` as a flat list of `StoredEntry` records. No network
  codec (not synced to clients).

### `getCooperators` geometry

Iterates all registered pistons (small set, usually 2-4). A candidate qualifies if:

1. Same direction.
2. Same `extending` flag.
3. `|pushAxisOffset| < MAX_PUSH_DEPTH` (12) — push chains can overlap.
4. Perpendicular offset is non-zero (not collinear/same column).
5. `perpDist ≤ MAX_PUSH_DEPTH` — within reach in the perpendicular plane.

This iteration-based approach replaced an earlier line-scan that walked perpendicular
axes at the scanning piston's own coordinates — that scan missed cooperators at
different push-axis offsets (e.g. one piston at y=0 pushing up, another at y=1).

## Cooperator startPos formula

In `supp$wrapResolveAddBlockLine`, after the primary chain succeeds, we call
`addBlockLine` for each cooperator's chain. The startPos differs by operation:

- **Extension:** `cooperator + pistonDirection*1` — the block immediately in front of
  the piston (what its own resolver would push).
- **Retraction:** `cooperator + pistonDirection*2` — the block 2 ahead (the block
  being pulled back).

Requires shadowing `pistonDirection` and `extending` on the resolver.

## `supp$preRetractCooperator` — the critical retraction subtlety

When `moveBlocks` runs for a cooperative retraction, we need to remove each
cooperator's `PISTON_HEAD` so the sticky-branching forward-scan doesn't hit it (it
has push reaction `BLOCK`, fails the scan).

Naïve removal (`setBlock(headPos, AIR, 20)`) destroys the cooperator's piston body!
Because `PistonHeadBlock.onRemove` looks behind the head: if it sees an extended
`PISTON`/`STICKY_PISTON` body, it calls `destroyBlock(bodyPos, true)`, popping the
piston as an item.

Vanilla retraction avoids this by ORDER: `triggerEvent` sets the body to
`MOVING_PISTON` FIRST, then `moveBlocks` removes the head. With the body already
`MOVING_PISTON`, `onRemove`'s check skips the destroy.

We replicate the same order for cooperators: before removing each cooperator's head,
set its body to `MOVING_PISTON` with the same `newMovingBlockEntity(...)` setup that
vanilla `triggerEvent` would use. When the cooperator's own `triggerEvent` runs
moments later, its `setBlock` is a no-op (state already matches); its
`setBlockEntity` just overwrites our placeholder block entity (both created at
progress=0 in the same tick, no visible animation jump).

## Mixin handler reference

### `PistonBaseBlockMixin`

| Handler                         | Type             | Target                                                                                |
|---------------------------------|------------------|---------------------------------------------------------------------------------------|
| `supp$wrapCheckIfExtendResolve` | `@WrapOperation` | `PistonStructureResolver.resolve()` call inside `checkIfExtend` (extension branch)    |
| `supp$registerRetraction`       | `@Inject`        | Second `Level.blockEvent(...)` call in `checkIfExtend` (ordinal=1, retraction branch) |
| `supp$wrapMoveBlocksResolver`   | `@WrapOperation` | `NEW PistonStructureResolver` in `moveBlocks`                                         |

### `PistonStructureResolverMixin`

| Handler                        | Type                                                   | Target                                                                                                                            |
|--------------------------------|--------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `supp$setCooperators`          | Interface impl (`ICooperativePiston`)                  | Called externally to inject data                                                                                                  |
| `supp$wrapResolveAddBlockLine` | `@WrapOperation`                                       | `addBlockLine(...)` call inside `resolve()` — adds extra chains for each cooperator after primary succeeds                        |
| `supp$modifyTrailingLimit`     | `@ModifyExpressionValue` + `@Expression("? > @(12)")`  | The two `BIPUSH 12 IF_ICMPLE` limit checks in the backward-scan loop (`require=2`)                                                |
| `supp$modifyForwardLimit`      | `@ModifyExpressionValue` + `@Expression("? >= @(12)")` | The `BIPUSH 12 IF_ICMPLT` limit check in the forward scan (`require=1`)                                                           |
| `supp$wrapPistonEqualsCheck`   | `@WrapOperation`                                       | All `BlockPos.equals(pistonPos)` `INVOKEVIRTUAL`s in `addBlockLine` — extends to also treat any cooperator position as a boundary |

## Edge cases handled

- **Different update orders** (A's `checkIfExtend` first vs B's first): the "last one
  to run" always finds all previously-registered cooperators in the tracker.
- **Vanilla resolve succeeds individually** (small structure with sticky branching):
  cooperative branch is skipped in `checkIfExtend`, but `moveBlocks` still applies
  cooperator data — the cooperative resolver excludes the other piston's body from
  `toPush` (which a buggy vanilla branching would have included via sticky pull).
- **Different heights** (e.g. piston A at y=0, piston B at y=1, both pushing UP):
  the iteration-based `getCooperators` finds them via push/perpendicular offset
  checks, unlike a line-scan at the scanner's own y-level.
- **Stale tracker data from a previous extension** when a retraction happens many
  ticks later: filtered out by the `extending` field in `AttemptInfo`.

## Known limitations

None remaining. Both former limitations were fixed when the static `PistonCooperationTracker`
was replaced by `CooperativePistonData` (WorldSavedData, per-level):

- **Cross-dimension isolation**: each `ServerLevel` has its own data instance.
- **Rescheduled moveBlocks**: entries now carry a registration tick and age out after
  20 ticks, so a retry running a few ticks later still finds its cooperation group.
