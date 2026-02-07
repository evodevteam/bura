# First Module Example

### Example Module

Create a new Java class inside your package.

Example path:

```
src/main/java/yourpackagename/ExampleModule.java
```

Replace the contents with:

```
package com.example.bura;

import bura.api.v1.Module;
import bura.api.v1.ModuleDescriptor;
import bura.api.v1.ModuleContext;
import bura.api.v1.events.RenderTickEvent;
import bura.api.v1.events.Registration;

public class ExampleModule implements Module {

    private final ModuleDescriptor descriptor =
        new ModuleDescriptor("example.basic", "Example Module", "0.1");

    private Registration renderRegistration;

    @Override
    public ModuleDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public void onInitialize(ModuleContext ctx) {

        // Read config value (read-only snapshot)
        boolean enabled = ctx.config().getBoolean("example.enabled");

        if (!enabled) {
            return;
        }

        // Register render tick listener
        renderRegistration = ctx.moduleApi()
            .renderTickEvent("example:render_tick")
            .register((long timeNs) -> {
                // Keep work small and fast here
            });
    }

    @Override
    public void onShutdown() {
        if (renderRegistration != null) {
            renderRegistration.unregister();
            renderRegistration = null;
        }
    }
}
```

***

### Register the Module

Create or open your mod entry class.

Example:

```
src/main/java/yourpackagename/ModEntry.java
```

Add:

```
package com.example.bura;

import net.fabricmc.api.ModInitializer;
import bura.api.v1.BuraApi;

public class ModEntry implements ModInitializer {

    @Override
    public void onInitialize() {
        BuraApi.get().featureRegistry().registerModule(new ExampleModule());
    }
}
```

***

### What This Example Does

* Creates a basic Bura module
* Registers it using the feature registry
* Reads configuration from ModuleContext
* Registers a render tick listener
* Properly unregisters during shutdown
