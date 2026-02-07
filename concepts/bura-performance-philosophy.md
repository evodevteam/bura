# Bura Performance Philosophy

Bura is designed as a performance-focused framework. Instead of forcing strict limits, it adapts to real-time conditions and adjusts workload dynamically.

Understanding these core ideas helps you build modules that work smoothly with the system.

***

### Adaptive Throttling

Game performance changes constantly based on player movement, world complexity, and rendering load.

Bura uses adaptive throttling to react to these changes.

Instead of always limiting work, the system:

* reduces workload when performance drops
* restores normal behavior when conditions improve

Mental model:

Think of a thermostat that increases or decreases work depending on temperature.

***

### Decision vs Action

Bura separates decision-making from execution.

* The framework decides what should run.
* The engine performs the actual work.

This allows policies to change without modifying engine behavior.

Mental model:

A traffic controller decides when cars move, but drivers still operate the vehicles.

***

### Budgeting and Scheduling

Work is managed through budgets rather than fixed rules.

Each tick has limits on how much expensive work can run.

Key ideas:

* tasks may wait until there is available budget
* nearby or important tasks are prioritized
* smoothing prevents sudden changes from single bad frames

Mental model:

Each tick has a small allowance. Tasks wait in line and execute when space becomes available.

***

### Graceful Degradation

Bura avoids completely disabling features whenever possible.

Instead it reduces workload gradually, for example:

* lowering frequency of updates
* delaying non-critical tasks
* reducing visual intensity when needed

This keeps gameplay responsive while maintaining visual quality.

Mental model:

During heavy traffic, speed is reduced instead of closing roads entirely.

***

### Lightweight Modules

Module callbacks run frequently.

Heavy work inside callbacks can:

* cause frame drops
* create spikes
* interfere with Bura’s scheduling system

Best practice:

* keep callbacks small
* avoid blocking operations
* move heavy work to background systems when needed

Mental model:

Small adjustments keep traffic flowing. Large slow actions cause congestion.

***

### Summary

Think of Bura as a runtime traffic manager:

* it monitors performance
* sets safe limits
* schedules work intelligently

Modules should cooperate by staying lightweight and signaling intent instead of forcing heavy execution.
