# Creating Your First Module

Modules are the main way to build structured features using the Bura API.

***

### Module Registration

A module starts working when it is registered.

Example:

```
BuraApi.get().featureRegistry().registerModule(yourModule);
```

When a module is registered:

1. Bura stores information about the module.
2. A `ModuleContext` is created for it.
3. Bura calls the module’s `onInitialize(ModuleContext ctx)` method.

This is where your module setup happens.

The registry may also return a `Registration` object. This can be used later to unregister the module if needed.

***

### ModuleContext

`ModuleContext` is the main object your module uses to interact with Bura.

Inside `onInitialize`, Bura provides the context:

```
public void onInitialize(ModuleContext ctx)
```

The context gives access to:

* `ctx.api()`\
  Access to the main Bura API.
* `ctx.moduleApi()`\
  The recommended way to access events and module systems.
* `ctx.config()`\
  A read-only configuration snapshot.

Typical usage:

* Read configuration values.
* Register event listeners.
* Access other modules if needed.

Configuration is read-only. Do not try to modify it directly.

***

### Module Lifecycle

Modules follow a simple lifecycle.

#### 1. Registration

The module is registered and a context is created.

#### 2. Initialization

Bura calls:

```
onInitialize(ModuleContext ctx)
```

Use this to:

* read configuration
* register events
* setup listeners

Keep initialization fast and non-blocking.

#### 3. Runtime

After initialization, your module runs through callbacks:

* render tick events
* other framework events

Keep event handlers small and fast.

#### 4. Shutdown

When Bura stops or removes the module:

```
onShutdown()
```

You should:

* unregister listeners
* stop background tasks
* release resources

***

### Recommended Usage Pattern

Typical module structure:

1. Implement the `Module` interface.
2. Register the module using the feature registry.
3. Use `onInitialize(ctx)` to setup logic.
4. Store `Registration` objects when registering listeners.
5. Unregister everything during shutdown.
