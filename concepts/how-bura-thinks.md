# How Bura Thinks

Bura is an adaptive runtime system that manages when work should execute based on current performance conditions. It does not enforce fixed rules. Instead, it continuously observes runtime signals and adjusts scheduling limits to maintain consistent responsiveness.

The system separates decision-making from execution. Bura decides which work is allowed to run, while the engine performs the actual execution.

***

### Core Concept

Bura functions as a coordinator for workload timing and priority.

Key properties:

* Scheduling decisions are independent from execution logic.
* Work is permitted through policies rather than forced directly.
* Performance conditions influence scheduling continuously.

The system prioritizes maintaining smooth frame pacing by adjusting workload timing and fidelity instead of disabling systems outright.

***

### Continuous Adaptation

Bura operates as a feedback loop.

#### Observation

Runtime signals are sampled continuously. Examples include:

* frame timing
* movement or activity indicators
* queue pressure

#### Smoothing

Short-term averaging reduces sensitivity to isolated spikes.

#### Classification

Observed conditions are interpreted into general performance states.

#### Response

Scheduling limits adjust dynamically:

* Workload restrictions increase under pressure.
* Restrictions relax gradually when conditions improve.

***

### Work Scheduling Model

Bura uses a priority and budget system.

Core rules:

* Each tick has a limited execution budget.
* Tasks request execution rather than executing immediately.
* Higher-priority work is selected first.
* Lower-priority work may be delayed.

Scheduling uses short time windows rather than strict per-frame counts. This allows short bursts when performance allows while maintaining limits during spikes.

When performance drops, Bura prefers soft adjustments such as:

* reducing update frequency
* delaying non-critical tasks
* lowering fidelity

***

### Conceptual Flow

The system operates through four stages:

1. Signals\
   Runtime metrics are collected.
2. Decisions\
   Policies interpret signals and define budgets, limits, and priorities.
3. Scheduling\
   Tasks are selected according to current rules and available budget.
4. Execution\
   The engine executes permitted tasks.

***

### Framework and Module Relationship

Modules cooperate with the framework.

Modules:

* register work requests
* provide priority information
* remain lightweight and non-blocking

The framework:

* determines when work executes
* enforces fairness and performance stability

Modules should not assume immediate execution. Scheduling decisions are centralized.

***

### Design Intent

The architecture supports:

* Stability through continuous adaptation.
* Predictable behavior through separation of decision and execution layers.
* Fair scheduling through budgets and priorities.
* Graceful degradation through reduced frequency or fidelity rather than hard disabling.

***

### Common Misconceptions

Bura does not simply disable features when performance drops. It reduces workload gradually.

Registering work does not guarantee immediate execution.

Modules are not intended to override scheduling policies frequently.

Heavy work in frequently called callbacks undermines scheduling effectiveness.

***

### Developer Guidelines

* Treat execution as permision-based.
* Keep callbacks small and fast.
* Use priority signals sparingly.
* Design work to tolerate deferral and incremental execution.
