# HyperBench ⚡
> Native Hardware Performance & Real-Time Frame Timing Suite for Android

HyperBench is an open-source hardware benchmarking application built with Kotlin and Jetpack Compose. It provides real-time diagnostic performance evaluation for CPU, GPU, RAM bandwidth, and storage I/O subsystems with frame-level timing analysis and cross-platform hardware comparison.

---

## 🚀 Features

- **CPU Compute**: Single-core and multi-core integer math, prime sieve, dense matrix algebra (GFLOPS), and SHA-256 cryptographic throughput.
- **RAM Bandwidth**: Native direct ByteBuffer streaming sequential read/write bandwidth (GB/s) and random access cache latency (ns).
- **Storage I/O**: Sequential read/write throughput (MB/s) and random 4K IOPS read/write with automated test cache cleanup.
- **GPU & Real-Time Rendering**: Interactive 3D particle vortex and dynamic wireframe polyhedron geometry on hardware-accelerated Canvas.
- **Detailed Frame Timing Analysis**:
  - Live FPS (Current, Average, Min, Max).
  - 1% Lows and 0.1% Lows frame times.
  - Real-time scrolling frame time sparkline with 60 FPS (16.6ms) and 120 FPS (8.3ms) guidelines.
  - Quantile distribution histogram (`< 8.3ms`, `8.3–16.6ms`, `16.6–33.3ms`, `> 33.3ms`).
- **Modes**:
  - Individual testing: CPU Only, GPU Only, RAM Only, Disk Only.
  - Combined Full System Stress Test with concurrent contention load and thermal throttling detection.
- **Load Presets & Difficulty**: Light, Medium, and Heavy presets with adjustable Level 1–10 difficulty and automatic hardware calibration.
- **GPU Backends & Driver Selection**:
  - Backends: Vulkan (1.1–1.4), OpenGL ES (3.0–3.2), and WebGPU / Skia.
  - Driver Profiles: System Default, Turnip (Mesa/KGSL for Adreno), Game Driver Developer Hook, and SwiftShader Software.
- **Live Meters**: Real-time battery temperature sensor (°C), RAM usage, CPU load %, GPU frame budget load %, and Shizuku/Root integration.
- **Hardware Tier Scoring**: Multi-axis radar spider charts and percentage deltas comparing against Flagship 2026, Flagship 2024-2025, Mid-Range, Budget, and Desktop PC baselines.
- **Cross-Platform JSON Spec (v2.1.0)**: Export and share benchmark runs across mobile, desktop, and web.

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK 36 (compileSdk 36, minSdk 24)
- Gradle 9.3+

### Local Build Commands
```bash
# Clone the repository
git clone https://github.com/your-username/hyperbench.git
cd hyperbench

# Run unit tests
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🤖 Continuous Integration (GitHub Actions)

A pre-configured GitHub Actions workflow (`.github/workflows/android-ci.yml`) is included. It automatically builds debug and release APKs and runs unit tests on every push or pull request to `main`. When a Git tag (e.g. `v1.0.0`) is pushed, it automatically creates a GitHub Release with attached APK binaries.

---

## 📄 License
This project is open-source under the Apache License 2.0.
