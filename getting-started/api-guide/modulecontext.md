# ModuleContext

`ModuleContext` is provided to your module during initialization.

You receive it inside:

```
onInitialize(ModuleContext ctx)
```

It gives your module access to:

* the Bura API
* module-specific API tools
* configuration values

Use it to register listeners, read settings, and interact with the framework.

***

### ctx.api()

Returns the global `BuraApi` instance.

Use this when you need access to top-level framework functionality.

Example:

```
@Override
public void onInitialize(ModuleContext ctx) {

    BuraApi api = ctx.api();

    String version = api.apiVersion();
}
```

Most modules will not need this frequently.

***

### ctx.moduleApi()

Returns the module-scoped API.

This is the main way modules interact with Bura.

Use it for:

* events
* render tick listeners
* feature registry access

Example:

```
@Override
public void onInitialize(ModuleContext ctx) {

    ctx.moduleApi()
        .renderTickEvent("my:render_tick")
        .register((long timeNs) -> {
            // small per-frame logic
        });
}
```

Prefer using `moduleApi()` for most integrations.

***

### ctx.config()

Returns a read-only configuration snapshot for your module.

Use it to read configuration values.

Example:

```
@Override
public void onInitialize(ModuleContext ctx) {

    boolean enabled = ctx.config().getBoolean("my.feature.enabled");
    int limit = ctx.config().getInt("my.feature.limit");

    if (!enabled) {
        return;
    }
}
```

Do not modify values from `config()`.

***

### Quick Rules

* Use `ctx.moduleApi()` for most interactions.
* Use `ctx.api()` only when global access is needed.
* Treat `ctx.config()` as read-only.
* Keep listeners and callbacks lightweight.
