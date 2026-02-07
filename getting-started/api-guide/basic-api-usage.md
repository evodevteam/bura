# Basic API Usage

### Getting Started

After Bura has initialized, obtain the API instance using:

```java
BuraApi api = BuraApi.get();
```

This is the main entry point into the framework.

***

### Registering a Module

Developers typically create a module and register it using the feature registry. This allows Bura to manage lifecycle and provide a ModuleContext.

Example flow:

```java
api.featureRegistry().registerModule(yourModule);
```

The returned registration object should be stored so it can be unregistered during shutdown.

***

### Module Lifecycle

Implement the `Module` interface to receive lifecycle callbacks.

Inside `onInitialize(ModuleContext ctx)` you can access:

* `ctx.api()`\
  Access the main Bura API.
* `ctx.moduleApi()`\
  Access module-scoped API features such as events.
* `ctx.config()`\
  Read configuration values from a read-only snapshot.

***

### Events

#### Render Tick Event

Use the render tick event for lightweight per-frame callbacks.

Example:

```java
RenderTickEventBus renderEvent =
    api.renderTickEvent("your:id");

renderEvent.register(timeNs -> {
    // lightweight per-frame logic
});
```

Keep render tick handlers short and non-blocking.

***

#### Generic Events

Generic events allow communication between modules.

Example:

```java
api.event("your:id", ListenerType.class);
```

Use events to signal work instead of running heavy logic directly.

***

### Recommended Usage Patterns

* Register a module through the feature registry.
* Use `ModuleContext` to access API, events, and config.
* Keep event handlers lightweight.
* Use returned registration objects to unregister listeners.
* Use `featureRegistry().findModule(id)` for optional integrations with other modules.
* Prefer `moduleApi()` instead of accessing internal framework classes.

***

### What Not To Do

* Do not attempt to replace or set the global API instance.
* Do not perform heavy or blocking work inside render tick or event handlers.
* Do not rely on internal implementation classes or mixins.
* Do not assume internal scheduling or policy behavior.
* Do not modify configuration snapshots directly.

***

### Lifecycle Checklist

Typical flow:

1. In your mod initializer:

```java
BuraApi.get()
    .featureRegistry()
    .registerModule(yourModule);
```

2. Inside `Module.onInitialize(ctx)`:

* Read config with `ctx.config()`.
* Register render tick listeners.
* Register other events.

3. On shutdown:

* Unregister listeners using registration objects.
* Release resources.

***

### Mental Model

Bura provides a small integration surface:

* register a Module
* use ModuleContext for API access
* subscribe to events for runtime behavior

Let Bura manage performance-sensitive decisions. Use events and supported APIs instead of forcing execution directly.
