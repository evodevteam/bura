# Events and Listeners

Events allow your module to react to actions inside Bura. You register listeners that run when an event is triggered.

All events are accessed through `ModuleContext`.

***

### Getting an Event

Inside `onInitialize`, use `ctx.moduleApi()` to access events.

Example:

```
MyEventListener listener = (int value) -> {
    // keep handlers small and fast
};

Event<MyEventListener> ev =
    ctx.moduleApi().event("example:my_event", MyEventListener.class);

Registration reg = ev.register(listener);
```

Always store the returned `Registration`.

***

### Render Tick Event

The render tick event runs every frame. Use it only for small, non-blocking logic.

Example:

```
Registration renderReg = ctx.moduleApi()
    .renderTickEvent("example:render_tick")
    .register((long timeNs) -> {
        // tiny per-frame work
    });
```

Avoid heavy work inside render tick listeners.

***

### Registration

When you register a listener, Bura returns a `Registration`.

The `Registration` represents your subscription to the event.

Example:

```
Registration reg = ev.register(listener);
```

***

### Unregistering

You should unregister listeners when your module shuts down.

Example:

```
@Override
public void onShutdown() {
    if (reg != null) {
        reg.unregister();
        reg = null;
    }
}
```

This prevents unused listeners from remaining active.

***

### Summary

* Get events through `ctx.moduleApi()`
* Register listeners
* Store the `Registration`
* Unregister during shutdown
