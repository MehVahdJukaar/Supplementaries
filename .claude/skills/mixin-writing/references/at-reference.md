# `@At` Parameter and `@Slice` Reference

Detailed reference for `@At` parameters and `@Slice` syntax. Consult when you need
exact parameter formats — the main SKILL.md covers which targeting approach to use.

> **Reminder:** Before reaching for `ordinal > 0`, `@Slice`, `CONSTANT` args, or
> `shift = BY`, consider whether an `@Expression` would match the target directly.

## `@At` Parameters

| Parameter | Description                                                                                                                                                                                                                             |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `target`  | Bytecode member descriptor: `Lowner/Class;name(Lparam;)Lreturn;` for methods, `Lowner/Class;name:Ltype;` for fields. **Uses the bytecode owner**, which may differ from the source declaration — use `mixin_method_bytecode` to verify. |
| `ordinal` | 0-based index when multiple instructions match. -1 (default) = match all.                                                                                                                                                               |
| `shift`   | `BEFORE` (default for most), `AFTER`, `BY` (with `by=N`; keep small, avoid offsets beyond 3).                                                                                                                                           |
| `opcode`  | ASM opcode int for FIELD and JUMP (e.g. `Opcodes.GETFIELD`, `Opcodes.IFEQ`).                                                                                                                                                            |
| `remap`   | `true` (default) remaps target through mappings. Set `false` for non-Minecraft methods.                                                                                                                                                 |
| `args`    | Extra args for CONSTANT and INVOKE_STRING (see below).                                                                                                                                                                                  |
| `slice`   | ID of a named `@Slice` to restrict the search region.                                                                                                                                                                                   |

## Additional `@At` values

These are omitted from the main SKILL.md table because `@Expression` is almost always
a better choice for the same targets:

| Value           | Targets                                   | Key params                                     |
|-----------------|-------------------------------------------|------------------------------------------------|
| `INVOKE_STRING` | Before an invoke with a single string arg | `target`, `args={"ldc=value"}`                 |
| `CONSTANT`      | Before a constant literal                 | `args` — see below                             |
| `JUMP`          | Before a jump instruction                 | `opcode` (IFEQ/IFNE/IFLT/GOTO/etc.), `ordinal` |

### CONSTANT args

Use exactly one discriminator:
`intValue=N`, `floatValue=N`, `longValue=N`, `doubleValue=N`, `stringValue=text`,
`classValue=fully/qualified/Name`, `nullValue=true`.

Also: `expandZeroConditions=LESS_THAN_ZERO,GREATER_THAN_ZERO` to match zero in conditionals.

## `@Slice`

Narrows the bytecode region an injection point searches in. Use when `ordinal` alone
can't disambiguate (e.g., same method called in two distinct code paths).

> **Prefer `@Expression` over `@Slice`** when possible. An expression like
> `this.emitGameEvent(ENTITY_MOUNT, ?)` is self-documenting and precisely targets the
> right call. A `@Slice` from one invoke to another is fragile if the target method
> is refactored.

```java
@Inject(
        method = "tick",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/...;startPhase()V"),
                to = @At(value = "INVOKE", target = "Lnet/...;endPhase()V")),
        at = @At(value = "INVOKE", target = "Lnet/...;process()V")
)
```

- `from`/`to` are both **inclusive**. Defaults: `from = @At("HEAD")`, `to = @At("TAIL")`.
- Named slices: give `@Slice(id = "mySlice")`, reference from `@At(slice = "mySlice")`.
- For multi-match `from`/`to`, use specifiers like `@At(value = "INVOKE:LAST", ...)`.
