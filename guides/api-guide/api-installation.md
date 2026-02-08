# API Installation

### Download API and Mod

Download both the Bura API and the Bura mod from:

[https://github.com/evodevteam/bura/tags](https://github.com/evodevteam/bura/tags)

You can also download a preconfigured API project if available.

Both the API and the Bura mod are required.

***

### First Launch

After downloading and opening the API project:

1. Open the Gradle panel on the right sidebar.
2. Click **Execute Gradle Task**.
3. Search for `runClient`.
4. Run `runClient`.

After Minecraft starts, close it.

***

### Install Bura Mod

1. Open the `run` directory inside the project.
2. Open the `mods` directory.
3. Place the Bura mod jar inside the `mods` directory.
4. Close the directory.

***

### Add Bura API

1. Create a new directory in the project root called:

```
libs
```

2. Place the Bura API jar inside the `libs` directory.

***

### Add API Dependency

Open `build.gradle`.

Inside the `dependencies` section add:

```
modImplementation files("libs/bura-api-1.0.0.jar")
```

If the line is highlighted:

* Check the exact name of the API jar inside the `libs` directory.
* Replace `bura-api-1.0.0.jar` with the correct file name, or rename the jar to match.

***

### Add Dependency in fabric.mod.json

Press Shift twice and search for:

```
fabric.mod.json
```

Open it and add Bura to the `depends` section:

```
"depends": {
    "fabricloader": ">=0.18.4",
    "minecraft": "~1.21.11",
    "java": ">=21",
    "fabric-api": "*",
    "bura": "*"
}
```

***

### Test API Integration

Navigate to:

```
src/main/java/yourpackagename
```

Inside this directory there should be a Java class file (usually your main mod initializer class).

Open the file and replace its contents with:

```java
package your.package;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bura.api.v1.BuraApi;
import bura.api.v1.events.RenderTickEventBus;

public class TheJavaClassFilename implements ModInitializer {

	public static final String MOD_ID = "test";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

		// GET API
		BuraApi api = BuraApi.get();

		LOGGER.info("Bura API version = " + api.apiVersion());

		// REGISTER RENDER EVENT
		RenderTickEventBus renderEvent = api.renderTickEvent("bura:render_tick");

		renderEvent.register(timeNs -> {
			LOGGER.info("Render tick from API: " + timeNs);
		});
	}
}
```

Make sure to:

* Replace `your.package` with your actual package name.
* Replace `TheJavaClassFilename` with the java class you're editing now.

***

### Verify Installation

Run the `runClient` task again.

If everything is installed correctly, you should see messages similar to:

```
Bura API initialized: <version>
Bura API version = <version>
Render tick from API: <timeNs>
```

If these messages appear, the Bura API is installed and working correctly.
