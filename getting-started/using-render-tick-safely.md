# Using Render Tick Safely

Render tick events run every frame. They allow modules to perform small updates aligned with rendering.

Because they run very frequently, render tick handlers must remain lightweight.

***

### Basic Usage

Register a render tick listener inside `onInitialize`.

Example:

```
@Override
public void onInitialize(ModuleContext ctx) {

    ctx.moduleApi()
        .renderTickEvent("example:render_tick")
        .register((long timeNs) -> {
            // small per-frame logic
        });
}
```

***

### What Render Tick Is Good For

Use render tick for:

* updating UI elements
* small counters or timers
* lightweight checks
* animation updates
* reading game state

***

### What NOT To Do

Avoid heavy operations inside render tick:

* long loops
* file operations
* blocking calls
* expensive calculations
* world scanning

Bad example:

```
register(timeNs -> {
    // DO NOT DO THIS
    heavyPathfindingCalculation();
});
```

***

### Why Lightweight Matters

Render tick runs every frame.

Heavy logic inside this callback can:

* reduce FPS
* create stutters
* interfere with Bura’s performance control systems

***

### Recommended Pattern

Use render tick only to trigger work, not perform heavy work.

Example:

```
register(timeNs -> {
    scheduleWork();
});
```

***

### Summary

* Keep handlers small
* Avoid blocking operations
* Use render tick for lightweight updates only
