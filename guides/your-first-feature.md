# Your First Feature

This guide shows a simple real-world example of using the Bura API.\
The module automatically lowers render distance when FPS drops and restores it when performance improves.

This demonstrates:

* creating a practical module
* using render tick events
* reading configuration
* modifying game settings safely

***

### Create the Module

Create a new class:

```
src/main/java/yourpackagename/AutoRenderDistanceModule.java
```

Add:

```
package com.example.bura;

import bura.api.v1.Module;
import bura.api.v1.ModuleDescriptor;
import bura.api.v1.ModuleContext;
import bura.api.v1.events.RenderTickEvent;
import bura.api.v1.events.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class AutoRenderDistanceModule implements Module {

    private final ModuleDescriptor descriptor =
        new ModuleDescriptor("example.autord", "Auto RenderDistance", "0.1");

    private Registration tickReg;
    private boolean lowered = false;
    private int originalRd = -1;
    private long lastTimeNs = -1;
    private double fpsAvg = 0.0;

    @Override
    public ModuleDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public void onInitialize(ModuleContext ctx) {

        boolean enabled = ctx.config().getBoolean("autord.enabled");
        int lowThreshold = ctx.config().getInt("autord.lowFpsThreshold");
        int minRenderDistance = ctx.config().getInt("autord.minRenderDistance");
        int restoreBuffer = ctx.config().getInt("autord.restoreBuffer");

        if (!enabled) return;

        tickReg = ctx.moduleApi()
            .renderTickEvent("example:autord_tick")
            .register((long timeNs) -> {

                if (lastTimeNs > 0) {
                    long deltaNs = Math.max(1L, timeNs - lastTimeNs);
                    double fps = 1_000_000_000.0 / (double) deltaNs;

                    if (fpsAvg == 0.0) fpsAvg = fps;
                    else fpsAvg = fpsAvg * 0.9 + fps * 0.1;

                    Minecraft mc = Minecraft.getInstance();
                    if (mc == null || mc.options == null) return;

                    Options opts = mc.options;
                    int currentRd = opts.renderDistance().get();

                    if (!lowered && fpsAvg < lowThreshold) {
                        originalRd = currentRd;
                        int target = Math.max(minRenderDistance, currentRd - 2);
                        opts.renderDistance().set(target);
                        lowered = true;
                    }

                    if (lowered && fpsAvg > (lowThreshold + restoreBuffer)) {
                        if (originalRd >= 0) {
                            opts.renderDistance().set(originalRd);
                        }
                        lowered = false;
                        originalRd = -1;
                    }
                }

                lastTimeNs = timeNs;
            });
    }

    @Override
    public void onShutdown() {
        if (tickReg != null) {
            tickReg.unregister();
            tickReg = null;
        }
    }
}
```

***

### Register the Module

Add or update your mod entry class:

```
package com.example.bura;

import net.fabricmc.api.ModInitializer;
import bura.api.v1.BuraApi;

public class ModEntry implements ModInitializer {

    @Override
    public void onInitialize() {
        BuraApi.get().featureRegistry().registerModule(new AutoRenderDistanceModule());
    }
}
```

***

### What This Example Shows

* Using `ModuleContext`
* Registering a render tick listener
* Reading config values
* Making small performance-aware adjustments
