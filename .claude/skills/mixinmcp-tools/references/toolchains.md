# Toolchain reference: getting Minecraft, Forge, and NeoForge sources searchable

Consult this when `mixin_search_in_deps` returns nothing for `net/minecraft/`,
`net/minecraftforge/`, or `net/neoforged/`, or when `mixin_get_dep_source` cannot
resolve a vanilla path. The SKILL.md covers triage; this covers the per-toolchain
recovery commands and what each toolchain ships.

## Diagnose first

Always start with `mixin_list_source_roots`. It reports:

- Which Library SOURCES roots are attached
- Which roots came from MixinMCP's auto-attach pass
- Whether vanilla / Forge / NeoForge game-API canaries (e.g.
  `net/minecraft/world/level/Level.java`, `net/minecraftforge/event/entity/EntityEvent.java`,
  `net/neoforged/neoforge/event/Event.java`) resolve
- The last MDG merged-jar auto-attach run, including warnings

Auto-attach warnings are almost always the root cause when MDG searches come up empty.
Fix those before reaching for fallbacks; if you can't, include them verbatim in any
bug report.

## ForgeGradle (older, pre-MDG)

Vanilla MC sources are properly attached as Library SOURCES roots out of the box.
`mixin_search_in_deps` can grep `net/minecraft/` directly with no extra steps.

## ModDevGradle (MDG) — Forge MDG and NeoForge

Vanilla Minecraft and the loader game API ship together in a **merged JAR** under
`build/moddev/artifacts/`. MixinMCP auto-attaches that merged jar as a Library SOURCES
root after each Gradle sync, falling back to a Gradle `*-sources.jar` (e.g.
`net.minecraftforge:forge:*-sources` or `net.neoforged:neoforge:*-sources` under
`~/.gradle/caches`) when the merged jar has no `.java` entries (i.e. when MDG
recompilation is disabled).

After auto-attach completes, `mixin_search_in_deps` is the primary tool for
`net/minecraft/`, `net/minecraftforge/`, and `net/neoforged/` paths.

If search is still empty:

1. Run `mixin_list_source_roots` and read the **MDG merged-jar source auto-attach** section.
2. If warnings appear there, fix or report those first; further fallbacks won't change anything until attachment
   succeeds.
3. Only then fall back to `mixin_find_class(includeSource=true)`, `mixin_method_bytecode`, or `mixin_search_symbols`.

## Fabric Loom

Run `./gradlew genSources` to materialise Minecraft sources, then call
`mixin_sync_project` so IntelliJ picks up the new `-sources.jar`.

## "Sources are missing entirely" recovery

If `mixin_list_source_roots` shows no Minecraft root at all (nothing under
`net/minecraft/`, no merged jar, no `-sources.jar`):

| Toolchain    | Command                                                                      |
|--------------|------------------------------------------------------------------------------|
| Fabric Loom  | `./gradlew genSources`                                                       |
| NeoForge MDG | `./gradlew downloadAssets`                                                   |
| Any loader   | `./gradlew genDependencySources --force` (needs `org.gradle.jvmargs=-Xmx4g`) |

Then run `mixin_sync_project` to refresh IntelliJ's project model.

## Notes on `mixin_get_dep_source` for vanilla paths

In MDG projects, vanilla Minecraft classes resolve via `path` once the merged jar is
auto-attached as Library SOURCES. If `path` fails, the issue is almost always
attachment, not the path itself; re-run `mixin_list_source_roots` and follow the
diagnose-first flow above. As a last resort, `mixin_find_class(includeSource=true)`
goes via PSI rather than the source roots and works even when attachment failed.
