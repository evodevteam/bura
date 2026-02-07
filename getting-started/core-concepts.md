# Core Concepts

### Overview

Bura is a performance-focused Fabric mod that sits between Minecraft’s rendering and engine subsystems.

Its goal is to improve client performance by controlling when expensive work happens instead of allowing the game to schedule everything immediately.

Bura observes game activity through targeted mixins, evaluates current performance conditions, and decides when tasks such as chunk compilation, uploads, and rendering updates should execute.

Think of Bura as a lightweight performance middleware layer.

***

### High Level Architecture

Bura is organized into several main layers:

#### Platform bootstrap

During initialization, Bura creates and publishes a central API implementation. This acts as the entry point for internal systems and external modules.

#### Runtime control plane

A small set of controllers tracks runtime state such as frame timing, player movement, and workload pressure. These controllers produce decisions about scheduling and throttling.

#### Work queues and caches

Expensive operations are placed into managed queues rather than executed immediately. Caches are used to avoid repeating work unnecessarily.

#### Instrumentation via mixins

Targeted mixins intercept important engine systems such as rendering, uploads, rebuild scheduling, and particles. These mixins send information to the control plane and follow its decisions.

#### UI and hooks

Bura provides a minimal HUD and lightweight event hooks for modules.

***

### Core Systems

#### Tick driven orchestrator

Every client tick, Bura updates timing information, movement state, and internal counters.

This tick acts as the central heartbeat that drives decisions and scheduling.

***

#### Policy engine

The policy engine evaluates performance conditions using:

* moving averages of frame time
* movement states
* spike detection
* workload pressure

It produces budgets and decisions such as:

* how many uploads can run
* whether rebuilds should be delayed
* when scheduling should slow down or speed up

***

#### Compile queue

Bura uses a producer consumer queue for compile tasks.

Game systems request work as producers. The framework decides when tasks actually run as the consumer.

The queue:

* prioritizes important tasks
* removes duplicates
* drains gradually based on budgets

***

#### Visibility filtering

A visibility cache reduces repeated frustum checks and helps prioritize nearby content.

Rendering systems use cached decisions when possible.

***

#### Subsystem throttling

During performance spikes, Bura reduces workload without fully disabling systems.

Examples include:

* probabilistically skipping particles
* limiting remote entity updates
* delaying rebuild scheduling

This allows smoother performance instead of sudden feature loss.

***

### Lifecycle Mental Model

1. Mod initialization creates the framework API.
2. Client initialization registers UI, keybinds, and events.
3. Each game tick updates internal timing and recomputes budgets.
4. Mixins intercept engine actions and consult the policy engine.
5. Work queues schedule tasks based on current budgets.
6. Rendering continues with adaptive throttling.

***

### Developer Mental Model

Developers interact with Bura through a small API surface.

Typical integration includes:

* accessing the framework API instance
* subscribing to lightweight events
* respecting scheduling decisions instead of forcing immediate work

Bura favors adaptive behavior. Modules should tolerate occasional skipped updates and only request priority when necessary.
