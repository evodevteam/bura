# Module Best Practices

This page explains recommended patterns when developing modules for Bura.

Following these practices helps maintain performance and compatibility with the framework.

***

### Keep Initialization Lightweight

`onInitialize()` should only perform setup tasks.

Good uses:

* reading config values
* registering listeners
* initializing small objects

Avoid:

* heavy calculations
* long loops
* blocking operations

***

### Keep Event Handlers Small

Events like render tick run frequently.

Handlers should:

* execute quickly
* avoid heavy logic
* avoid blocking calls

Example:

```
register(timeNs -> {
    updateCounter();
});
```

***

### Always Store Registration Objects

When registering listeners, save the returned `Registration`.

Example:

```
Registration reg = event.register(listener);
```

This allows you to unregister later.

***

### Clean Up During Shutdown

Always remove listeners in `onShutdown()`.

Example:

```
@Override
public void onShutdown() {
    if (reg != null) {
        reg.unregister();
    }
}
```

***

### Respect Configuration

Use `ctx.config()` to read settings and allow users to disable features.

Example:

```
if (!ctx.config().getBoolean("feature.enabled")) {
    return;
}
```

***

### Do Not Access Internal Systems

Use only public API methods.

Avoid:

* internal classes
* mixins
* undocumented features

***

### Summary

* Keep modules lightweight
* Use events properly
* Clean up resources
* Respect configuration
* Use only supported APIs
