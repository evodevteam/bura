# Work Lifecycle

Bura manages work through a structured lifecycle that separates intent, decision-making, and execution. Modules introduce work into the system, and the framework determines when that work may run based on current performance conditions.

The goal is controlled flow rather than maximum throughput. Work progresses through defined stages from initial request to execution and feedback.

***

### Conceptual Overview

Work items move through a continuous decision loop:

Intent → Queue → Evaluation → Scheduling → Execution → Feedback

Modules submit requests. The framework evaluates conditions and grants execution permission only when current budgets and priorities allow.

***

### Lifecycle Stages

#### Intent and Enqueue

A module registers a work request when something needs to happen, such as rebuilding data or performing a heavy computation.

The request represents intent. Execution is not immediate.

***

#### Metadata and Tagging

Each work item carries metadata used for scheduling decisions, for example:

* priority level
* estimated cost
* locality or importance
* rules for resuming or deduplication

This information helps the scheduler compare tasks without executing them.

***

#### Queuing and Visibility

Work enters a shared queue or registry.

The system may:

* collapse duplicate requests
* merge similar tasks
* prevent redundant execution

Queued work remains visible to the scheduler but is not guaranteed to run immediately.

***

#### Sensing and Evaluation

Runtime signals are sampled continuously. Examples include:

* frame timing
* movement
* backlog size
* recent performance spikes

Signals are smoothed over short windows to avoid reacting to isolated slow frames.

The system computes an internal performance state that influences scheduling decisions.

***

#### Decision and Budgeting

Based on current signals, Bura defines limits for the next scheduling window:

* total allowed work
* priority weighting
* burst allowances
* temporary restrictions during spikes

Budgets represent how much work may proceed, not which specific tasks will run.

***

#### Scheduling Selection

The scheduler selects tasks from the queue according to:

* priority
* locality or urgency
* estimated cost
* remaining budget

Selection balances fairness with responsiveness. Some tasks may wait across multiple cycles.

***

#### Execution Permission

Selected tasks receive permission to execute.

The engine or worker system performs the actual work only after permission is granted. Tasks without permission remain queued.

***

#### Feedback and Lifecycle Update

Execution outcomes feed back into the system:

* time taken
* success or failure
* resource usage

Tasks may:

* complete and exit the lifecycle
* be partially finished and requeued
* be rescheduled with updated metadata.

***

### How Modules Introduce Work

Modules propose work rather than forcing execution.

Recommended practices:

* provide accurate priority or importance hints
* keep tasks resumable and partitionable
* avoid assumptions about execution timing.

Work should be structured so it can run incrementally across multiple scheduling windows.

***

### Signals and Adaptive Behavior

Signals influence work over time through a feedback loop.

Under stress:

* budgets tighten
* lower priority tasks are deferred
* fidelity or frequency may be reduced.

When performance improves:

* budgets relax
* queued work may be processed more aggressively.

The system adapts continuously rather than using fixed thresholds.

***

### Priority and Budget Interaction

Budgeting limits how much work can execute in a window.

Priority determines which tasks receive execution permission within that limit.

The scheduler balances:

* urgency
* locality
* fairness
* overall responsiveness.

Not all requests execute immediately.

***

### Delayed or Skipped Work

Deferred work remains queued for reconsideration.

Skipped work may:

* run later
* execute at reduced fidelity
* be partially processed.

Modules should tolerate delays and design tasks accordingly.

***

### Design Intent

The lifecycle exists to achieve:

* consistent responsiveness through controlled workload
* gradual adjustments based on smoothed signals
* centralized policy decisions separate from execution logic
* graceful degradation through reduced frequency or fidelity.

***

### Common Misconceptions

Requesting work does not guarantee immediate execution.

Frequent overrides of scheduling policies undermine global stability.

Skipping work usually reduces frequency or detail temporarily rather than disabling features.

Heavy work inside frequently called callbacks interferes with scheduling.

***

### Developer Guidelines

* Enqueue intent instead of executing directly.
* Design tasks to be small and resumable.
* Provide clear priority hints.
* Expect deferral and plan for incremental execution.
