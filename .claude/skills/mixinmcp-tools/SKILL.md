---
name: mixinmcp-tools
description: >
  MixinMCP IntelliJ tooling for searching Minecraft, mod, and dependency sources.
  ALWAYS use this skill instead of Grep, Read, or jar extraction when you need
  to search or read vanilla Minecraft code, mod code, or any dependency on the
  classpath; these tools index inside jars that Grep cannot see and return
  structured results that use less context than raw file dumps. Use when looking
  up classes, methods, fields, bytecode, inheritance hierarchies, call graphs,
  mixin conflicts, or dependency sources. Also use when writing @At(target)
  strings, targeting lambdas, or diagnosing "target not found" errors.
---

# MixinMCP Tools

MixinMCP tools are provided by the IntelliJ MCP server and can intelligently search
Minecraft sources and dependency sources. They index the entire classpath including
inside dependency jars (which Grep cannot see), provide dedicated type hierarchy,
call graph, and reference-finding tools that are faster and more accurate than text
search, and return clean structured results that use less context than raw file dumps.
Dependencies without published sources are decompiled via the MixinMCP Gradle plugin
(Vineflower) so every library on the classpath is searchable.

**ALWAYS prefer these tools over Grep, Read, or jar extraction.**

## Invoking Tools

MixinMCP tools are available as regular MCP tools once the IntelliJ MCP server is
connected. Call them directly by name:

```
mixin_find_class(className="net.minecraft.world.level.Level", includeMembers=true)
```

Tool descriptions document all parameters and defaults — read them before calling.

## Tool Selection

| Goal                                      | Tool                                                                                                                                                                                 |
|-------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Look up a class by FQCN                   | `mixin_find_class` (if SourceKind is "Classes JAR (binary)", use `mixin_get_dep_source` for better source)                                                                           |
| Read just one method's body               | `mixin_find_class(className, methodName=...)`. Returns one method instead of dumping the whole file (huge for `Block` / `BlockBehaviour`). Pair with `fieldName` for a single field. |
| Search names across classpath             | `mixin_search_symbols`                                                                                                                                                               |
| Grep dependency sources by regex          | `mixin_search_in_deps` → then `mixin_get_dep_source` with returned `url`. For **short** matched bodies, pass `contextLines` and skip the follow-up read.                             |
| Read a known dependency file              | `mixin_get_dep_source` (pass `path`, e.g. `io/redspace/.../Utils.java`)                                                                                                              |
| Inheritance chain                         | `mixin_type_hierarchy`                                                                                                                                                               |
| All implementors                          | `mixin_find_impls`                                                                                                                                                                   |
| All usages of a class/method/field        | `mixin_find_references` (supports both methods and fields via `memberName`)                                                                                                          |
| All call sites across MC + all deps       | `mixin_find_references` (more complete than `mixin_search_in_deps` for call-site enumeration)                                                                                        |
| Cross-mod mixin conflicts on a target     | `mixin_find_targeting_mixins` — finds all @Mixin classes + their injection points                                                                                                    |
| Call graph                                | `mixin_call_hierarchy`                                                                                                                                                               |
| Method origin in hierarchy                | `mixin_super_methods`                                                                                                                                                                |
| All overrides of a method                 | `mixin_find_overrides` — walks down the class hierarchy; mirror of `mixin_super_methods`                                                                                             |
| Synthetic/lambda method names             | `mixin_class_bytecode` (filter="synthetic")                                                                                                                                          |
| Exact @At(target) for an INVOKE           | `mixin_method_bytecode` — read the owner class from INVOKE* instructions                                                                                                             |
| Bytecode for a specific method            | `mixin_method_bytecode`                                                                                                                                                              |
| Convert a name between mapping namespaces | `mixin_mappings_lookup` — mojmap / yarn / intermediary / srg / obf, for class / method / field                                                                                       |
| Diagnose missing source roots             | `mixin_list_source_roots`                                                                                                                                                            |

## Examples

Look up a class:

```
mixin_find_class(className="net.minecraft.world.level.Level", includeMembers=true)
```

Search dependency sources, then read the result:

```
mixin_search_in_deps(regexPattern="destroyBlock", fileMask="Level")

// Results are grouped by file. Each group shows a url: line once —
// pass that url to mixin_get_dep_source:
mixin_get_dep_source(url="<url from result>", lineNumber=42, linesBefore=10, linesAfter=20)
```

Search and capture the matched method body inline (skip the follow-up read):

```
mixin_search_in_deps(regexPattern="isPathfindable\\(", pathPrefix="net/minecraft/", contextLines=8)
```

Read just one method from a huge class (e.g. `Block` / `BlockBehaviour`):

```
mixin_find_class(
  className="net.minecraft.world.level.block.state.BlockBehaviour",
  methodName="isPathfindable",
)
```

Narrow to Minecraft-only sources with pathPrefix:

```
mixin_search_in_deps(regexPattern="addEffect\\(", pathPrefix="net/minecraft/", timeout=25000)
```

Read source by known path (without searching first):

```
mixin_get_dep_source(path="io/redspace/ironsspellbooks/player/ServerPlayerEvents.java", lineNumber=360, linesBefore=20, linesAfter=20)
```

Find field references:

```
mixin_find_references(className="net.minecraft.world.entity.LivingEntity", memberName="DATA_HEALTH_ID")
```

## Common Pitfalls

### mixin_find_class

- For huge classes like `Block`, `BlockBehaviour`, `LivingEntity`: prefer `methodName=` (or `fieldName=`) to read just
  the member you care about. `includeSource=true` dumps the full file (50KB+ for some vanilla classes) and forces extra
  grep work.
- When a name is only inherited, the tool shows the inherited declaration with an `(inherited from X)` tag. Follow up
  with `mixin_super_methods` to walk the chain or call `mixin_find_class` on `X` directly for canonical declarations.
- Overloads are listed in order, each with its own line range header. Disambiguate by reading the parameter list, not
  the index.

### mixin_search_in_deps

- `regexPattern` is **Java regex**. Escape metacharacters: `addEffect\\(` not `addEffect(`.
  If you pass unescaped metacharacters, the tool will return a hint suggesting the fix.
- Each result's `url:` line includes `[rootKind: ...]`. Prefer Library SOURCES hits over decompiled ones.
- `fileMask` matches file path inside jar (e.g. `net/minecraft/world/entity/LivingEntity.java`):
    - No wildcards → case-insensitive **substring** match anywhere in the path
    - With wildcards → glob
    - Does NOT match jar names or Maven coordinates.
    - **Caution:** Short substrings like `apotheosis` will also match paths in *other* mods' compatibility packages (
      e.g. `compat/apotheosis/`). Use longer path fragments or `pathPrefix` for precision.
- `pathPrefix`: restricts to files whose logical path starts with the prefix (e.g. `net/minecraft/` or
  `io/redspace/ironsspellbooks/`). Use forward slashes.
- `roots`: `all` (default), `library` (only published -sources.jar), `decompiled` (only MixinMCP cache). When `all`,
  cache files are skipped if the same path already matched in library sources.
- `contextLines` (default 0, max 200): include N lines around each match. Match lines stay highlighted with
  `||markers||` and are prefixed with `>`; overlapping windows are merged per file. Use small values (3–10) when you
  expect a **short** matched body (a few-line override) so the result captures the body inline and you can skip the
  follow-up `mixin_get_dep_source` call. For longer methods or unfocused searches keep the default 0.
- Broad searches can time out. Increase `timeout` (e.g. 20000–30000) for searches without a fileMask.

### Vanilla / Forge / NeoForge sources are empty

When `mixin_search_in_deps` returns nothing for `net/minecraft/`,
`net/minecraftforge/`, or `net/neoforged/`, follow this triage order:

1. **Run `mixin_list_source_roots`** and read the **MDG merged-jar source auto-attach** section.
2. **Auto-attach warnings → fix or report those first.** Until attachment succeeds, every other fallback is fighting the
   wrong fire.
3. **Sources truly missing for the toolchain?** See `references/toolchains.md` for the per-toolchain recovery commands (
   Fabric Loom `genSources`, NeoForge MDG `downloadAssets`, any loader `genDependencySources --force`). Then call
   `mixin_sync_project`.
4. **Last resort fallbacks:** `mixin_find_class(includeSource=true)` and `mixin_method_bytecode` work via PSI /
   classfile and don't depend on the source roots being attached.

`mixin_list_source_roots` uses canary `.java` paths (e.g.
`net/minecraft/world/level/Level.java`,
`net/minecraftforge/event/entity/EntityEvent.java`,
`net/neoforged/neoforge/event/Event.java`) to confirm each game-API tree is
actually present, not just nominally on the classpath.

### mixin_get_dep_source

- `url`: copy the exact `url:` string from search results (strip `[rootKind: ...]` suffix).
- `path`: package path with `/` separators and `.java` extension (e.g. `io/redspace/.../Utils.java`). NOT a filesystem
  path.
- If a path is not found, fall back to `mixin_search_in_deps` then use the returned `url`.
- **Vanilla Minecraft classes** in MDG projects should resolve via `path` once the merged jar is auto-attached. If
  `path` fails, the issue is almost always attachment, not the path; see the "Vanilla / Forge / NeoForge sources are
  empty" triage above and `references/toolchains.md` for recovery.

### mixin_find_references / mixin_call_hierarchy / mixin_super_methods / mixin_find_overrides

- `memberName` supports **both methods and fields**. For fields, no disambiguation is needed. For methods, disambiguate
  overloads with:
    - `parameterTypes=["MobEffectInstance", "Entity"]` — simple type names
    - `methodDescriptor="(Lnet/...;)Z"` — JVM descriptor
    - Parameterless: `parameterTypes=[]` or `methodDescriptor="()V"`
- If disambiguation fails, the error lists all overloads with ready-to-copy `parameterTypes` and declaring class.
- `mixin_find_references` returns both runtime call sites AND string references in mixin annotations.
- For dedicated mixin conflict analysis, prefer `mixin_find_targeting_mixins`.
- `mixin_call_hierarchy` callees cover direct method calls, constructor invocations (`new Foo(...)`), method
  references (`Foo::bar`, `Foo::new`), and the real synthetic target behind each lambda. The `lambda$X$N` synthetic is
  resolved through the `INVOKEDYNAMIC` bootstrap handle and tagged `[lambda]`; constructors are tagged `[ctor]`. Method
  references that resolve to an existing non-synthetic method (e.g. `this::setPosToBed`) surface as untagged callees —
  indistinguishable from a direct call, because that's the method you'd mixin into. Output is `owner#name(descriptor)`
  in JVM format, ready to paste into `@At(target = "...")`. Non-lambda `INVOKEDYNAMIC` (string concat, switch
  bootstraps) is intentionally omitted. If the method body is not available in source (binary class), the tool
  automatically falls back to structured bytecode analysis that covers the same cases.
- `mixin_call_hierarchy` recurses up to `maxDepth` (default 3, capped at 10). Each depth is indented two spaces and
  tagged `[L1]`, `[L2]`, ... so nesting is visible at a glance; cycles and already-expanded nodes are marked `[cycle]`
  and not re-expanded; abstract / native leaves show `(abstract — no body to walk)` so terminal branches read distinctly
  from depth-cap truncation. `maxResults` is a **global budget across all depths and branches** — raise it for wide
  hierarchies, or narrow the query (start with `maxDepth=1` and grow) if output is too long. For callers, references
  from field/static initialisers appear as `(non-method context)` leaves and don't recurse. Both callers and callees
  work across JVM languages via UAST (Java, Kotlin, Groovy, Scala) — Kotlin callers resolve to their enclosing function
  and Kotlin method bodies are walked for callees, alongside the Java PSI and bytecode paths.
- `mixin_find_overrides` is the downward counterpart to `mixin_super_methods` — use it when you need to know every
  concrete implementation of an abstract/interface method before injecting. `[abstract]` tags mark overrides that are
  themselves abstract (interface extensions, not implementations). For non-overridable methods (
  static/private/final/constructors/final classes) the tool returns an explanation and no list.
- `mixin_super_methods` walks the **full** chain to every root declaration (both superclass and super-interface paths),
  not just the direct super. Output is indented by depth; entries tagged `[root declaration]` are the original
  declarations — usually the best mixin target when you want to affect all overriders. When a method overrides both a
  class method and an interface default, multiple roots are listed and summarized at the end.

### mixin_search_symbols

- Searches **short names** only, not FQCNs. Pass `LivingEntity` not the full package path.
  (FQCNs are auto-simplified with a note — use `mixin_find_class` for exact FQCN lookup.)
- Required parameter is `query` (a single name substring). Search one name at a time.
- Method results include parameter types for disambiguation.

### mixin_class_bytecode

- Decompiled source does NOT show synthetic method names. To target a lambda in @Redirect or @Inject, you MUST use this
  tool with `filter="synthetic"`.

### mixin_method_bytecode

- Each INVOKE* instruction shows the **real owner class**, not the declaring class from source. Always use this owner
  when writing `@At(target = "...")`.

### mixin_find_targeting_mixins

- Increase `maxResults` (default 50) for heavily-targeted classes like `LivingEntity` or `Player`.

### mixin_type_hierarchy

- Increase `maxResults` (default 50) for heavily-inherited classes like `LivingEntity` or `Block` when you need the full
  subtype list. Otherwise the output shows `... (truncated at N results)`.
- Use `direction="supers"` to skip the potentially-large subtype listing when you only need the inheritance chain
  upward.
- **Direct interfaces** are the class's own `implements`/`extends` clause. **Inherited interfaces** are the transitive
  closure — everything picked up from the superclass chain and from super-interface extension. Each inherited entry is
  tagged `(from X)` (introduced by superclass X) or `(via X)` (reached by extending interface X). Check both sections
  before assuming a class does not implement something: e.g. `LivingEntity` does not directly implement `CommandSource`,
  but inherits it from `Entity`.

### mixin_mappings_lookup

- **Input symbol must be in the `from` namespace** — if `from="srg"` the class must be the SRG name (e.g.
  `net/minecraft/src/C_12_`), not the mojmap/yarn name. Cross-namespace class renames mean you can't mix-and-match.
- Method / field inputs include the **owner class**: `net/minecraft/src/C_12_.m_8793_`. Accepts `.` or `/` as package
  separator; last dot before `(` (method) or `:` (field) splits owner from member.
- Method descriptor is optional (e.g. `m_8793_(Ljava/util/function/BooleanSupplier;)V`) — if omitted, all overloads on
  the class are returned. Same for field type descriptors (`entities:Lnet/...;` vs just `entities`).
- MC version is auto-detected from `gradle.properties` (keys: `minecraft_version`, `mc_version`, `minecraftVersion`,
  `mcVersion`, `minecraft.version`, `mc.version`, checking root + each subproject). Pass `mcVersion` explicitly if
  auto-detect fails.
- First call per MC version downloads mappings (Mojang launcher meta + Fabric Maven + Forge/NeoForge Maven) to
  `~/.cache/mixinmcp/mappings/`. Subsequent calls are cached in memory for the IDE session.
- `obf` (the obfuscated production names) is available as a namespace even though mod authors rarely type it directly —
  useful for mixin debugging.
- **SRG** is published by Forge's `mcp_config` and NeoForged's `neoform`. If neither has published for a very new MC
  version yet, the error names both URLs tried.
- **Yarn** tiny files often carry only `intermediary → named` (no obf); the tool bridges through the
  separately-downloaded intermediary mappings automatically.
- ProGuard (Mojang) mappings do NOT carry field descriptors — mojmap field lookups may show the name without a type.

## Mixin Workflow

1. **Type hierarchy first.** Run `mixin_type_hierarchy` before designing the @Mixin. The target's parent often defines
   the method you really want to inject into; jumping straight at the subclass means rewriting the mixin once you
   discover the real declaration site.
2. **Bytecode for synthetics.** Decompiled source hides lambda and bridge names.
   `mixin_class_bytecode(filter="synthetic")` is the only way to get the actual `lambda$X$N` symbol you need to put in
   `method = "..."`.
3. **Bytecode owner for `@At(target)`.** The owner emitted in an INVOKE instruction can differ from the declaring class
   shown in source (devirtualisation, override resolution, mixin re-application). The bytecode owner is what runs at
   injection time, so use `mixin_method_bytecode` and copy that owner into your `@At`.
4. **Method origin questions.** `mixin_super_methods` walks up the chain to the original declaration;
   `mixin_find_overrides` walks down to every concrete implementation.
5. **After writing a mixin**, validate for errors before committing.
6. **After dependency changes**, run `./gradlew genDependencySources` then `mixin_sync_project` so the indexed sources
   match the new classpath.

For injector selection (`@ModifyExpressionValue` vs `@WrapOperation` vs `@Inject` vs ...), `@At` parameter format, and
MixinExtras `@Expression` syntax, switch to the **mixin-writing** skill once you've nailed down the target via the tools
above.

**Note:** Only `mixin_sync_project` accepts an optional `projectPath` parameter. All other tools automatically use the
open project.

## Troubleshooting

**If `mixin_*` tools are not available:**
The tools are provided by IntelliJ's MCP server. If they don't appear:

1. Ensure IntelliJ is running with the project open
2. Check IntelliJ: Settings → Plugins → verify both "MCP Server" and "MixinMCP" are enabled
3. Verify the IntelliJ MCP server is configured in your Claude Code MCP settings
   (`~/.claude/settings.json` or project `.claude/settings.json` under `mcpServers`)
4. **Restart Claude Code** if IntelliJ was started after the Claude Code session began
