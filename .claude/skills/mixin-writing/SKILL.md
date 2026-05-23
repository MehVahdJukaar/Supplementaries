---
name: mixin-writing
description: >
  SpongePowered Mixin and MixinExtras writing reference for Minecraft mods.
  Use this skill whenever writing, reviewing, debugging, or planning mixin code —
  including injector selection, @At injection points, @Slice, @Local, @Share,
  accessor/invoker patterns, interface injection, MixinExtras expressions,
  or diagnosing mixin pitfalls and conflicts. Also use when the user asks
  which injector to use, how to target a specific bytecode pattern, or
  why a mixin isn't working.
---

# Mixin Writing Reference

When writing SpongePowered Mixins, **always prefer MixinExtras injectors** over vanilla
patterns. MixinExtras injectors chain when multiple mods target the same code; vanilla
`@Overwrite`, `@Inject` at HEAD with cancel/return, `@Redirect`, and `@ModifyConstant`
do not chain and will silently conflict with other mods.

## Injector Selection

Pick the **most specific** MixinExtras injector that fits:

| Goal                                          | Use                                  | Avoid                                                                       |
|-----------------------------------------------|--------------------------------------|-----------------------------------------------------------------------------|
| Tweak a method's return value                 | `@ModifyReturnValue`                 | `@Inject(at = RETURN)` + `cir.setReturnValue(modify(cir.getReturnValue()))` |
| Add/change a condition around an expression   | `@ModifyExpressionValue`             | `@Redirect` on the boolean call                                             |
| Conditionally skip a void call or field write | `@WrapWithCondition`                 | `@Redirect` returning default/void                                          |
| Wrap a call/field/instanceof in custom logic  | `@WrapOperation`                     | `@Redirect`                                                                 |
| Wrap an entire method (try/catch, sync, etc.) | `@WrapMethod`                        | `@Overwrite` or `@Inject` at HEAD + RETURN                                  |
| Change receiver of a call/field access        | `@ModifyReceiver`                    | `@Redirect`                                                                 |
| Replace a method wholesale (last resort)      | `@Overwrite` only if no other option | —                                                                           |

**Why not vanilla patterns:**

- **`@Overwrite`**: Breaks ALL other mods targeting the same method.
- **`@Inject` at HEAD + `ci.cancel()`/`cir.setReturnValue()`**: Prevents other mods' injections from running.
- **`@Redirect`**: Does not chain. Use `@WrapOperation`/`@ModifyExpressionValue`/`@WrapWithCondition`/`@ModifyReceiver`.
- **`@ModifyConstant`**: Does not chain. Use `@ModifyExpressionValue` on the constant expression.

Exceptions exist — sometimes `@Inject` at HEAD with cancel is the only practical option
(e.g., returning early before any logic runs). Use judgment, but default to MixinExtras.

**Vanilla injectors still useful alongside MixinExtras:**

- `@ModifyArg` / `@ModifyArgs` — modify argument(s) passed to a specific method call.
  No MixinExtras replacement; these are fine to use. Specify `index` for multi-arg calls.
- `@ModifyVariable` — modify a local variable's value at a specific point. Takes
  `method`, `at`, and `ordinal`/`name`/`index` to identify which local. Prefer `ordinal`
  over `index` — see pitfalls.

## Targeting Bytecode

This section covers how to tell an injector *where* to inject. Two mechanisms exist:
standard `@At` values and MixinExtras `@Expression`. **Pick the right one:**

### When to use which

- **Simple, unambiguous targets** → standard `@At` is fine: `HEAD`, `TAIL`, `RETURN`,
  a single `INVOKE` or `FIELD` that doesn't need `ordinal`.
- **Anything requiring `ordinal > 0`, `@Slice`, `CONSTANT` args, or `shift = BY`** →
  **stop and use `@Expression` instead.** Expressions match the *semantic structure* of
  the code (comparisons, field access patterns, specific call + argument combinations),
  making them far more precise and readable than positional bytecode offsets. An expression
  that says `this.fallDistance > 0.0` is self-documenting; `@At(value = "JUMP", opcode = IFLE, ordinal = 2)` is not.
- **Comparisons, instanceof, casts, array ops, instantiations, compound patterns** →
  `@Expression` is the *only* clean option.

> **Rule of thumb:** If you're about to add `ordinal`, `@Slice`, or `shift = BY` to an
> `@At`, ask yourself whether an `@Expression` would match the target directly. It almost
> always will.

### `@At` Injection Points (simple targets)

| Value                    | Targets                                | Key params                             |
|--------------------------|----------------------------------------|----------------------------------------|
| `HEAD`                   | First instruction in method            | —                                      |
| `RETURN`                 | Before **every** return instruction    | `ordinal`                              |
| `TAIL`                   | Before the **last** return only        | —                                      |
| `INVOKE`                 | Before a method call                   | `target`, `ordinal`                    |
| `INVOKE_ASSIGN`          | After a method call's result is stored | `target`, `ordinal`                    |
| `FIELD`                  | Before a field get/set                 | `target`, `opcode`                     |
| `NEW`                    | Before a `new` instruction             | `target`                               |
| `MIXINEXTRAS:EXPRESSION` | Expression target                      | Use with `@Expression` + `@Definition` |

**RETURN vs TAIL**: `RETURN` fires before *every* return (including early returns).
`TAIL` fires only before the *final* return.

> For the full `@At` parameter reference (`target` descriptor format, `shift`, `opcode`,
> `remap`, `args`, `slice`), `@Slice` syntax, and `CONSTANT`/`INVOKE_STRING`/`JUMP`
> details, see `references/at-reference.md`.

### MixinExtras Expressions (preferred for complex targets)

Expressions use java-like strings to target complex bytecode patterns. They work with
**bytecode, not source code** — you cannot just copy-paste Java. But they are far more
readable and maintainable than positional `@At` targeting.

> For the full expression language spec (all literals, operators, syntax), read
> `references/expressions-language.md`.

#### Core mechanics

- `@At` value must be `"MIXINEXTRAS:EXPRESSION"` — the real target is in `@Expression`.
- `@Definition` binds identifiers used in `@Expression` to fields, methods, types, or locals.
- `?` = wildcard — matches any expression or identifier. Use for brevity or brittle locals.
- `this` is built-in (no `@Definition` needed).
- Strings use **single quotes** inside `@Expression` (already in a Java annotation string).
- Works with any injector, but `@ModifyExpressionValue` and `@WrapOperation` are the most
  natural fit. `@WrapOperation` additionally supports comparisons and array get/set.

#### `@Definition` types

| Parameter | What it defines                                | Format                                                       |
|-----------|------------------------------------------------|--------------------------------------------------------------|
| `field`   | Field access                                   | Same as `@At("FIELD")` target: `Lowner;name:Ltype;`          |
| `method`  | Method call                                    | Same as `@At("INVOKE")` target: `Lowner;name(Lparams;)Lret;` |
| `type`    | Class (for `new`, `instanceof`, casts, arrays) | Class literal: `type = BlockState.class`                     |
| `local`   | Local variable                                 | `local = @Local(type = X.class)` or with `ordinal`           |

Static fields/methods have **no receiver** in the expression: `SOME_FIELD`, not `SomeClass.SOME_FIELD`.

#### Targeting with `@(...)`

By default, the expression targets its "last" instruction. Use `@(...)` to explicitly
mark which sub-expression to target:

```java
// Target the instantiation, not the throw:
@Expression("throw @(new IllegalStateException('Oh no!'))")

// Target multiple things:
@Expression("this.someMethod(@(value1), @(value2))")
```

#### Common patterns

```java
// Modify a comparison in an if-condition:
@Definition(id = "fallDistance", field = "Lnet/minecraft/entity/Entity;fallDistance:F")
@Expression("this.fallDistance > 0.0")
@ModifyExpressionValue(method = "fall", at = @At("MIXINEXTRAS:EXPRESSION"))
private boolean modifyFallCheck(boolean original) { ... }

// Inject after a specific method call with a specific argument:
@Definition(id = "emitGameEvent", method = "Lnet/...;emitGameEvent(Lnet/...;Lnet/...;)V")
@Definition(id = "ENTITY_MOUNT", field = "Lnet/...;ENTITY_MOUNT:Lnet/...;")
@Expression("this.emitGameEvent(ENTITY_MOUNT, ?)")
@Inject(method = "addPassenger", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
private void afterMount(CallbackInfo ci) { ... }

// Modify a new-expression result:
@Definition(id = "BlockStateParticleEffect", type = BlockStateParticleEffect.class)
@Definition(id = "BLOCK", field = "Lnet/...;BLOCK:Lnet/...;")
@Expression("new BlockStateParticleEffect(BLOCK, ?)")
@ModifyExpressionValue(method = "spawnSprintingParticles", at = @At("MIXINEXTRAS:EXPRESSION"))
private BlockStateParticleEffect modifyParticle(BlockStateParticleEffect original) { ... }
```

#### Quickfire expression examples

| Java source                                    | Expression                        |
|------------------------------------------------|-----------------------------------|
| `this.pistonMovementDelta[i] = d;`             | `this.pistonMovementDelta[?] = ?` |
| `nbt.putShort("Fire", (short)this.fireTicks);` | `?.putShort('Fire', ?)`           |
| `entityKilled instanceof ServerPlayerEntity`   | `? instanceof ServerPlayerEntity` |
| `return this.distance < d * d;`                | `return this.distance < ? * ?`    |

#### Expression gotchas

- **Cannot match jumps**: `a && !b`, `a ? b : c` cannot be expressed directly. Use `?`
  wildcards to match them as part of a wider expression.
- **Comparisons must be top-level**: You cannot match `print(a == b)` — only `a == b` alone.
- **Comparison inversion**: For non-float/double types, `x >= y` is indistinguishable from
  `x < y` in bytecode. Make your expression specific enough to avoid ambiguity.
- **`true`/`false` = `1`/`0`** in bytecode. `if (myBoolean)` looks like `? != 0`, and due
  to inversion, `? == 0` also matches. Avoid wildcards for boolean comparisons.
- **Don't target a wildcard** with `@ModifyExpressionValue` — you won't know the concrete
  type. Use a different injector (e.g. `@ModifyArg` for `this.setX(?)`).
- **No float/double distinction**: Use `0.0`, not `0.0F` in expressions.
- Standard `@At` params (`shift`, `ordinal`, `@Slice`) all work normally with expressions.

## Local Variable Capture (`@Local`)

Use MixinExtras `@Local` instead of `locals = LocalCapture.CAPTURE_FAILHARD`:

```java
@Inject(method = "use", at = @At(value = "INVOKE", target = "..."))
private void onUse(CallbackInfoReturnable<?> cir, @Local ItemStack stack) {
    stack.shrink(1);
}
```

The traditional `LocalCapture` approach requires listing every preceding local in
parameter order — fragile when the target method changes. `@Local` selects by type
(or `@Local(ordinal = N)` if ambiguous).

For mutable capture use `LocalRef<T>` / `LocalIntRef` / `LocalDoubleRef` etc.:

```java
@Inject(method = "target", at = @At(...))
private void mutateLocals(CallbackInfo ci,
                          @Local LocalRef<String> name,
                          @Local LocalIntRef color) {
    name.set("modified");
    color.set(0xFF0000);
}
```

## Sharing Values Between Handlers (`@Share`)

Thread-safe value sharing between handlers in the same target method (no mixin fields):

```java
@ModifyArg(method = "target", at = @At(value = "INVOKE", target = "..."))
private int captureArg(int arg, @Share("myArg") LocalIntRef ref) {
    ref.set(arg);
    return arg;
}

@Inject(method = "target", at = @At("TAIL"))
private void useArg(CallbackInfo ci, @Share("myArg") LocalIntRef ref) {
    doSomething(ref.get());
}
```

## `@Cancellable` Sugar

Any MixinExtras injector can receive `@Cancellable CallbackInfo(Returnable)` to
cancel the enclosing method without a separate `@Inject`:

```java
@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "..."))
private Identifier skipPoison(Identifier texture, @Cancellable CallbackInfo ci) {
    if (shouldSkip(texture)) ci.cancel();
    return texture;
}
```

## Accessor and Invoker Patterns

Interface mixins for accessing private members without reflection:

```java
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("health")
    float getHealth();

    @Accessor("health")
    void setHealth(float health);

    @Invoker("actuallyHurt")
    void invokeActuallyHurt(DamageSource source, float amount);
}
```

- `@Accessor` generates a getter and/or setter for a private field.
- `@Invoker` generates a delegating call to a private method.
- Use from external code: `((LivingEntityAccessor) entity).getHealth()`.
- `@Coerce` does NOT work on `@Accessor`/`@Invoker`.

## Interface Injection (Duck Typing)

A mixin can add interfaces to the target class:

```java
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CustomInterface {
    @Unique
    private int myMod$customField;

    @Override
    public int getCustomValue() { return myMod$customField; }
}
```

Cast to access: `((CustomInterface) entity).getCustomValue()`.
If the target already has a method matching the interface signature, make the mixin
abstract or use `@Intrinsic` on the matching method to tell Mixin to use the target's
existing implementation.

## Obscure Features

**`@Pseudo`** — Target class may not exist at compile or runtime (optional mod compat).
Applied to the mixin class. Implies `remap = false`. Use `targets = "com.example.Class"`
(string, not class literal). Mixin is silently skipped at runtime if the target is absent.

**`@Coerce`** — Use a supertype, interface, or inner class when the real type is inaccessible
(package-private, etc.). Applied to handler parameters or on the method (return type
coercion). Works with `@Inject` (local capture) and `@Redirect`. Does NOT work on
`@Accessor`/`@Invoker`. MixinExtras injector support is unconfirmed — test before relying.

**`@Shadow` + `@Final` + `@Mutable`** — To read a target's field: `@Shadow private Type field`.
For final fields, always add `@Final`: `@Shadow @Final private Type field`. To *modify* a
final field, also add `@Mutable`: `@Shadow @Final @Mutable private Type field`. Omitting
`@Mutable` when writing to a final field causes a runtime crash.

**`@Unique`** — Marks fields/methods as belonging solely to this mixin. Prevents name
collision if the target already has a member with the same name. Always prefix mixin-added
members with your modid: `mymod$fieldName`.

## Pitfalls

- **`@Shadow` and superclass members**: `@Shadow` can only target members declared directly
  on the target class, not inherited members. To access inherited fields/methods, have the
  mixin class extend the target's superclass (with a dummy `super(...)` constructor if needed).
  Use `abstract` on the mixin class to avoid implementing the parent's interface methods.
- **`@At` target owner**: Always use the bytecode owner class, not where the method is
  declared in source. Use `mixin_method_bytecode` to verify — the INVOKE* owner may be a
  subclass of the declaring class.
- **`remap = false`**: Required on `@At(target = "...")` and `@Inject(method = "...")` when
  targeting non-Minecraft methods (mod APIs, other mods). Minecraft methods are remapped by
  the toolchain; everything else is not. Forgetting this causes "target not found" at runtime.
- **Constructor injection**: `@Inject` into `<init>` can only inject **after** the `super()`
  call. You cannot inject before `super()`. `RETURN` in a constructor means end of `<init>`,
  not an early return.
- **`@ModifyArg` index**: When the target call has multiple arguments, you MUST specify
  `index` (0-based) to identify which argument to modify. Omitting it is ambiguous for
  multi-arg calls.
- **`@ModifyVariable` index vs ordinal**: `index` is the raw LVT slot number — `this`
  takes slot 0 in instance methods, and `long`/`double` consume two slots. Use `ordinal`
  (Nth variable of the matching type, 0-based) instead to avoid off-by-one errors.
- **`@Unique` field thread safety**: `@Unique` fields are shared across ALL instances of the
  target class on ALL threads. Do not assume single-threaded access. Use `@Share` for
  per-invocation values.
- **Handler naming**: Prefix mixin handler methods with your modid (`mymod$onTick`) to avoid
  collision with other mods' mixins targeting the same class.
- **`require` and `expect`**: `require = 1` (default) crashes if the injection target isn't
  found. Use `require = 0` for targets that may not exist in all versions. `expect` logs a
  warning instead of crashing.
- **`@WrapOperation`**: You MUST call `original.call(...)` to preserve the original behavior
  (and let other mods' wraps run). Only skip the call if you genuinely want to suppress the
  operation.
- **Priority**: `@Mixin(priority = N)` controls merge order. Higher priority mixins are
  applied later and "win" conflicts. Default is 1000. Adjusting priority is a last resort.
- **Modifying other mods' Mixins**: Requires the MixinSquared library (by Bawnorton).
  Can cancel mixin classes/methods or target their injectors. MixinSquared does NOT require
  explicit bootstrap — only to register cancellers and adjusters in your mixin plugin.
