# MixinExtras Expression Language Reference

Full syntax documentation for `@Expression` strings. Consult when writing non-trivial
expressions — the SKILL.md covers common patterns; this covers every construct.

## Literals

```
'hello'    // String
'x'        // String or char
23         // int or long
0xFF       // int or long (hex)
1.5        // float or double (no distinction)
true       // boolean (equivalent to 1 in bytecode — be careful)
null
```

## Unary Expressions

```
-x
~x         // bitwise not — indistinguishable from x ^ -1, matches both
```

## Binary Expressions

```
a * b    a / b    a % b
a + b    a - b
a << b   a >> b   a >>> b
a & b    a ^ b    a | b
```

Precedences match Java. `&` and `|` are **bitwise only** — there is no way to match
logical `&&` or `||` (or `!`). Use wildcards to match expressions involving these.

## Comparisons

```
a == b   a != b
a < b    a <= b
a > b    a >= b
```

- **Must be top-level** — you cannot match `print(a == b)`.
- `@ModifyExpressionValue` and `@WrapOperation` both work on comparisons.
- For all types **except float/double**, comparisons are **indistinguishable from their
  inverses** in bytecode (e.g. `>=` looks the same as `<`). Make expressions specific
  enough to avoid ambiguity.
- Be especially careful with `== 0` / `!= 0`: `if (myBoolean)` compiles to `!= 0`,
  and due to inversion, `== 0` also matches.

## Identifier Expressions

```
someLocal                  // load
someLocal = someValue      // store
SOME_STATIC_FIELD          // get
SOME_STATIC_FIELD = someValue  // put
```

All identifiers must be defined in `@Definition` (except `this`).

## Member Expressions

```
x.someField                // get
x.someField = someValue    // put
```

The field identifier must be defined in `@Definition`. `.length` on arrays is built-in.

## Method Calls

```
x.someMethod()             // instance, no args
x.someMethod(a, b)         // instance, 2 args
staticMethod()             // static, no args
staticMethod(a)            // static, 1 arg
```

The method identifier must be defined in `@Definition`. Static methods use no receiver.

## Wildcards

```
?                      // match any expression
someObject.?           // match any field on someObject
someObject.?(someArg)  // match any method on someObject taking 1 arg
```

- Replace an entire expression or just an identifier.
- Use for brevity, brittle locals, or expressions involving jumps (which can't be
  explicitly specified).
- **Don't target a wildcard** with `@ModifyExpressionValue` — the concrete type may
  differ from expectations. Use a different injector instead.

## Array Operations

```
someArray[someIndex]              // load
someArray[someIndex] = someValue  // store
```

`@WrapOperation` has special support for array get/set.

## Array Creations

```
new SomeType[]{a, b, c}     // filled
new SomeType[someLength]     // empty
new SomeType[3][4][5]        // multi-dimensional
```

The type must be defined in `@Definition`.

## Casts

```
(SomeType) someExpression
```

The type must be defined in `@Definition`. Primitive casts are built-in: `(float) x`.

## Instanceof

```
x instanceof SomeType
```

The type must be defined in `@Definition`.

## Instantiations

```
new SomeType()        // no args
new SomeType(x, y)    // 2 args
```

The type must be defined in `@Definition`.

## Method References, Constructor References, and Lambdas

```
::someMethod               // unbound reference
someReceiver::someMethod   // bound reference
SomeType::new              // constructor reference
::someLambda               // unbound if lambda doesn't capture `this`
this::someLambda           // bound if lambda captures `this`
```

- Method references and lambdas are treated identically and need `@Definition`.
- Only capture of `this` determines bound vs unbound (other local captures don't matter).
- Unbound references match both static and non-static methods.

## Returns and Throws

```
return someExpression
throw someException
```

## Targets `@(...)`

```
@(someExpression)
someMethod(@(new SomeType()))
this.something + @(this.somethingElse)
this.someMethod(@(value1), @(value2))
```

- Mark which sub-expression the injector should target.
- Without explicit `@(...)`, the entire expression is implicitly targeted (its "last"
  instruction).
- Multiple explicit targets are allowed.

## `@Definition` Parameter Summary

| Parameter | Defines                              | Format                                                                 |
|-----------|--------------------------------------|------------------------------------------------------------------------|
| `field`   | Field access                         | `Lowner/Class;name:Ltype;` (same as `@At("FIELD")` target)             |
| `method`  | Method call                          | `Lowner/Class;name(Lparams;)Lreturn;` (same as `@At("INVOKE")` target) |
| `type`    | Class (new, instanceof, cast, array) | Class literal: `type = SomeClass.class`                                |
| `local`   | Local variable                       | `local = @Local(type = X.class)` or with `ordinal`                     |

Built-in identifiers (no `@Definition` needed): `this`.
