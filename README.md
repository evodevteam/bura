# Bura
**Bura** is an experimental performance mod for Minecraft that targets the underlying causes of frame-time spikes and rendering instability. Instead of only increasing average FPS, Bura focuses on improving consistency so gameplay feels smoother and more responsive.
## Overview

**Bura** is an experimental performance mod for Minecraft that targets the underlying causes of frame-time spikes and rendering instability. Instead of only increasing average FPS, Bura focuses on improving consistency so gameplay feels smoother and more responsive.

The project explores optimizations in chunk rendering pipelines, scheduling, and visibility logic while remaining compatible with existing performance-focused mods.

---

## Project Status

⚠️ **Alpha / In Development**

Bura is currently early-stage software. Expect frequent changes, experimental features, and evolving behavior.

- APIs may change between releases
- Performance results may vary
- Some rendering paths are still unoptimized
- Currently focused primarily on singleplayer environments

---

## Target Environment

- Minecraft Version: 1.21.11
- Mod Loader: Fabric
- Development Environment: IntelliJ IDEA

---

## Features

- Optimized chunk compilation to reduce heavy frame-time spikes
- Improved chunk upload scheduling for smoother GPU workload distribution
- More efficient visibility culling to avoid unnecessary rendering
- Focus on frametime consistency rather than raw FPS numbers
- Designed to complement existing performance mods instead of replacing them

---

## Goals

Bura aims to:

- Reduce micro-stutter and rendering hitching
- Improve frame pacing during world loading and movement
- Explore deeper rendering pipeline improvements
- Provide a lightweight optimization layer compatible with existing ecosystems

---

## Planned Improvements

- Further chunk pipeline optimizations
- Expanded and more aggressive visibility culling
- Improved compatibility with other performance mods
- Profiling-driven optimizations for rendering bottlenecks
- Long-term exploration of deeper rendering system changes

---

## Installation

1. Install Fabric Loader for Minecraft 1.21.11
2. Download the latest Bura release
3. Place the `.jar` file into your `mods` folder
4. Launch the game

---

## Contributing

Contributions, testing, and feedback are welcome, especially during alpha development.

If you want to help:

- Open issues for bugs or performance regressions
- Provide profiling data where possible
- Suggest improvements or ideas

---

## Philosophy

Bura focuses on solving root causes of performance instability rather than applying surface-level fixes. The aim is to make Minecraft feel smoother and more consistent under real gameplay conditions.
